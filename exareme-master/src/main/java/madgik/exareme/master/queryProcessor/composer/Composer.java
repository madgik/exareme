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
import madgik.exareme.master.queryProcessor.composer.Algorithms.AlgorithmProperties.ParameterProperties;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static madgik.exareme.master.engine.iterations.handler.IterationsConstants.iterationsParameterIterDBKey;
import static madgik.exareme.master.engine.iterations.handler.IterationsConstants.terminationConditionTemplateSQLFilename;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerUtils.generateIterationsDBName;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.*;

/**
 * The Composer created the DFL scripts that will be used to execute the algorithms.
 * It uses the algorithm parameters from the request and the algorithm properties from
 * the local algorithm properties.json files.
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

    public String getIterativeAlgorithmFolderPath(
            String algorithmName,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase,
            int iteration) {
        return getAlgorithmFolderPath(algorithmName) + "/" + iterativeAlgorithmPhase.name() + "/" + iteration;
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
        for (ParameterProperties parameter : algorithmProperties.getParameters()) {
            if (parameter.getValue().equals(""))
                continue;
            if (parameter.getType() == ParameterProperties.ParameterType.column) {
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
                    filters = filters.replaceAll("'", "\"");
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
     * @param qKey                the query key, or in general a key for the algorithm
     * @param algorithmProperties the algorithm properties instance
     * @return the generated DFL script
     * @throws ComposerException If the algorithm type or the iterative algorithm phase isn't
     *                           supported or finally, if this method could not retrieve
     *                           ContainerProxies.
     */
    public String composeDFLScript(String qKey,
                                   Algorithms.AlgorithmProperties algorithmProperties
    ) throws ComposerException {
        try {
            return composeDFLScript(qKey, algorithmProperties,
                    ArtRegistryLocator.getArtRegistryProxy() == null ? 0 : ArtRegistryLocator.getArtRegistryProxy().getContainers().length);
        } catch (RemoteException e) {
            throw new ComposerException(e.getMessage());
        }
    }

    /**
     * Composes the DFL script for the given algorithm properties and query.
     * It does not create the script for iterative algorithms.
     *
     * @param algorithmKey        the algorithm key, or in general a key for the algorithm
     * @param algorithmProperties the algorithm properties instance
     * @return the generated DFL script
     * @throws ComposerException If the algorithm type or the iterative algorithm phase isn't
     *                           supported or finally, if this method could not retrieve
     *                           ContainerProxies.
     */
    public String composeDFLScript(
            String algorithmKey,
            Algorithms.AlgorithmProperties algorithmProperties,
            int numberOfWorkers
    ) throws ComposerException {
        // Assigning the proper identifier for the defaultDB
        //      if the dbIdentifier is provided as a parameter or not
        String dbIdentifier = algorithmProperties.getParameterValue(ComposerConstants.dbIdentifierKey);
        if (dbIdentifier == null)
            dbIdentifier = algorithmKey;
        String algorithmName = algorithmProperties.getName();
        String defaultDBFilePath = HBPConstants.DEMO_DB_WORKING_DIRECTORY + dbIdentifier + "_defaultDB.db";
        String transferDBFilePath = HBPConstants.DEMO_DB_WORKING_DIRECTORY + dbIdentifier + "/transfer.db";
        String inputLocalTbl = createLocalTableQuery(algorithmProperties);
        String outputGlobalTbl = "output_" + algorithmKey;

        // Create the dflScript depending on algorithm type
        String dflScript;
        switch (algorithmProperties.getType()) {
            case python_local_global:
                dflScript = composePythonLocalGlobalAlgorithmsDFLScript(algorithmName, outputGlobalTbl,
                        transferDBFilePath, algorithmProperties.getParameters());
                break;
            case local:
                dflScript = composeLocalAlgorithmsDFLScript(algorithmName, inputLocalTbl, outputGlobalTbl,
                        defaultDBFilePath, algorithmProperties.getParameters());
                break;
            case local_global:
                dflScript = composeLocalGlobalAlgorithmsDFLScript(algorithmName, inputLocalTbl, outputGlobalTbl,
                        defaultDBFilePath, algorithmProperties.getParameters());
                break;
            case multiple_local_global:
                dflScript = composeMultipleLocalGlobalAlgorithmsDFLScript(algorithmName, inputLocalTbl, outputGlobalTbl,
                        defaultDBFilePath, algorithmProperties.getParameters());
                break;
            case pipeline:
                dflScript = composePipelineAlgorithmsDFLScript(algorithmName, inputLocalTbl, outputGlobalTbl,
                        defaultDBFilePath, algorithmProperties.getParameters(), numberOfWorkers);
                break;
            case iterative:
                throw new ComposerException("Iterative Algorithms should not call composeDFLScripts");
            default:
                throw new ComposerException("Unable to determine algorithm type.");
        }
        return dflScript;

    }


    /**
     * Returns an exaDFL script for the algorithms of type python_local_global
     *
     * @param algorithmName         the name of the algorithm
     * @param outputGlobalTbl       the name of the output table
     * @param transferDBFilePath    the absolute path of the file where the transfered results will be saved
     * @param algorithmParameters   the parameters of the algorithm
     * @return
     */
    private String composePythonLocalGlobalAlgorithmsDFLScript(
            String algorithmName,
            String outputGlobalTbl,
            String transferDBFilePath,
            ParameterProperties[] algorithmParameters
    ) {
        StringBuilder dflScript = new StringBuilder();

        String localPythonScriptPath = getAlgorithmFolderPath(algorithmName) + "/local.py";
        String globalPythonScriptPath = getAlgorithmFolderPath(algorithmName) + "/global.py";

        // Format local
        dflScript.append("distributed create temporary table output_local_tbl as virtual \n");
        dflScript.append("select * from (\n  call_python_script 'python " + localPythonScriptPath + " ");
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("-%s %s ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("-%s %s ", ComposerConstants.localCSVKey, DATA_DIRECTORY));
        dflScript.append("'\n);\n");

        // Format union
        dflScript
                .append("\ndistributed create temporary table input_global_tbl to 1 as  \n");
        dflScript.append("select * from output_local_tbl;\n");

        // Format global
        dflScript.append(String
                .format("\nusing input_global_tbl \ndistributed create table %s as external \n",
                        outputGlobalTbl));
        dflScript.append("select * from (\n  call_python_script 'python " + globalPythonScriptPath + " ");
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("-%s %s ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("-%s %s ", ComposerConstants.localDBsKey, transferDBFilePath));
        dflScript.append(
                String.format("' select * from (output '%s' select * from input_global_tbl)\n);\n", transferDBFilePath));

        return dflScript.toString();
    }

    /**
     * Returns an exaDFL script for the algorithms of type local
     *
     * @param algorithmName       the name of the algorithm
     * @param inputLocalTbl       the query to read from the local table
     * @param outputGlobalTbl     the table where the output is going to be printed
     * @param defaultDBFileName   the name of the local db that the SQL scripts are going to use
     * @param algorithmParameters the parameters of the algorithm provided
     * @return an ExaDFL script that Exareme will use to run the query
     */
    private String composeLocalAlgorithmsDFLScript(
            String algorithmName,
            String inputLocalTbl,
            String outputGlobalTbl,
            String defaultDBFileName,
            ParameterProperties[] algorithmParameters

    ) {
        StringBuilder dflScript = new StringBuilder();
        String localScriptPath = getAlgorithmFolderPath(algorithmName) + "/local.template.sql";
        String algorithmFolderPath = getAlgorithmFolderPath(algorithmName);

        dflScript.append("distributed create table " + outputGlobalTbl + " as external \n");
        dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalTblKey, inputLocalTbl));
        dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
        dflScript.append(");\n");

        return dflScript.toString();
    }

    /**
     * Returns an exaDFL script for the algorithms of type local_global
     *
     * @param algorithmName       the name of the algorithm
     * @param inputLocalTbl       the query to read from the local table
     * @param outputGlobalTbl     the table where the output is going to be printed
     * @param defaultDBFileName   the name of the local db that the SQL scripts are going to use
     * @param algorithmParameters the parameters of the algorithm provided
     * @return an ExaDFL script that Exareme will use to run the query
     */
    private String composeLocalGlobalAlgorithmsDFLScript(
            String algorithmName,
            String inputLocalTbl,
            String outputGlobalTbl,
            String defaultDBFileName,
            ParameterProperties[] algorithmParameters
    ) {
        StringBuilder dflScript = new StringBuilder();

        String localScriptPath = getAlgorithmFolderPath(algorithmName) + "/local.template.sql";
        String globalScriptPath = getAlgorithmFolderPath(algorithmName) + "/global.template.sql";
        String algorithmFolderPath = getAlgorithmFolderPath(algorithmName);

        // Format local
        dflScript.append("distributed create temporary table output_local_tbl as virtual \n");
        dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalTblKey, inputLocalTbl));

        dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
        dflScript.append(");\n");

        // Format union
        dflScript
                .append("\ndistributed create temporary table input_global_tbl to 1 as  \n");
        dflScript.append("select * from output_local_tbl;\n");

        // Format global
        dflScript.append(String
                .format("\nusing input_global_tbl \ndistributed create table %s as external \n",
                        outputGlobalTbl));
        dflScript.append("select * \n");
        dflScript.append("from (\n");
        dflScript.append(String.format("  execnselect 'path:%s' ", algorithmFolderPath));
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputGlobalTblKey, "input_global_tbl"));

        dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
        dflScript.append(");\n");

        return dflScript.toString();
    }

    /**
     * Returns an exaDFL script for the algorithms of type multiple_local_global
     *
     * @param algorithmName       the name of the algorithm
     * @param inputLocalTbl       the query to read from the local table
     * @param outputGlobalTbl     the table where the output is going to be printed
     * @param defaultDBFileName   the name of the local db that the SQL scripts are going to use
     * @param algorithmParameters the parameters of the algorithm provided
     * @return an ExaDFL script that Exareme will use to run the query
     */
    private String composeMultipleLocalGlobalAlgorithmsDFLScript(
            String algorithmName,
            String inputLocalTbl,
            String outputGlobalTbl,
            String defaultDBFileName,
            ParameterProperties[] algorithmParameters
    ) {
        StringBuilder dflScript = new StringBuilder();

        String algorithmFolderPath = getAlgorithmFolderPath(algorithmName);
        File[] listFiles = new File(algorithmFolderPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        Arrays.sort(listFiles);

        // Iterating through all the local_global folders of the algorithm
        for (int iteration = 1; iteration <= listFiles.length; iteration++) {
            String inputGlobalTbl = "input_global_tbl_" + iteration;
            String outputLocalTbl = "output_local_tbl_" + iteration;
            String tempOutputGlobalTbl = "output_global_tbl_" + iteration;
            String prevOutputGlobalTbl = "output_global_tbl_" + (iteration - 1);
            String currentIterationAlgorithmFolderPath = algorithmFolderPath + "/" + listFiles[iteration - 1].getName();
            String localScriptPath = currentIterationAlgorithmFolderPath + "/local.template.sql";
            String globalScriptPath = currentIterationAlgorithmFolderPath + "/global.template.sql";

            // Format local
            if (iteration > 1)
                dflScript.append(String.format("using %s\n", prevOutputGlobalTbl));
            dflScript.append(String
                    .format("distributed create temporary table %s as virtual \n", outputLocalTbl));
            dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ",
                    currentIterationAlgorithmFolderPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalTblKey, inputLocalTbl));
            if (iteration > 1)
                dflScript.append(String.format("'%s:%s' ", ComposerConstants.prevOutputGlobalTblKey, prevOutputGlobalTbl));
            dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
            dflScript.append(");\n");

            // Format union
            dflScript.append(String.format("\ndistributed create temporary table %s to 1 as \n", inputGlobalTbl));
            dflScript.append(String.format("select * from %s;\n", outputLocalTbl));

            // Format global
            if (iteration != listFiles.length) {
                dflScript.append(String.format(
                        "\nusing %s \ndistributed create temporary table %s as external \n",
                        inputGlobalTbl, tempOutputGlobalTbl));
            } else {
                dflScript.append(String
                        .format("\nusing %s \ndistributed create table %s as external \n",
                                inputGlobalTbl, outputGlobalTbl));
            }
            dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ",
                    currentIterationAlgorithmFolderPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputGlobalTblKey, inputGlobalTbl));
            dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
            dflScript.append(");\n");
        }
        return dflScript.toString();
    }

    /**
     * Returns an exaDFL script for the algorithms of type pipeline
     *
     * @param algorithmName       the name of the algorithm
     * @param inputLocalTbl       the query to read from the local table
     * @param outputGlobalTbl     the table where the output is going to be printed
     * @param defaultDBFileName   the name of the local db that the SQL scripts are going to use
     * @param algorithmParameters the parameters of the algorithm provided
     * @param numberOfWorkers     the number of workers that the algorithm is going to run on
     * @return an ExaDFL script that Exareme will use to run the query
     */
    private String composePipelineAlgorithmsDFLScript(
            String algorithmName,
            String inputLocalTbl,
            String outputGlobalTbl,
            String defaultDBFileName,
            ParameterProperties[] algorithmParameters,
            int numberOfWorkers
    ) {
        StringBuilder dflScript = new StringBuilder();
        String localScriptPath = getAlgorithmFolderPath(algorithmName) + "/local.template.sql";
        String localUpdateScriptPath = getAlgorithmFolderPath(algorithmName) + "/localupdate.template.sql";
        String globalScriptPath = getAlgorithmFolderPath(algorithmName) + "/global.template.sql";
        String algorithmFolderPath = getAlgorithmFolderPath(algorithmName);
        String outputLocalTbl = "output_local_tbl_" + 0;
        String prevOutputLocalTbl;

        dflScript.append(String.format(
                "distributed create temporary table %s as remote \n", outputLocalTbl));
        dflScript.append(String
                .format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalTblKey, inputLocalTbl));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.outputGlobalTblKey, outputGlobalTbl));
        dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
        dflScript.append(");\n");

        for (int iteration = 1; iteration < numberOfWorkers; iteration++) {
            outputLocalTbl = "output_local_tbl_" + iteration;
            prevOutputLocalTbl = "output_local_tbl_" + (iteration - 1);

            dflScript.append(String.format("using %s distributed create temporary table %s as remote \n",
                    prevOutputLocalTbl, outputLocalTbl));
            dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalTblKey, inputLocalTbl));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.outputGlobalTblKey, outputGlobalTbl));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.prevOutputLocalTblKey, prevOutputLocalTbl));
            dflScript.append(String.format("\n  select filetext('%s')\n", localUpdateScriptPath));
            dflScript.append(");\n");
        }

        prevOutputLocalTbl = "output_local_tbl_" + (numberOfWorkers - 1);
        dflScript.append(String.format("using output_local_tbl_%d distributed create table %s as external ",
                (numberOfWorkers - 1), outputGlobalTbl));
        dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalTblKey, inputLocalTbl));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.outputGlobalTblKey, outputGlobalTbl));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.prevOutputLocalTblKey, prevOutputLocalTbl));
        dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
        dflScript.append(");\n");

        return dflScript.toString();
    }

    /**
     * Returns an exaDFL script for the algorithms of type iterative
     *
     * @param algorithmKey            the key of the algorithm
     * @param algorithmProperties     the properties of the algorithm
     * @param iterativeAlgorithmPhase the phase of the iteration
     * @return an ExaDFL script that Exareme will use to run the query
     */
    public String composeIterativeAlgorithmsDFLScript(
            String algorithmKey,
            Algorithms.AlgorithmProperties algorithmProperties,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase
    ) throws ComposerException {
        String dbIdentifier = algorithmProperties.getParameterValue(ComposerConstants.dbIdentifierKey);
        if (dbIdentifier == null)
            dbIdentifier = algorithmKey;
        String algorithmName = algorithmProperties.getName();
        String defaultDBFileName = HBPConstants.DEMO_DB_WORKING_DIRECTORY + dbIdentifier + "_defaultDB.db";
        String inputLocalTbl = createLocalTableQuery(algorithmProperties);
        ParameterProperties[] algorithmParameters = algorithmProperties.getParameters();

        StringBuilder dflScript = new StringBuilder();

        if (iterativeAlgorithmPhase == null)
            throw new ComposerException("Unsupported iterative algorithm phase.");

        String algorithmFolderPath =
                generateIterativeWorkingDirectoryString(algorithmKey, iterativeAlgorithmPhase);
        String outputGlobalTbl = IterationsHandlerDFLUtils.generateIterativePhaseOutputTblName(
                algorithmKey, iterativeAlgorithmPhase);
        String iterationsDBName = generateIterationsDBName(algorithmKey);

        if (iterativeAlgorithmPhase.equals(termination_condition)) {
            // Format termination condition script.
            dflScript.append(String.format("distributed create table %s as external \n", outputGlobalTbl));
            dflScript.append(
                    String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalTblKey, inputLocalTbl));
            dflScript.append(String.format("'%s:%s' ", iterationsParameterIterDBKey, iterationsDBName));
            dflScript.append(String.format("\n  select filetext('%s')\n",
                    algorithmFolderPath + "/" + terminationConditionTemplateSQLFilename));
            dflScript.append(");\n");

            return dflScript.toString();
        }

        // The iteration works like multiple_local_global in the init,step,finalize steps
        File[] listFiles = new File(algorithmFolderPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        Arrays.sort(listFiles);

        for (int iteration = 1; iteration <= listFiles.length; iteration++) {
            String inputGlobalTbl = "input_global_tbl_" + iteration;
            String outputLocalTbl = "output_local_tbl_" + iteration;
            String tempOutputGlobalTbl = "output_global_tbl_" + iteration;
            String prevOutputGlobalTbl = "output_global_tbl_" + (iteration - 1);
            String localSQLScriptsPath =
                    getIterativeAlgorithmFolderPath(algorithmName, iterativeAlgorithmPhase, iteration);
            String localScriptPath = localSQLScriptsPath + "/local.template.sql";

            // Global template SQL scripts should be retrieved from the demo directory because they are modified
            String globalSQLScriptsPath = algorithmFolderPath + "/" + iteration;
            String globalScriptPath = globalSQLScriptsPath + "/global.template.sql";

            if (iteration > 1) {
                dflScript.append(String.format("using %s\n", prevOutputGlobalTbl));
            } else if (iterativeAlgorithmPhase.equals(init)) {
                // Create database directory
                dflScript.append(String.format("distributed create temporary table createPathTempTable as virtual\n" +
                                "select execprogram(null, 'mkdir', '-p', '%s') as C1;\n\n",
                        HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey));
                dflScript.append("using createPathTempTable\n");
            }

            dflScript.append(String.format("distributed create temporary table %s as virtual \n", outputLocalTbl));
            dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", localSQLScriptsPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalTblKey, inputLocalTbl));
            if (iteration > 1)
                dflScript.append(String.format("'%s:%s' ", ComposerConstants.prevOutputGlobalTblKey, prevOutputGlobalTbl));
            dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
            dflScript.append(");\n");

            // format union
            dflScript.append(String.format("\ndistributed create temporary table %s to 1 as \n", inputGlobalTbl));
            dflScript.append(String.format("select * from %s;\n", outputLocalTbl));

            // format global
            if (iteration != listFiles.length) {
                dflScript.append(String.format(
                        "\nusing %s \ndistributed create temporary table %s as external \n",
                        inputGlobalTbl, tempOutputGlobalTbl));
            } else {
                dflScript.append(String
                        .format("\nusing %s \ndistributed create table %s as external \n",
                                inputGlobalTbl, outputGlobalTbl));
            }
            dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ",
                    globalSQLScriptsPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ",
                    IterationsConstants.iterationsParameterIterDBKey, iterationsDBName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputGlobalTblKey, inputGlobalTbl));

            dflScript.append(String.format("\n  select filetext('%s')\n", globalScriptPath));
            dflScript.append(");\n");
        }
        return dflScript.toString();
    }

    // Utilities --------------------------------------------------------------------------------

    /**
     * Provides the folder paths for the iterative algorithms' phases
     * The DEMO_ALGORITHMS_WORKING_DIRECTORY is used because the iterative algorithms
     * do not use the mip-algorithms folder to read the sql scripts.
     * The sql scripts are modified and saved on the demo working directory.
     * If the iterativeAlgorithmPhase is null it returns the directory of the algorithms
     *
     * @param algorithmKey            is the identifier of the algorithm
     * @param iterativeAlgorithmPhase the phase of the iterative algorithm
     * @return the directory where the iterative algorithm's sql scripts are
     * @throws ComposerException if the iterativeAlgorithmPhase is not proper
     */
    private static String generateIterativeWorkingDirectoryString(
            String algorithmKey,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase
    ) throws ComposerException {
        String iterativeAlgorithmsFolderPath = HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY;
        if (iterativeAlgorithmPhase == null)
            return iterativeAlgorithmsFolderPath;

        String algorithmPhaseWorkingDir;
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
     * @throws IOException if writing the DFLScript fails.
     */
    public static void persistDFLScriptToAlgorithmsDemoDirectory(
            String algorithmDemoDirectoryName, String dflScript,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativePhase)
            throws IOException {
        File dflScriptOutputFile;
        if (iterativePhase != null) {
            dflScriptOutputFile = new File(algorithmDemoDirectoryName + "/"
                    + iterativePhase.name() + ComposerConstants.DFL_SCRIPT_FILE_EXTENSION);
        } else {
            dflScriptOutputFile = new File(algorithmDemoDirectoryName
                    + ComposerConstants.DFL_SCRIPT_FILE_EXTENSION);
        }

        if (!dflScriptOutputFile.getParentFile().exists() && !dflScriptOutputFile.getParentFile().mkdirs())
            throw new IOException("Failed to create parent directories: " + dflScriptOutputFile.getParentFile());

        if (!dflScriptOutputFile.createNewFile())
            throw new IOException("Failed to create file : " + dflScriptOutputFile.getAbsolutePath()
                    + " because it already exists");

        FileUtil.writeFile(dflScript, dflScriptOutputFile);
    }
}