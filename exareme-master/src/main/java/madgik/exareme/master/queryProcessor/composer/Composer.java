package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itfsw.query.builder.SqlQueryBuilderFactory;
import com.itfsw.query.builder.support.builder.SqlBuilder;
import com.itfsw.query.builder.support.model.result.SqlQueryResult;
import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.engine.iterations.handler.IterationsConstants;
import madgik.exareme.master.engine.iterations.handler.IterationsHandlerDFLUtils;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static madgik.exareme.master.engine.iterations.handler.IterationsConstants.*;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerUtils.generateIterationsDBName;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.*;

/**
 * TODO write better description when done
 *
 * Responsible to produce data flows (dfl)
 * by combining repository templates and custom parameters.
 *
 */
public class Composer {

    private static final Logger log = Logger.getLogger(Composer.class);
    private static final Composer instance = new Composer();

    // The directory where the algorithms' SQL scripts are
    private static String algorithmsFolderPath;

    private static String DATA_DIRECTORY;
    private static Algorithms algorithms;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        algorithmsFolderPath = AdpProperties.getGatewayProperties().getString("algorithms.path");
        DATA_DIRECTORY = AdpProperties.getGatewayProperties().getString("data.path");
        try {
            algorithms = Algorithms.createAlgorithms(algorithmsFolderPath);
        } catch (IOException e) {
            log.error("Unable to locate repository properties (*.json).", e);
        }
    }

    public static Composer getInstance() {
        return instance;
    }

    public String getAlgorithmFolderPath(String algorithmName) {
        return algorithmsFolderPath + algorithmName;
    }

    public String getAlgorithms() {
        return gson.toJson(algorithms.getAlgorithms(), Algorithms.AlgorithmProperties[].class);
    }

    /**
     * Creates the query that will run against the local dataset file to fetch the data
     *
     * @param algorithmProperties the properties of the algorithm
     * @return a query for the local database
     */

    public String createLocalTableQuery(Algorithms.AlgorithmProperties algorithmProperties) {
        List<String> variables = new ArrayList<>();
        String filters = "";
        for (Algorithms.AlgorithmProperties.ParameterProperties parameter : algorithmProperties.getParameters()) {
            if (parameter.getValue().equals(""))
                continue;
            if (parameter.getType() == Algorithms.AlgorithmProperties.ParameterProperties.ParameterType.database) {
                if (parameter.getValueMultiple()) {
                    variables.addAll(Arrays.asList(parameter.getValue().split("[,+*]")));
                } else {
                    variables.add(parameter.getValue());
                }
            } else if (parameter.getType() == Algorithms.AlgorithmProperties.ParameterProperties.ParameterType.filter) {
                SqlQueryBuilderFactory sqlQueryBuilderFactory = new SqlQueryBuilderFactory();
                SqlBuilder sqlBuilder = sqlQueryBuilderFactory.builder();
                try {   // build query
                    SqlQueryResult sqlQueryResult = sqlBuilder.build(parameter.getValue());
                    filters = String.valueOf(sqlQueryResult);
                    filters = filters.replaceAll("'","\"");
                    log.debug(filters);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (parameter.getType() == Algorithms.AlgorithmProperties.ParameterProperties.ParameterType.dataset) {
                variables.add(parameter.getName());
            }
        }

        StringBuilder builder = new StringBuilder();
        if (variables.isEmpty())
            builder.append("(select * from (file header:t file:" + DATA_DIRECTORY + "))");
        else {
            builder.append("(select ");
            for (String variable : variables) {
                builder.append(variable);
                builder.append(",");
            }
            builder.deleteCharAt(builder.lastIndexOf(","));
            builder.append(" from (file header:t file:" + DATA_DIRECTORY + ")");
            if ("".equals(filters)) {
                builder.append(")");
            } else {
                builder.append(" where " + filters + ")");
            }
            log.info(builder.toString());
        }
        return builder.toString();
    }

    /**
     * Composes the DFL script for the given algorithm properties and query.
     *
     * @param qKey                    the query key, or in general a key for the algorithm
     * @param algorithmProperties     the algorithm properties instance
     * @param iterativeAlgorithmPhase in the case of iterative algorithms, this is one value of
     *                                {@link madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel}
     *                                <b>otherwise, it is null</b>
     * @return the generated DFL script
     * @throws ComposerException If the algorithm type or the iterative algorithm phase isn't
     *                           supported or finally, if this method could not retrieve
     *                           ContainerProxies.
     */
    public String composeVirtual(String qKey,
                                 Algorithms.AlgorithmProperties algorithmProperties,
                                 IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase
    ) throws ComposerException {
        try {
            return composeVirtual(qKey, algorithmProperties, iterativeAlgorithmPhase,
                    ArtRegistryLocator.getArtRegistryProxy() == null ? 0 : ArtRegistryLocator.getArtRegistryProxy().getContainers().length);
        } catch (RemoteException e) {
            throw new ComposerException(e.getMessage());
        }
    }

    /**
     * Composes the DFL script for the given algorithm properties and query.
     *
     * @param algorithmKey            the algorithm key, or in general a key for the algorithm
     * @param algorithmProperties     the algorithm properties instance
     * @param iterativeAlgorithmPhase in the case of iterative algorithms, this is one value of
     *                                {@link madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel}
     *                                <b>otherwise, it is null</b>
     * @return the generated DFL script
     * @throws ComposerException If the algorithm type or the iterative algorithm phase isn't
     *                           supported or finally, if this method could not retrieve
     *                           ContainerProxies.
     */
    public String composeVirtual(String algorithmKey,
                                  Algorithms.AlgorithmProperties algorithmProperties,
                                  IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase,
                                  int numberOfWorkers
    )
            throws ComposerException {

        StringBuilder dflScript = new StringBuilder();

        HashMap<String, String> algorithmParameters =
                Algorithms.AlgorithmProperties.toHashMap(algorithmProperties.getParameters());

        // Assigning the proper identifier for the defaultDB
        //      if the dbIdentifier is provided as a parameter or not
        String dbIdentifier;
        if (algorithmParameters.get(ComposerConstants.dbIdentifierKey) == null) {
            dbIdentifier = algorithmKey;
        }else {
            dbIdentifier = algorithmParameters.get(ComposerConstants.dbIdentifierKey);
            algorithmParameters.remove(ComposerConstants.dbIdentifierKey);      // It is no longer needed
        }
        algorithmParameters.put(ComposerConstants.defaultDBKey,
                HBPConstants.DEMO_DB_WORKING_DIRECTORY + dbIdentifier + "_defaultDB.db");

        String inputLocalTbl = createLocalTableQuery(algorithmProperties);
        algorithmParameters.put(ComposerConstants.inputLocalTblKey, inputLocalTbl);

        String outputGlobalTbl = "output_" + algorithmKey;
        algorithmParameters.put(ComposerConstants.outputGlobalTblKey, outputGlobalTbl);

        File[] listFiles;       // TODO Remove when iterative is refactored
        String currentAlgorithmFolderPath = getAlgorithmFolderPath(algorithmProperties.getName());
        String localScriptPath =
                getAlgorithmFolderPath(algorithmProperties.getName()) + "/local.template.sql";
        String localUpdateScriptPath =
                getAlgorithmFolderPath(algorithmProperties.getName()) + "/localupdate.template.sql";
        String globalScriptPath =
                getAlgorithmFolderPath(algorithmProperties.getName()) + "/global.template.sql";

        // Create the dflScript depending on algorithm type
        switch (algorithmProperties.getType()) {
            case local:
                algorithmParameters.remove(ComposerConstants.outputGlobalTblKey);

                dflScript.append("distributed create table " + outputGlobalTbl + " as external \n");
                dflScript.append(String.format("select * from (\n    execnselect 'path:%s' ", currentAlgorithmFolderPath));
                for (String key : algorithmParameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, algorithmParameters.get(key)));
                }
                dflScript.append(String.format("\n    select filetext('%s')\n", localScriptPath));
                dflScript.append(");\n");

                break;

            case local_global:
                algorithmParameters.remove(ComposerConstants.outputGlobalTblKey);

                // Format local
                dflScript.append("distributed create temporary table output_local_tbl as virtual \n");
                dflScript.append(String.format("select * from (\n    execnselect 'path:%s' ", currentAlgorithmFolderPath));
                for (String key : algorithmParameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, algorithmParameters.get(key)));
                }
                dflScript.append(String.format("\n    select filetext('%s')\n", localScriptPath));
                dflScript.append(");\n");

                // format union
                dflScript
                        .append("\ndistributed create temporary table input_global_tbl to 1 as  \n");
                dflScript.append("select * from output_local_tbl;\n");

                // Format global
                algorithmParameters.remove(ComposerConstants.inputLocalTblKey);
                algorithmParameters.put(ComposerConstants.inputGlobalTblKey, "input_global_tbl");

                dflScript.append(String
                        .format("\nusing input_global_tbl \ndistributed create table %s as external \n",
                                outputGlobalTbl));
                dflScript.append("select * \n");
                dflScript.append("from (\n");
                dflScript.append(String.format("  execnselect 'path:%s' ", currentAlgorithmFolderPath));
                for (String key : algorithmParameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, algorithmParameters.get(key)));
                }
                dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
                dflScript.append(");\n");

                break;

            case multiple_local_global:


                // ------------->  // TODO
                dflScript.append(composeMultipleLocalGlobal(
                        currentAlgorithmFolderPath, algorithmProperties, algorithmParameters, inputLocalTbl,
                        outputGlobalTbl, iterativeAlgorithmPhase
                ));
                // <--------------------

                break;

            case iterative:
                if (iterativeAlgorithmPhase == null)
                    throw new ComposerException("Unsupported iterative algorithm phase.");

                // workingDir and outputGlobalTbl are different on iterative
                currentAlgorithmFolderPath = generateIterativeWorkingDirectoryString(algorithmKey, iterativeAlgorithmPhase);
                outputGlobalTbl = IterationsHandlerDFLUtils.generateIterativePhaseOutputTblName(
                        IterationsConstants.iterationsOutputTblPrefix, algorithmKey, iterativeAlgorithmPhase);

                algorithmParameters.put(IterationsConstants.iterationsParameterIterDBKey,
                        generateIterationsDBName(algorithmKey));
                algorithmParameters.remove(iterationsPropertyConditionQueryProvided);        // Not needed in the dflScript

                if (iterativeAlgorithmPhase.equals(termination_condition)) {
                    // Remove outputGlobalTblKey since it's not needed as an execnselect parameter.
                    algorithmParameters.remove(ComposerConstants.outputGlobalTblKey);

                    // Specify algorithm key to contain the current iterative algorithm's phase.
                    algorithmParameters.put(ComposerConstants.algorithmKey, algorithmProperties.getName() + "/"
                            + iterativeAlgorithmPhase.name());

                    // Format termination condition script.
                    dflScript.append("distributed create table ").append(outputGlobalTbl).append(" as external \n");
                    dflScript.append(
                            String.format("select * from (\n    execnselect 'path:%s' ", currentAlgorithmFolderPath));
                    for (String key : algorithmParameters.keySet()) {
                        dflScript.append(String.format("'%s:%s' ", key, algorithmParameters.get(key)));
                    }
                    dflScript.append(String.format("\n    select filetext('%s')\n",
                            currentAlgorithmFolderPath + "/" + terminationConditionTemplateSQLFilename));
                    dflScript.append(");\n");

                } else {          // The iteration works like multiple_local_global in the init,step,finalize steps
                    // TODO Refactor after multipleLocalGlobal function is done
                    // iterationsPropertyMaximumNumber is needed only in the termination_condition
                    algorithmParameters.remove(iterationsPropertyMaximumNumber);

                    listFiles = new File(currentAlgorithmFolderPath).listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return pathname.isDirectory();
                        }
                    });
                    Arrays.sort(listFiles);
                    for (int i = 0; i < listFiles.length; i++) {
                        algorithmParameters.put(ComposerConstants.inputLocalTblKey, inputLocalTbl);
                        algorithmParameters.put(ComposerConstants.outputGlobalTblKey, outputGlobalTbl);

                        // Create database directory
                        if (iterativeAlgorithmPhase.equals(init) && !listFiles[i].getName().equals("2")) {
                            dflScript.append(String.format("distributed create temporary table createPathTempTable as virtual\n" +
                                            "select execprogram(null, 'mkdir', '-p', '%s') as C1;\n\n",
                                    HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey));
                        }

                        algorithmParameters.put(ComposerConstants.algorithmKey,
                                "/" + algorithmKey
                                        + "/" + iterativeAlgorithmPhase.name()
                                        + "/" + listFiles[i].getName());
                        /*
                         * [Only for iterative algorithms]
                         * Only use previousPhaseOutputTbl parameter (set in outputGlobalTblKey)
                         * if it's the 1st step of a multiple_local_global (for step/finalize
                         * phases).
                         */
                        if ((iterativeAlgorithmPhase.equals(step) ||
                                iterativeAlgorithmPhase.equals(finalize)) && i == 1)
                            algorithmParameters.remove(IterationsConstants.previousPhaseOutputTblVariableName);
                        algorithmParameters.put(ComposerConstants.outputPrvGlobalTblKey, "output_global_tbl_"
                                .concat(String.valueOf(Integer.valueOf(listFiles[i].getName()) - 1)));
                        algorithmParameters.put(ComposerConstants.algorithmIterKey, String.valueOf(i + 1));
                        if (listFiles.length - 1 == i) {
                            algorithmParameters.put(ComposerConstants.isTmpKey, "false");
                        } else {
                            algorithmParameters.put(ComposerConstants.isTmpKey, "true");
                        }
                        dflScript.append(composeLocalGlobal(algorithmParameters,
                                iterativeAlgorithmPhase));
                    }
                }

                break;

            case pipeline:
                dflScript.append(String.format(
                        "distributed create temporary table output_local_tbl_%d as remote \n", 0));
                dflScript.append(String
                        .format("select * from (\n    execnselect 'path:%s' ", currentAlgorithmFolderPath));
                for (String key : algorithmParameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, algorithmParameters.get(key)));
                }
                dflScript.append(
                        String.format("\n    select filetext('%s')\n", localScriptPath));
                dflScript.append(");\n");

                for (int i = 1; i < numberOfWorkers; i++) {
                    dflScript.append(String.format(
                            "using output_local_tbl_%d distributed create temporary table output_local_tbl_%d as remote \n",
                            i - 1, i));

                    dflScript.append(String.format("select * from (\n    execnselect 'path:%s' ", currentAlgorithmFolderPath));
                    for (String key : algorithmParameters.keySet()) {
                        dflScript.append(String.format("'%s:%s' ", key, algorithmParameters.get(key)));
                    }
                    dflScript.append(String.format("'prv_output_local_tbl:(output_local_tbl_%d)' ", i - 1));
                    dflScript.append(String.format("\n    select filetext('%s')\n", localUpdateScriptPath));

                    dflScript.append(");\n");
                }

                dflScript.append(String.format("using output_local_tbl_%d distributed create table %s as external ",
                        (numberOfWorkers - 1), outputGlobalTbl));
                dflScript.append(String.format("select * from (\n    execnselect 'path:%s' ", currentAlgorithmFolderPath));
                for (String key : algorithmParameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, algorithmParameters.get(key)));
                }
                dflScript.append(String.format("'prv_output_local_tbl:(output_local_tbl_%d)' ", numberOfWorkers - 1));
                dflScript.append(String.format("\n    select filetext('%s')\n", globalScriptPath));
                dflScript.append(");\n");

                break;
            default:
                throw new ComposerException("Unable to determine algorithm type.");
        }
        return dflScript.toString();

    }

    // TODO Refactor and unite with composeLocalGlobal
    // TODO Write description afterwards
    private String composeMultipleLocalGlobal(
            String workingDir, Algorithms.AlgorithmProperties algorithmProperties,
            HashMap<String, String> algorithmParameters, String inputLocalTbl, String outputGlobalTbl,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase
    ) {

        File[] listFiles;
        StringBuilder dflScript = new StringBuilder();

        listFiles = new File(workingDir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        Arrays.sort(listFiles);
        for (int i = 0; i < listFiles.length; i++) {
            algorithmParameters.put(ComposerConstants.inputLocalTblKey, inputLocalTbl);
            algorithmParameters.put(ComposerConstants.outputGlobalTblKey, outputGlobalTbl);
            algorithmParameters.put(ComposerConstants.algorithmKey,
                    algorithmProperties.getName() + "/" + listFiles[i].getName());
            algorithmParameters.put(ComposerConstants.algorithmIterKey, String.valueOf(i + 1));
            algorithmParameters.put(ComposerConstants.outputPrvGlobalTblKey, "output_global_tbl_"
                    .concat(String.valueOf(Integer.valueOf(listFiles[i].getName()) - 1)));
            if (listFiles.length - 1 == i) {
                algorithmParameters.put(ComposerConstants.isTmpKey, "false");
            } else {
                algorithmParameters.put(ComposerConstants.isTmpKey, "true");
            }
            dflScript.append(composeLocalGlobal(algorithmParameters,
                    iterativeAlgorithmPhase));
        }
        return dflScript.toString();
    }

    private String composeLocalGlobal(
            HashMap<String, String> algorithmParameters,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase) {

        StringBuilder dflScript = new StringBuilder();

        String algorithmKey = algorithmParameters.get(ComposerConstants.algorithmKey);
        boolean isTmp = Boolean.valueOf(algorithmParameters.get(ComposerConstants.isTmpKey));
        String outputGlobalTbl = algorithmParameters.get(ComposerConstants.outputGlobalTblKey);
        String algorithmIterStr = algorithmParameters.get(ComposerConstants.algorithmIterKey);

        int algorithmIter = Integer.valueOf(algorithmIterStr);
        algorithmParameters.remove(ComposerConstants.outputGlobalTblKey);

        String workingDir;
        if (iterativeAlgorithmPhase != null)
            workingDir = HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey;
        else
            workingDir = getAlgorithmFolderPath(algorithmKey);

        // All local.template.sql scripts are to be retrieved from the algorithms repository
        // directory, since they are not modified (even in iterative algorithms).
        String localScriptsWorkingDir;
        if (iterativeAlgorithmPhase != null) {
            // In iterative algorithms, algorithmKey is comprised of algorithmName + the time the
            // request was received in ms. In a DFL's local execnselect we need to refer to
            // unmodified local.template.sql file. Thus, avoiding a file transfer of the scripts from
            // master to worker containers. Hence, its path must be generated using the algorithm
            // name solely.
            String algorithmName = algorithmKey.substring(1, algorithmKey.lastIndexOf('_'));
            String restOfAlgorithmKey = "/" + iterativeAlgorithmPhase.name()
                    + algorithmKey.substring(algorithmKey.lastIndexOf("/"), algorithmKey.length());
            localScriptsWorkingDir = algorithmsFolderPath + algorithmName + restOfAlgorithmKey;
        } else
            localScriptsWorkingDir = algorithmsFolderPath + algorithmKey;

        String localScriptPath = localScriptsWorkingDir + "/local.template.sql";
        String globalScriptPath = workingDir + "/global.template.sql";


        if (algorithmIter > 1) {
            dflScript.append(String.format("using output_global_tbl_%d\n", algorithmIter - 1));
        } else if (iterativeAlgorithmPhase != null && iterativeAlgorithmPhase.equals(init)) {
            dflScript.append("using createPathTempTable\n");
        }

        dflScript.append(String
                .format("distributed create temporary table output_local_tbl_%d as virtual \n",
                        algorithmIter));
        dflScript.append(String.format("select * from (\n    execnselect 'path:%s' ",
                localScriptsWorkingDir));
        for (String key : algorithmParameters.keySet()) {
            dflScript.append(String.format("'%s:%s' ", key, algorithmParameters.get(key)));
        }
        dflScript.append(String.format("\n    select filetext('%s')\n", localScriptPath));
        dflScript.append(");\n");

        // format union
        dflScript.append(String
                .format("\ndistributed create temporary table input_global_tbl_%d to 1 as \n",
                        algorithmIter));
        dflScript.append(String.format("select * from output_local_tbl_%d;\n", algorithmIter));

        // format global
        algorithmParameters.remove(ComposerConstants.outputPrvGlobalTblKey);
        if (algorithmParameters.get(ComposerConstants.inputLocalTblKey) != null) {
            algorithmParameters.remove(ComposerConstants.inputLocalTblKey);
            algorithmParameters.put("input_global_tbl", String.format("input_global_tbl_%d", algorithmIter));
        }

        if (isTmp)
            dflScript.append(String.format(
                    "\nusing input_global_tbl_%d \ndistributed create temporary table output_global_tbl_%d as external \n",
                    algorithmIter, algorithmIter));
        else
            dflScript.append(String
                    .format("\nusing input_global_tbl_%d \ndistributed create table %s as external \n",
                            algorithmIter, outputGlobalTbl));

        dflScript.append("select * \n");
        dflScript.append("from (\n");
        dflScript.append(String.format("  execnselect 'path:%s' ", workingDir));
        for (String key : algorithmParameters.keySet()) {
            dflScript.append(String.format("'%s:%s' ", key, algorithmParameters.get(key)));
        }
        dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
        dflScript.append(");\n");
        algorithmParameters.remove("input_global_tbl");
        return dflScript.toString();
    }


    // Utilities --------------------------------------------------------------------------------

    /**
     * Provides the folder paths for the iterative algorithms' phases
     * The DEMO_ALGORITHMS_WORKING_DIRECTORY is used because the iterative algorithms
     * do not use the mip-algorithms folder to read the sql scripts.
     * The sql scripts are modified and saved on the demo working directory.
     *
     * @param algorithmKey              is the identifier of the algorithm
     * @param iterativeAlgorithmPhase   the phase of the iterative algorithm
     * @return                          the directory where the iterative algorithm's sql scripts are
     * @throws ComposerException        if the iterativeAlgorithmPhase is not proper
     */
    public static String generateIterativeWorkingDirectoryString(
            String algorithmKey,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase)
            throws ComposerException {

        if (iterativeAlgorithmPhase == null)
            throw new ComposerException("IterativeAlgorithmPhasesModel should not be null");

        String algorithmPhaseWorkingDir;

        // TODO   algorithmWorkingDirectory should not be hardcoded anywhere
        String iterativeAlgorithmsFolderPath = HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY;
        switch (iterativeAlgorithmPhase) {
            case init:
                algorithmPhaseWorkingDir = iterativeAlgorithmsFolderPath + "/" + algorithmKey + "/" + init.name();
                break;
            case step:
                algorithmPhaseWorkingDir = iterativeAlgorithmsFolderPath + "/" + algorithmKey + "/" + step.name();
                break;
            case termination_condition:
                algorithmPhaseWorkingDir = iterativeAlgorithmsFolderPath + "/" + algorithmKey + "/" + termination_condition.name();
                break;
            case finalize:
                algorithmPhaseWorkingDir = iterativeAlgorithmsFolderPath + "/" + algorithmKey + "/" + finalize.name();
                break;
            default:
                throw new ComposerException("Unsupported iterative algorithm case.");
        }
        return algorithmPhaseWorkingDir;
    }

    /**
     * Persists DFL Script on disk, at demo algorithm's directory - for an algorithm's particular
     * execution.
     *
     * @param algorithmDemoDirectoryName the algorithm's demo execution directory
     * @param dflScript                  the algorithm's particular execution DFL scripts
     * @throws IOException               if writing the DFLScript fails.
     */
    public static void persistDFLScriptToAlgorithmsDemoDirectory(
            String algorithmDemoDirectoryName, String dflScript,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativePhase)
            throws IOException {
        File dflScriptOutputFile;
        if (iterativePhase != null) {
            dflScriptOutputFile = new File(algorithmDemoDirectoryName + "/"
                    + iterativePhase.name() + ComposerConstants.DFL_SCRIPT_FILE_EXTENSION);
        }else {
            dflScriptOutputFile = new File(algorithmDemoDirectoryName
                    + ComposerConstants.DFL_SCRIPT_FILE_EXTENSION);
        }
        
        if(!dflScriptOutputFile.getParentFile().mkdirs())
            throw new IOException("Failed to create parent directories: " + dflScriptOutputFile.getParentFile());

        if(!dflScriptOutputFile.createNewFile())
            throw new IOException("Failed to create file : " + dflScriptOutputFile.getAbsolutePath()
                    + " because it already exists");

        FileUtil.writeFile(dflScript, dflScriptOutputFile);
    }
}