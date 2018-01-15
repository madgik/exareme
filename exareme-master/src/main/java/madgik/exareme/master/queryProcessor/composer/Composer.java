package madgik.exareme.master.queryProcessor.composer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.engine.iterations.handler.IterationsConstants;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;

import static madgik.exareme.master.engine.iterations.handler.IterationsConstants.iterationsPropertyConditionQueryProvided;
import static madgik.exareme.master.engine.iterations.handler.IterationsConstants.iterationsPropertyMaximumNumber;
import static madgik.exareme.master.engine.iterations.handler.IterationsConstants.terminationConditionTemplateSQLFilename;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerUtils.generateIterationsDBName;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.finalize;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.init;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition;

/**
 * Responsible to produce data flows (dfl)
 * by combining repository templates and custom parameters.
 * @author alexpap
 * @version 0.1
 */
public class Composer {

    private static final Logger log = Logger.getLogger(Composer.class);
    private static String[] inputVariables = new String[]{"variable","column1", "column2", "groupings","covariables","dataset","x","y"};
    private Composer() {
    }
    private static final Composer instance = new Composer();
    private static String repoPath = null;
    private static AlgorithmsProperties algorithms = null;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        try {
            repoPath = AdpProperties.getGatewayProperties().getString("demo.repository.path");
            log.trace(repoPath);
            algorithms = AlgorithmsProperties.createAlgorithms(repoPath);
        } catch (IOException e) {
            log.error("Unable to locate repository properties (*.json).", e);
        }
    }

    public static Composer getInstance() {
        return instance;
    }


    public String getRepositoryPath() {
        return repoPath;
    }

    public String getEndpoints() throws ComposerException {
        return gson.toJson(algorithms.getEndpoints(), AlgorithmsProperties.EndpointProperties[].class);
    }

    public String getAlgorithms() throws ComposerException {
        return gson.toJson(algorithms.getAlgorithms(), AlgorithmsProperties.AlgorithmProperties[].class);
    }

    public String getAlgorithmsProperties() throws ComposerException{
        return gson.toJson(algorithms, AlgorithmsProperties.class);
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
                                 AlgorithmsProperties.AlgorithmProperties algorithmProperties,
                                 String query,
                                 IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase
    ) throws ComposerException {
        return composeVirtual(null, qKey, algorithmProperties, query, iterativeAlgorithmPhase);
    }

    /**
     * Composes the DFL script for the given algorithm properties and query.
     *
     * @param repositoryPath          the algorithm's repository path
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
    public String composeVirtual(String repositoryPath, String qKey,
                                 AlgorithmsProperties.AlgorithmProperties algorithmProperties,
                                 String query,
                                 IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase
                                 )
        throws ComposerException {

        StringBuilder dflScript = new StringBuilder();

        String workingDir;
        if (iterativeAlgorithmPhase == null)
            workingDir = generateWorkingDirectoryString(repositoryPath, algorithmProperties.getName(),
                        null);
        else
            workingDir = generateWorkingDirectoryString(repositoryPath, qKey, iterativeAlgorithmPhase);

        HashMap<String, String> parameters =
                AlgorithmsProperties.AlgorithmProperties.toHashMap(algorithmProperties);
        String localScriptPath =
            repoPath + algorithmProperties.getName() + "/local.template.sql";
        String localUpdateScriptPath =
                repoPath + algorithmProperties.getName() + "/localupdate.template.sql";
        String globalScriptPath =
            repoPath + algorithmProperties.getName() + "/global.template.sql";
        // get filters
        List<String> variables = new ArrayList<>();
        for (String inputVariable : inputVariables) {
            if(parameters.containsKey(inputVariable)){
                String s = parameters.get(inputVariable);
                if("covariables".equals(inputVariable) || "groupings".equals(inputVariable)){
                    for (String s1 : s.split(",")) {
                        variables.add(s1);
                    }
                }
                else if( "dataset".equals(inputVariable) ){
                    variables.add("dataset");
                }
                else if("x".equals(inputVariable)){
                    for (String s1 :s.split("\\+|\\*")){
                        variables.add(s1);
                    }
                }
                else if("y".equals(inputVariable)){
                    variables.add(s);
                }
                else{
                    //if (!"dataset".equals(inputVariable))
                        variables.add(s);
                }
            }
        }
        String inputLocalTbl;
        if(variables.isEmpty())
             inputLocalTbl = algorithms.getLocal_engine_default().toUDF(query);
        else
            inputLocalTbl = algorithms.getLocal_engine_default().toUDF(variables);

        log.info("lcltble : "+inputLocalTbl);
        parameters.put(ComposerConstants.inputLocalTblKey, inputLocalTbl);
        String outputGlobalTbl = parameters.get(ComposerConstants.outputGlobalTblKey);

        if (iterativeAlgorithmPhase != null) {
            // Handle iterations specific logic, related to Composer
            // 1. Create iterationsDB and defaultDB algorithm parameters.
            // qKey is actually algorithm key in the case of iterative algorithms.
            parameters.put(IterationsConstants.iterationsParameterIterDBKey,
                    generateIterationsDBName(qKey));
            parameters.put(ComposerConstants.defaultDBKey,
                    HBPConstants.DEMO_DB_WORKING_DIRECTORY + qKey + "_defaultDB.db");

            // 2. Remove unneeded parameter
            parameters.remove(iterationsPropertyConditionQueryProvided);

            // 3. Remove max iterations for all iterative phases except for termination_condition.
            if (!iterativeAlgorithmPhase.equals(
                    IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition))
                parameters.remove(iterationsPropertyMaximumNumber);
        }
        else {
            parameters.put(ComposerConstants.defaultDBKey,
                    HBPConstants.DEMO_DB_WORKING_DIRECTORY + qKey + "/defaultDB.db");
        }

        switch (algorithmProperties.getType()) {

            case local:
                parameters.remove(ComposerConstants.outputGlobalTblKey);

                String lp = repoPath + algorithmProperties.getName() + "/local.template.sql";

                // format local
                dflScript.append("distributed create table " + outputGlobalTbl + " as external \n");
                dflScript.append(
                    String.format("select * from (\n    execnselect 'path:%s' ", workingDir));
                for (String key : parameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
                }
                dflScript.append(String.format("\n    select filetext('%s')\n", lp));
                dflScript.append(");\n");
                break;
            case pipeline:
                parameters.remove(ComposerConstants.inputLocalTblKey);

                ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();
                ContainerProxy[] containerProxies;
                try {
                    containerProxies = ArtRegistryLocator.getArtRegistryProxy().getContainers();
                } catch (RemoteException e) {
                    throw new ComposerException("Failed to retrieve containerProxies");
                }

                for(int i = 0; i < containerProxies.length; i++){

                    if(i == 0 ){
                            dflScript.append(String.format(
                                "distributed create temporary table output_local_tbl_%d as remote \n", i));
                    } else {
                        dflScript.append(String.format(
                            "using output_local_tbl_%d distributed create temporary table output_local_tbl_%d as remote \n",
                                i-1, i));
                    }
                    dflScript.append(String
                        .format("select * from (\n    execnselect 'path:%s' ", workingDir));
                    for (String key : parameters.keySet()) {
                        dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
                    }

                    if (i == 0 ) {
                        dflScript.append(
                                String.format("\n    select filetext('%s')\n", localScriptPath));
                    }else {
                        dflScript.append(String.format("'prv_output_local_tbl:(output_local_tbl_%d)' ", i-1));
                        dflScript.append(
                                String.format("\n    select filetext('%s')\n", localUpdateScriptPath));
                    }
                    dflScript.append(");\n");
                }

                dflScript.append(String.format("using output_local_tbl_%d distributed create table %s as ",
                        (containerProxies.length-1), outputGlobalTbl));
                dflScript.append(String
                        .format("select * from (\n    execnselect 'path:%s' ", workingDir));
                for (String key : parameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
                }

                dflScript.append(String.format("'prv_output_local_tbl:(output_local_tbl_%d)' ",
                                containerProxies.length-1));
                dflScript.append( String.format("\n    select filetext('%s')\n", globalScriptPath));
                dflScript.append(");\n");


                break;
            case local_global:
                parameters.remove(ComposerConstants.outputGlobalTblKey);

                // format local
                dflScript
                    .append("distributed create temporary table output_local_tbl as virtual \n");
                dflScript.append(
                    String.format("select * from (\n    execnselect 'path:%s' ", workingDir));
                for (String key : parameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
                }
                dflScript.append(String.format("\n    select filetext('%s')\n", localScriptPath));
                dflScript.append(");\n");

                // format union
                dflScript
                    .append("\ndistributed create temporary table input_global_tbl to 1 as  \n");
                dflScript.append("select * from output_local_tbl;\n");

                // format global
                if (parameters.containsKey(ComposerConstants.inputLocalTblKey)) {
                    parameters.remove(ComposerConstants.inputLocalTblKey);
                    parameters.put(ComposerConstants.inputGlobalTblKey, "input_global_tbl");
                }


                dflScript.append(String
                    .format("\nusing input_global_tbl \ndistributed create table %s as external \n",
                        outputGlobalTbl));
                dflScript.append("select * \n");
                dflScript.append("from (\n");
                dflScript.append(String.format("  execnselect 'path:%s' ", workingDir));
                for (String key : parameters.keySet()) {
                    dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
                }
                dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
                dflScript.append(");\n");

                break;
            case multiple_local_global:
                File[] listFiles = new File(workingDir).listFiles(new FileFilter() {
                    @Override public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
                Arrays.sort(listFiles);
                for (int i = 0; i < listFiles.length; i++) {

                    parameters.put(ComposerConstants.inputLocalTblKey, inputLocalTbl);

                    // Iterations support distinction
                    if (iterativeAlgorithmPhase == null) {
                        parameters.put(ComposerConstants.algorithmKey,
                            algorithmProperties.getName() + "/" + listFiles[i].getName());
                    }
                    else {
                        /*
                         * qKey here is actually the algorithm's key, which is the one we want to
                         * use. This is due to the requirement for persisting algorithm template
                         * and DFL scripts in demo-algorithm's directory, for a **particular**
                         * algorithm execution.
                         */
                        parameters.put(ComposerConstants.algorithmKey,
                                "/" + qKey
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
                            parameters.remove(IterationsConstants.previousPhaseOutputTblVariableName);
                    }

                    parameters.put(ComposerConstants.outputGlobalTblKey, outputGlobalTbl);
                    parameters.put(ComposerConstants.outputPrvGlobalTblKey, "output_global_tbl_"
                        .concat(String.valueOf(Integer.valueOf(listFiles[i].getName()) - 1)));
                    parameters.put(ComposerConstants.algorithmIterKey, String.valueOf(i + 1));
                    if (listFiles.length - 1 == i) {
                        parameters.put(ComposerConstants.isTmpKey, "false");
                    } else {
                        parameters.put(ComposerConstants.isTmpKey, "true");
                    }
                    dflScript.append(composeLocalGlobal(repositoryPath, parameters,
                            iterativeAlgorithmPhase));
                }
                break;
            case iterative:
                // Handling special iterative case, such as termination_condition DFL.
                if (iterativeAlgorithmPhase != null &&
                        iterativeAlgorithmPhase.equals(termination_condition)) {

                    // Remove outputGlobalTblKey since it's not needed as an execnselect parameter.
                    parameters.remove(ComposerConstants.outputGlobalTblKey);

                    // Specify algorithm key to contain the current iterative algorithm's phase.
                    parameters.put(ComposerConstants.algorithmKey, algorithmProperties.getName() + "/"
                                    + iterativeAlgorithmPhase.name());

                    // Format termination condition script.
                    dflScript.append("distributed create table ").append(outputGlobalTbl).append(" as external \n");
                    dflScript.append(
                            String.format("select * from (\n    execnselect 'path:%s' ", workingDir));
                    for (String key : parameters.keySet()) {
                        dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
                    }
                    dflScript.append(String.format("\n    select filetext('%s')\n",
                            workingDir + "/" + terminationConditionTemplateSQLFilename ));
                    dflScript.append(");\n");
                }
                else
                    throw new ComposerException("Unsupported iterative algorithm phase.");
                break;
            default:
                throw new ComposerException("Unable to determine algorithm type.");
        }
        return dflScript.toString();

    }

    private static String composeLocalGlobal(
            String repositoryPath,
            HashMap<String, String> parameters,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase)
        throws ComposerException {

        StringBuilder dflScript = new StringBuilder();

        String algorithmKey = parameters.get(ComposerConstants.algorithmKey);
        boolean isTmp = Boolean.valueOf(parameters.get(ComposerConstants.isTmpKey));
        String inputLocalTbl = parameters.get(ComposerConstants.inputLocalTblKey);
        String outputGlobalTbl = parameters.get(ComposerConstants.outputGlobalTblKey);
        String prvOutputGlobalTbl = parameters.get(ComposerConstants.outputPrvGlobalTblKey);
        String algorithmIterstr = parameters.get(ComposerConstants.algorithmIterKey);

        int algorithmIter = Integer.valueOf(algorithmIterstr);
        parameters.remove(ComposerConstants.outputGlobalTblKey);

        if (repositoryPath == null)
            repositoryPath = Composer.repoPath;
        String workingDir = repositoryPath + algorithmKey;

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
            localScriptsWorkingDir = repoPath + algorithmName + restOfAlgorithmKey;
        }
        else
            localScriptsWorkingDir = repoPath + algorithmKey;

        String localScriptPath = localScriptsWorkingDir + "/local.template.sql";
        String globalScriptPath = workingDir + "/global.template.sql";

        // format local
        if (algorithmIter > 1) {
            dflScript.append(String.format("using output_global_tbl_%d\n", algorithmIter - 1));
        }

        dflScript.append(String
            .format("distributed create temporary table output_local_tbl_%d as virtual \n",
                algorithmIter));
        dflScript.append(String.format("select * from (\n    execnselect 'path:%s' ",
                localScriptsWorkingDir));
        for (String key : parameters.keySet()) {
            dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
        }
        dflScript.append(String.format("\n    select filetext('%s')\n", localScriptPath));
        dflScript.append(");\n");

        // format union
        dflScript.append(String
            .format("\ndistributed create temporary table input_global_tbl_%d to 1 as \n",
                algorithmIter));
        dflScript.append(String.format("select * from output_local_tbl_%d;\n", algorithmIter));

        // format global
        parameters.remove(ComposerConstants.outputPrvGlobalTblKey);
        for (String key : parameters.keySet()) {
            if (key.equals("input_local_tbl")) {
                parameters.remove("input_local_tbl");
                parameters
                    .put("input_global_tbl", String.format("input_global_tbl_%d", algorithmIter));
                break;
            }
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
        for (String key : parameters.keySet()) {
            dflScript.append(String.format("'%s:%s' ", key, parameters.get(key)));
        }
        dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
        dflScript.append(");\n");
        parameters.remove("input_global_tbl");
        return dflScript.toString();
    }


    // Utilities --------------------------------------------------------------------------------
    /**
     * Persists DFL Script on disk, at demo algorithm's directory - for an algorithm's particular
     * execution.
     *
     * @param algorithmDemoDirectoryName the algorithm's demo execution directory
     * @param dflScript                  the algorithm's particular execution DFL scripts
     * @throws ComposerException if writing a DFLScript fails.
     */
    public static void persistDFLScriptToAlgorithmsDemoDirectory(
            String algorithmDemoDirectoryName, String dflScript,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativePhase)
            throws ComposerException {
        File dflScriptOutputFile;
        if (iterativePhase != null)
            dflScriptOutputFile = new File(algorithmDemoDirectoryName + "/"
                    + iterativePhase.name() + ComposerConstants.DFL_SCRIPT_FILE_EXTENSION);
        else
            dflScriptOutputFile = new File(algorithmDemoDirectoryName
                    + ComposerConstants.DFL_SCRIPT_FILE_EXTENSION);

        try {
            dflScriptOutputFile.getParentFile().mkdirs();
            Files.createFile(dflScriptOutputFile.toPath());
            dflScriptOutputFile.createNewFile();
            FileUtil.writeFile(dflScript, dflScriptOutputFile);
        } catch (IOException e) {
            throw new ComposerException("Failed to persist DFL Script ["
                    + dflScriptOutputFile.getName() + "].");
        }
    }

    /**
     * Generates the working directory string, depending on the algorithm name and in the case of
     * iterative algorithms, its current phase.
     *
     * @param repositoryPath          the algorithm's repository path, if it's null, then the
     *                                default (Composer's) repository path is used
     * @param algorithmName           the algorithm's name or query's key in case of non iterative
     *                                algorithm, can be null
     * @param iterativeAlgorithmPhase the iterative algorithm phase for which to generate working
     *                                directory String<br> <b>In the case of non iterative
     *                                algorithms this is null</b>
     * @return the working directory string
     * @throws ComposerException In case of iterative algorithms, if an iterative phase is not
     *                           supported.
     * @see madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel
     */
    public static String generateWorkingDirectoryString(
            String repositoryPath,
            String algorithmName,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase)
            throws ComposerException {

        if (repositoryPath == null)
            repositoryPath = Composer.repoPath;

        String workingDir;
        if (iterativeAlgorithmPhase == null)
            workingDir = repositoryPath + algorithmName;
        else {
            switch (iterativeAlgorithmPhase) {
                case init:
                    workingDir = repositoryPath + "/" +  algorithmName + "/" + init.name();
                    break;
                case step:
                    workingDir = repositoryPath + "/" + algorithmName + "/" + step.name();
                    break;
                case termination_condition:
                    workingDir = repositoryPath + "/" + algorithmName + "/" + termination_condition.name();
                    break;
                case finalize:
                    workingDir = repositoryPath + "/" + algorithmName + "/" + finalize.name();
                    break;
                default:
                    throw new ComposerException("Unsupported iterative algorithm case.");
            }
        }
        return workingDir;
    }
}
