package madgik.exareme.master.queryProcessor.composer;

import com.itfsw.query.builder.SqlQueryBuilderFactory;
import com.itfsw.query.builder.support.builder.SqlBuilder;
import com.itfsw.query.builder.support.model.result.SqlQueryResult;
import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.engine.iterations.handler.IterationsConstants;
import madgik.exareme.master.engine.iterations.handler.IterationsHandlerDFLUtils;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.queryProcessor.composer.Exceptions.ComposerException;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.properties.GenericProperties;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
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

    /**
     * Creates the query that will run against the local dataset file to fetch the data
     *
     * @param algorithmProperties the properties of the algorithm
     * @return a query for the local database
     */
    private static String createLocalTableQuery(AlgorithmProperties algorithmProperties) throws ComposerException {
        List<String> variables = new ArrayList<>();
        List<String> datasets = new ArrayList<>();
        String filters = "";
        for (ParameterProperties parameter : algorithmProperties.getParameters()) {
            if (parameter.getValue().equals(""))
                continue;

            if (parameter.getType() == ParameterProperties.ParameterType.column) {
                for (String variable : Arrays.asList(parameter.getValue().split("[,]"))) {
                    if (variables.contains(variable)) {
                        throw new ComposerException("Column '" + variable + "' was given twice as input. This is not allowed.");
                    }
                    variables.add(variable);
                }
            } else if (parameter.getType() == ParameterProperties.ParameterType.formula) {
                for (String variable : Arrays.asList(parameter.getValue().split("[+\\-*:]+"))) {
                    if (!variable.equals("0"))
                        variables.add(variable);
                }
            } else if (parameter.getType() == ParameterProperties.ParameterType.filter) {
                SqlQueryBuilderFactory sqlQueryBuilderFactory = new SqlQueryBuilderFactory();
                SqlBuilder sqlBuilder = sqlQueryBuilderFactory.builder();
                try {   // build query
                    SqlQueryResult sqlQueryResult = sqlBuilder.build(parameter.getValue());
                    filters = String.valueOf(sqlQueryResult);
                    filters = filters.replace("'", "\"");
                    log.debug(filters);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (parameter.getType() == ParameterProperties.ParameterType.dataset) {
                for (String dataset : Arrays.asList(parameter.getValue().split("[,]"))) {
                    if (datasets.contains(dataset)) {
                        throw new ComposerException("Dataset '" + dataset + "' was given twice as input. This is not allowed.");
                    }
                    datasets.add(dataset);
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        boolean whereAdded = false;
        if (variables.isEmpty())
            builder.append("select * from (" + ComposerConstants.getDBDataTablename() + ")");
        else {
            builder.append("select ");
            for (String variable : variables) {
                builder.append(variable);
                builder.append(",");
            }
            builder.deleteCharAt(builder.lastIndexOf(","));
            builder.append(" from (" + ComposerConstants.getDBDataTablename() + ")");
            if (!"".equals(filters)) {
                builder.append(" where " + filters);
                whereAdded = true;
            }
            if (!datasets.isEmpty()) {
                if (!whereAdded) {
                    builder.append(" where ");
                } else {
                    builder.append(" AND ");
                }
                builder.append(" ( dataset IN (");
                for (String dataset : datasets) {
                    builder.append("\"" + dataset + "\",");
                }
                builder.deleteCharAt(builder.lastIndexOf(","));
                builder.append("))");
            }

            log.debug(builder.toString());
        }
        return builder.toString();
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
    public static String composeDFLScript(
            String algorithmKey,
            AlgorithmProperties algorithmProperties,
            int numberOfWorkers
    ) throws ComposerException {
        // Assigning the proper identifier for the defaultDB
        //      if the dbIdentifier is provided as a parameter or not
        String dbIdentifier = algorithmProperties.getParameterValue(ComposerConstants.dbIdentifierKey);

        // If the algorithm does not have a dbIdentifier parameter we assign the algorithmKey as the identifier
        if (dbIdentifier == null)
            dbIdentifier = algorithmKey;

        // If the algorithm has a dbIdentifier parameter but is blank, we set is to the algorithmKey
        if (dbIdentifier.equals("")) {
            dbIdentifier = algorithmKey;
            algorithmProperties.setParameterValue(ComposerConstants.dbIdentifierKey, dbIdentifier);
        }

        String algorithmName = algorithmProperties.getName();
        String defaultDBFilePath = HBPConstants.DEMO_DB_WORKING_DIRECTORY + dbIdentifier + "_defaultDB.db";
        String inputLocalDB = getDataPath(algorithmProperties);
        String dbQuery = createLocalTableQuery(algorithmProperties);
        // Escaping double quotes for python algorithms because they are needed elsewhere
        String pythonDBQuery = dbQuery.replace("\"", "\\\"");
        ArrayList<Pair<String, String>> csvDatabaseProperties = getCSVDatabaseProperties();
        String outputGlobalTbl = "output_" + algorithmKey;

        // Create the dflScript depending on algorithm type
        String dflScript;
        switch (algorithmProperties.getType()) {
            case local:
                dflScript = composeLocalAlgorithmsDFLScript(algorithmName, inputLocalDB, dbQuery, csvDatabaseProperties, outputGlobalTbl,
                        defaultDBFilePath, algorithmProperties.getParameters());
                break;
            case local_global:
                dflScript = composeLocalGlobalAlgorithmsDFLScript(algorithmName, inputLocalDB, dbQuery, csvDatabaseProperties,
                        outputGlobalTbl, defaultDBFilePath, algorithmProperties.getParameters());
                break;
            case multiple_local_global:
                dflScript = composeMultipleLocalGlobalAlgorithmsDFLScript(algorithmName, inputLocalDB, dbQuery, csvDatabaseProperties, outputGlobalTbl,
                        defaultDBFilePath, algorithmProperties.getParameters());
                break;
            case pipeline:
                dflScript = composePipelineAlgorithmsDFLScript(algorithmName, inputLocalDB, dbQuery, csvDatabaseProperties, outputGlobalTbl,
                        defaultDBFilePath, algorithmProperties.getParameters(), numberOfWorkers);
                break;
            case python_local:
                dflScript = composePythonLocalAlgorithmsDFLScript(algorithmName, inputLocalDB, pythonDBQuery, csvDatabaseProperties,
                        outputGlobalTbl, algorithmProperties.getParameters());
                break;
            case python_local_global:
                dflScript = composePythonLocalGlobalAlgorithmsDFLScript(algorithmName, algorithmKey, inputLocalDB,
                        pythonDBQuery, csvDatabaseProperties, outputGlobalTbl, algorithmProperties.getParameters());
                break;
            case python_multiple_local_global:
                dflScript = composePythonMultipleLocalGlobalAlgorithmsDFLScript(algorithmName, algorithmKey,
                        inputLocalDB, pythonDBQuery, csvDatabaseProperties, outputGlobalTbl, algorithmProperties.getParameters());
                break;
            case iterative:
                throw new ComposerException("Iterative Algorithms should not call composeDFLScripts");
            case python_iterative:
                throw new ComposerException("Python iterative Algorithms should not call composeDFLScripts");
            default:
                throw new ComposerException("Unable to determine algorithm type.");
        }
        return dflScript;

    }

    /**
     * Returns an exaDFL script for the algorithms of type local
     *
     * @param algorithmName         the name of the algorithm
     * @param inputLocalDB          the location of the local database
     * @param dbQuery               the query to execute on the database
     * @param csvDatabaseProperties the csv database properties to construct the sql query
     * @param outputGlobalTbl       the table where the output is going to be printed
     * @param defaultDBFileName     the name of the local db that the SQL scripts are going to use
     * @param algorithmParameters   the parameters of the algorithm provided
     * @return an ExaDFL script that Exareme will use to run the query
     */
    private static String composeLocalAlgorithmsDFLScript(
            String algorithmName,
            String inputLocalDB,
            String dbQuery,
            ArrayList<Pair<String, String>> csvDatabaseProperties,
            String outputGlobalTbl,
            String defaultDBFileName,
            ParameterProperties[] algorithmParameters
    ) {
        StringBuilder dflScript = new StringBuilder();
        String localScriptPath = ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/local.template.sql";
        String algorithmFolderPath = ComposerConstants.getAlgorithmFolderPath(algorithmName);

        dflScript.append("distributed create table " + outputGlobalTbl + " as direct \n");
        dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.dbQueryKey, dbQuery));
        for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
            dflScript.append(String.format("'%s:%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
        }
        dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
        dflScript.append(");\n");

        return dflScript.toString();
    }

    /**
     * Returns an exaDFL script for the algorithms of type local_global
     *
     * @param algorithmName         the name of the algorithm
     * @param inputLocalDB          the query to read from the local table
     * @param dbQuery               the query to execute on the database
     * @param csvDatabaseProperties the csv database properties to construct the sql query
     * @param outputGlobalTbl       the table where the output is going to be printed
     * @param defaultDBFileName     the name of the local db that the SQL scripts are going to use
     * @param algorithmParameters   the parameters of the algorithm provided
     * @return an ExaDFL script that Exareme will use to run the query
     */
    private static String composeLocalGlobalAlgorithmsDFLScript(
            String algorithmName,
            String inputLocalDB,
            String dbQuery,
            ArrayList<Pair<String, String>> csvDatabaseProperties,
            String outputGlobalTbl,
            String defaultDBFileName,
            ParameterProperties[] algorithmParameters
    ) {
        StringBuilder dflScript = new StringBuilder();

        String localScriptPath = ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/local.template.sql";
        String globalScriptPath = ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/global.template.sql";
        String algorithmFolderPath = ComposerConstants.getAlgorithmFolderPath(algorithmName);

        // Format local
        dflScript.append("distributed create temporary table input_global_tbl to 1 as virtual \n");
        dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.dbQueryKey, dbQuery));
        for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
            dflScript.append(String.format("'%s:%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
        }

        dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
        dflScript.append(");\n");

        // Format global
        dflScript.append(String
                .format("\nusing input_global_tbl \ndistributed create table %s as direct \n",
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
     * @param algorithmName         the name of the algorithm
     * @param inputLocalDB          the location of the local database
     * @param dbQuery               the query to execute on the database
     * @param csvDatabaseProperties the csv database properties to construct the sql query
     * @param outputGlobalTbl       the table where the output is going to be printed
     * @param defaultDBFileName     the name of the local db that the SQL scripts are going to use
     * @param algorithmParameters   the parameters of the algorithm provided
     * @return an ExaDFL script that Exareme will use to run the query
     */
    private static String composeMultipleLocalGlobalAlgorithmsDFLScript(
            String algorithmName,
            String inputLocalDB,
            String dbQuery,
            ArrayList<Pair<String, String>> csvDatabaseProperties,
            String outputGlobalTbl,
            String defaultDBFileName,
            ParameterProperties[] algorithmParameters
    ) {
        StringBuilder dflScript = new StringBuilder();

        String algorithmFolderPath = ComposerConstants.getAlgorithmFolderPath(algorithmName);
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
            String tempOutputGlobalTbl = "output_global_tbl_" + iteration;
            String prevOutputGlobalTbl = "output_global_tbl_" + (iteration - 1);
            String currentIterationAlgorithmFolderPath = algorithmFolderPath + "/" + listFiles[iteration - 1].getName();
            String localScriptPath = currentIterationAlgorithmFolderPath + "/local.template.sql";
            String globalScriptPath = currentIterationAlgorithmFolderPath + "/global.template.sql";

            // Format local
            if (iteration > 1)
                dflScript.append(String.format("using %s\n", prevOutputGlobalTbl));
            dflScript.append(String
                    .format("distributed create temporary table %s to 1 as virtual \n", inputGlobalTbl));
            dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ",
                    currentIterationAlgorithmFolderPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.dbQueryKey, dbQuery));
            for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
                dflScript.append(String.format("'%s:%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
            }
            if (iteration > 1)
                dflScript.append(String.format("'%s:%s' ", ComposerConstants.prevOutputGlobalTblKey, prevOutputGlobalTbl));
            dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
            dflScript.append(");\n");

            // Format global
            if (iteration != listFiles.length) {
                dflScript.append(String.format(
                        "\nusing %s \ndistributed create temporary table %s as direct \n",
                        inputGlobalTbl, tempOutputGlobalTbl));
            } else {
                dflScript.append(String
                        .format("\nusing %s \ndistributed create table %s as direct \n",
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
     * @param algorithmName         the name of the algorithm
     * @param inputLocalDB          the location of the local database
     * @param dbQuery               the query to execute on the database
     * @param csvDatabaseProperties the csv database properties to construct the sql query
     * @param outputGlobalTbl       the table where the output is going to be printed
     * @param defaultDBFileName     the name of the local db that the SQL scripts are going to use
     * @param algorithmParameters   the parameters of the algorithm provided
     * @param numberOfWorkers       the number of workers that the algorithm is going to run on
     * @return an ExaDFL script that Exareme will use to run the query
     */
    private static String composePipelineAlgorithmsDFLScript(
            String algorithmName,
            String inputLocalDB,
            String dbQuery,
            ArrayList<Pair<String, String>> csvDatabaseProperties,
            String outputGlobalTbl,
            String defaultDBFileName,
            ParameterProperties[] algorithmParameters,
            int numberOfWorkers
    ) {
        StringBuilder dflScript = new StringBuilder();
        String localScriptPath = ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/local.template.sql";
        String localUpdateScriptPath = ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/localupdate.template.sql";
        String globalScriptPath = ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/global.template.sql";
        String algorithmFolderPath = ComposerConstants.getAlgorithmFolderPath(algorithmName);
        String outputLocalTbl = "output_local_tbl_" + 0;
        String prevOutputLocalTbl;

        dflScript.append(String.format(
                "distributed create temporary table %s as direct \n", outputLocalTbl));
        dflScript.append(String
                .format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.dbQueryKey, dbQuery));
        for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
            dflScript.append(String.format("'%s:%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.outputGlobalTblKey, outputGlobalTbl));
        dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
        dflScript.append(");\n");

        for (int iteration = 1; iteration < numberOfWorkers; iteration++) {
            outputLocalTbl = "output_local_tbl_" + iteration;
            prevOutputLocalTbl = "output_local_tbl_" + (iteration - 1);

            dflScript.append(String.format("using %s distributed create temporary table %s as direct \n",
                    prevOutputLocalTbl, outputLocalTbl));
            dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.dbQueryKey, dbQuery));
            for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
                dflScript.append(String.format("'%s:%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.outputGlobalTblKey, outputGlobalTbl));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.prevOutputLocalTblKey, prevOutputLocalTbl));
            dflScript.append(String.format("\n  select filetext('%s')\n", localUpdateScriptPath));
            dflScript.append(");\n");
        }

        prevOutputLocalTbl = "output_local_tbl_" + (numberOfWorkers - 1);
        dflScript.append(String.format("using output_local_tbl_%d distributed create table %s as direct ",
                (numberOfWorkers - 1), outputGlobalTbl));
        dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
        }
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
        dflScript.append(String.format("'%s:%s' ", ComposerConstants.dbQueryKey, dbQuery));
        for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
            dflScript.append(String.format("'%s:%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
        }
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
    public static String composeIterativeAlgorithmsDFLScript(
            String algorithmKey,
            AlgorithmProperties algorithmProperties,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase
    ) throws ComposerException {
        if (iterativeAlgorithmPhase == null)
            throw new ComposerException("Unsupported iterative algorithm phase.");

        String dbIdentifier = algorithmProperties.getParameterValue(ComposerConstants.dbIdentifierKey);
        if (dbIdentifier == null)
            dbIdentifier = algorithmKey;
        String algorithmName = algorithmProperties.getName();
        String defaultDBFileName = HBPConstants.DEMO_DB_WORKING_DIRECTORY + dbIdentifier + "_defaultDB.db";
        String inputLocalDB = getDataPath(algorithmProperties);
        String dbQuery = createLocalTableQuery(algorithmProperties);
        ArrayList<Pair<String, String>> csvDatabaseProperties = getCSVDatabaseProperties();
        ParameterProperties[] algorithmParameters = algorithmProperties.getParameters();

        StringBuilder dflScript = new StringBuilder();

        String algorithmFolderPath =
                generateIterativeWorkingDirectoryString(algorithmName, iterativeAlgorithmPhase);
        String outputGlobalTbl = IterationsHandlerDFLUtils.generateIterativePhaseOutputTblName(
                algorithmKey, iterativeAlgorithmPhase);
        String iterationsDBName = generateIterationsDBName(algorithmKey);

        if (iterativeAlgorithmPhase.equals(termination_condition)) {
            // Format termination condition script.
            dflScript.append(String.format("distributed create table %s as direct \n", outputGlobalTbl));
            dflScript.append(
                    String.format("select * from (\n  execnselect 'path:%s' ", algorithmFolderPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.dbQueryKey, dbQuery));
            for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
                dflScript.append(String.format("'%s:%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
            }
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
            String tempOutputGlobalTbl = "output_global_tbl_" + iteration;
            String prevOutputGlobalTbl = "output_global_tbl_" + (iteration - 1);
            String localSQLScriptsPath =
                    getIterativeAlgorithmFolderPath(algorithmName, iterativeAlgorithmPhase, iteration);
            String localScriptPath = localSQLScriptsPath + "/local.template.sql";

            // Global template SQL scripts should be retrieved from the demo directory because they are modified
            String globalSQLScriptsPath = algorithmFolderPath + "/" + iteration;
            String globalScriptPath = globalSQLScriptsPath + "/global.template.sql";

            // format local
            if (iteration > 1)
                dflScript.append(String.format("using %s\n", prevOutputGlobalTbl));
            dflScript.append(String.format("distributed create temporary table %s to 1 as virtual \n", inputGlobalTbl));
            dflScript.append(String.format("select * from (\n  execnselect 'path:%s' ", localSQLScriptsPath));
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'%s:%s' ", parameter.getName(), parameter.getValue()));
            }
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.defaultDBKey, defaultDBFileName));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
            dflScript.append(String.format("'%s:%s' ", ComposerConstants.dbQueryKey, dbQuery));
            for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
                dflScript.append(String.format("'%s:%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
            }
            if (iteration > 1)
                dflScript.append(String.format("'%s:%s' ", ComposerConstants.prevOutputGlobalTblKey, prevOutputGlobalTbl));
            dflScript.append(String.format("\n  select filetext('%s')\n", localScriptPath));
            dflScript.append(");\n");

            // format global
            if (iteration != listFiles.length) {
                dflScript.append(String.format(
                        "\nusing %s \ndistributed create temporary table %s as direct \n",
                        inputGlobalTbl, tempOutputGlobalTbl));
            } else {
                dflScript.append(String
                        .format("\nusing %s \ndistributed create table %s as direct \n",
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

    /**
     * Returns an exaDFL script for the algorithms of type python_local
     *
     * @param algorithmName         the name of the algorithm
     * @param inputLocalDB          the location of the local database
     * @param dbQuery               the query to execute on the database
     * @param csvDatabaseProperties the csv database properties to construct the sql query
     * @param outputGlobalTbl       the name of the output table
     * @param algorithmParameters   the parameters of the algorithm
     * @return
     */
    private static String composePythonLocalAlgorithmsDFLScript(
            String algorithmName,
            String inputLocalDB,
            String dbQuery,
            ArrayList<Pair<String, String>> csvDatabaseProperties,
            String outputGlobalTbl,
            ParameterProperties[] algorithmParameters
    ) {
        StringBuilder dflScript = new StringBuilder();
        String localPythonScriptPath = ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/local.py";

        // Format local
        dflScript.append("distributed create table " + outputGlobalTbl + " as direct \n");
        dflScript.append("select * from (\n  call_python_script '" + localPythonScriptPath + "' ");
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'-%s' '%s' ", parameter.getName(), parameter.getValue().replace("'", "\\'")));
        }
        dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
        dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.dbQueryKey, dbQuery));
        for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
            dflScript.append(String.format("'-%s' '%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
        }
        dflScript.append("\n);\n");

        return dflScript.toString();
    }

    /**
     * Returns an exaDFL script for the algorithms of type python_local_global
     *
     * @param algorithmName         the name of the algorithm
     * @param algorithmKey          the key of the specific algorithm
     * @param inputLocalDB          the location of the local database
     * @param dbQuery               the query to execute on the database
     * @param csvDatabaseProperties the csv database properties to construct the sql query
     * @param outputGlobalTbl       the name of the output table
     * @param algorithmParameters   the parameters of the algorithm
     * @return
     */
    private static String composePythonLocalGlobalAlgorithmsDFLScript(
            String algorithmName,
            String algorithmKey,
            String inputLocalDB,
            String dbQuery,
            ArrayList<Pair<String, String>> csvDatabaseProperties,
            String outputGlobalTbl,
            ParameterProperties[] algorithmParameters
    ) {
        StringBuilder dflScript = new StringBuilder();
        String localPythonScriptPath = ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/local.py";
        String globalPythonScriptPath = ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/global.py";
        String transferDBFilePath = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/transfer.db";

        // Format local
        dflScript.append("distributed create temporary table input_global_tbl to 1 as virtual \n");
        dflScript.append("select * from (\n  call_python_script '" + localPythonScriptPath + "' ");
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'-%s' '%s' ", parameter.getName(), parameter.getValue().replace("\"", "\\\"")));
        }
        dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
        dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.dbQueryKey, dbQuery));
        for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
            dflScript.append(String.format("'-%s' '%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
        }
        dflScript.append("\n);\n");

        // Format global
        dflScript.append(String
                .format("\nusing input_global_tbl \ndistributed create table %s as direct \n",
                        outputGlobalTbl));
        dflScript.append("select * from (\n  call_python_script '" + globalPythonScriptPath + "' ");
        for (ParameterProperties parameter : algorithmParameters) {
            dflScript.append(String.format("'-%s' '%s' ", parameter.getName(), parameter.getValue().replace("\"", "\\\"")));
        }
        dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.localDBsKey, transferDBFilePath));
        dflScript.append(
                String.format("select * from (output '%s' select * from input_global_tbl)\n);\n", transferDBFilePath));

        return dflScript.toString();
    }

    /**
     * Returns an exaDFL script for the algorithms of type python_multiple_local_global
     *
     * @param algorithmName         the name of the algorithm
     * @param algorithmKey          the key of the specific algorithm
     * @param inputLocalDB          the location of the local database
     * @param dbQuery               the query to execute on the database
     * @param csvDatabaseProperties the csv database properties to construct the sql query
     * @param outputGlobalTbl       the name of the output table
     * @param algorithmParameters   the parameters of the algorithm
     * @return
     */
    private static String composePythonMultipleLocalGlobalAlgorithmsDFLScript(
            String algorithmName,
            String algorithmKey,
            String inputLocalDB,
            String dbQuery,
            ArrayList<Pair<String, String>> csvDatabaseProperties,
            String outputGlobalTbl,
            ParameterProperties[] algorithmParameters
    ) {
        StringBuilder dflScript = new StringBuilder();

        String algorithmFolderPath = ComposerConstants.getAlgorithmFolderPath(algorithmName);
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
            String prevOutputGlobalTbl = "output_global_tbl_" + (iteration - 1);
            String tempOutputGlobalTbl = "output_global_tbl_" + iteration;
            String currentIterationAlgorithmFolderPath = algorithmFolderPath + "/" + listFiles[iteration - 1].getName();
            String localScriptPath = currentIterationAlgorithmFolderPath + "/local.py";
            String globalScriptPath = currentIterationAlgorithmFolderPath + "/global.py";
            String localTransferDBFilePath = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                    + "/" + (iteration - 1) + "/local/transfer.db";
            String globalTransferDBFilePath = HBPConstants.DEMO_DB_WORKING_DIRECTORY
                    + algorithmKey + "/" + iteration + "/global/transfer.db";
            String localStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                    + "/" + iteration + "/local_state.pkl";
            String prevLocalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                    + "/" + (iteration - 1) + "/local_state.pkl";
            String globalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                    + "/" + iteration + "/global_state.pkl";
            String prevGlobalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                    + "/" + (iteration - 1) + "/global_state.pkl";

            // Format local
            if (iteration > 1)
                dflScript.append(String.format("using %s\n", prevOutputGlobalTbl));
            dflScript.append(String
                    .format("distributed create temporary table %s to 1 as virtual \n", inputGlobalTbl));
            dflScript.append("select * from (\n  call_python_script '" + localScriptPath + "' ");
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'-%s' '%s' ", parameter.getName(), parameter.getValue().replace("\"", "\\\"")));
            }
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.dbQueryKey, dbQuery));
            for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
                dflScript.append(String.format("'-%s' '%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
            }
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.curStatePKLKey, localStatePKLFile));
            if (iteration > 1) {
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.prevStatePKLKey, prevLocalStatePKLFile));
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.globalDBKey, localTransferDBFilePath));
                dflScript.append(String.format("select * from (output '%s' select * from %s)\n);\n",
                        localTransferDBFilePath, prevOutputGlobalTbl));
            } else
                dflScript.append("\n);\n");

            // Format global
            if (iteration != listFiles.length) {
                dflScript.append(String.format(
                        "\nusing %s \ndistributed create temporary table %s as direct \n",
                        inputGlobalTbl, tempOutputGlobalTbl));
            } else {
                dflScript.append(String
                        .format("\nusing %s \ndistributed create table %s as direct \n",
                                inputGlobalTbl, outputGlobalTbl));
            }

            dflScript.append("select * from (\n  call_python_script '" + globalScriptPath + "' ");
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'-%s' '%s' ", parameter.getName(), parameter.getValue().replace("\"", "\\\"")));
            }
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.localDBsKey, globalTransferDBFilePath));
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.curStatePKLKey, globalStatePKLFile));
            if (iteration > 1) {
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.prevStatePKLKey, prevGlobalStatePKLFile));
            }
            dflScript.append(String.format("select * from (output '%s' select * from %s)\n);\n",
                    globalTransferDBFilePath, inputGlobalTbl));
        }
        return dflScript.toString();

    }

    /**
     * Returns an exaDFL script for the algorithms of type python_iterative
     *
     * @param algorithmKey            the key of the specific algorithm
     * @param algorithmProperties     the properties of the algorithm
     * @param iterativeAlgorithmPhase the phase of the iteration
     * @return
     */
    public static String composePythonIterativeAlgorithmsDFLScript(
            String algorithmKey,
            AlgorithmProperties algorithmProperties,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase
    ) throws ComposerException {

        if (iterativeAlgorithmPhase == null)
            throw new ComposerException("Unsupported iterative algorithm phase.");

        String algorithmName = algorithmProperties.getName();
        String inputLocalDB = getDataPath(algorithmProperties);
        String dbQuery = createLocalTableQuery(algorithmProperties);
        // Escaping double quotes for python algorithms because they are needed elsewhere
        dbQuery = dbQuery.replace("\"", "\\\"");
        ArrayList<Pair<String, String>> csvDatabaseProperties = getCSVDatabaseProperties();
        ParameterProperties[] algorithmParameters = algorithmProperties.getParameters();
        String algorithmFolderPath = generateIterativeWorkingDirectoryString(
                algorithmName, iterativeAlgorithmPhase);
        String outputGlobalTbl = IterationsHandlerDFLUtils.generateIterativePhaseOutputTblName(
                algorithmKey, iterativeAlgorithmPhase);
        StringBuilder dflScript = new StringBuilder();

        if (iterativeAlgorithmPhase.equals(termination_condition)) {
            String globalScriptPath = algorithmFolderPath + "/global.py";
            String prevGlobalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/phase_global_state.pkl";

            dflScript.append("distributed create table " + outputGlobalTbl + " as direct \n");
            dflScript.append("select * from (\n  call_python_script '" + globalScriptPath + "' ");
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'-%s' '%s' ", parameter.getName(), parameter.getValue().replace("\"", "\\\"")));
            }
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.prevStatePKLKey, prevGlobalStatePKLFile));
            dflScript.append("\n);\n");

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

        // Iterating through all the local_global folders of the algorithm
        for (int iteration = 1; iteration <= listFiles.length; iteration++) {
            String inputGlobalTbl = "input_global_tbl_" + iteration;
            String tempOutputGlobalTbl = "output_global_tbl_" + iteration;
            String prevOutputGlobalTbl = "output_global_tbl_" + (iteration - 1);
            String currentIterationAlgorithmFolderPath =
                    getIterativeAlgorithmFolderPath(algorithmName, iterativeAlgorithmPhase, iteration);
            String localScriptPath = currentIterationAlgorithmFolderPath + "/local.py";
            String globalScriptPath = currentIterationAlgorithmFolderPath + "/global.py";

            // Initializing prev and cur states between phases and iterations
            String prevLocalStatePKLFile;
            String prevGlobalStatePKLFile;
            String localStatePKLFile;
            String globalStatePKLFile;
            if (iteration == 1 && iteration == listFiles.length) {
                prevLocalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/phase_local_state.pkl";
                prevGlobalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/phase_global_state.pkl";
                localStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/phase_local_state.pkl";
                globalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/phase_global_state.pkl";
            } else if (iteration == 1) {
                prevLocalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/phase_local_state.pkl";
                prevGlobalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/phase_global_state.pkl";
                localStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                        + "/" + iterativeAlgorithmPhase.name() + "/" + iteration + "/local_state.pkl";
                globalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                        + "/" + iterativeAlgorithmPhase.name() + "/" + iteration + "/global_state.pkl";
            } else if (iteration == listFiles.length) {
                prevLocalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                        + "/" + iterativeAlgorithmPhase.name() + "/" + (iteration - 1) + "/local_state.pkl";
                prevGlobalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                        + "/" + iterativeAlgorithmPhase.name() + "/" + (iteration - 1) + "/global_state.pkl";
                localStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/phase_local_state.pkl";
                globalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey + "/phase_global_state.pkl";
            } else {
                prevLocalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                        + "/" + iterativeAlgorithmPhase.name() + "/" + (iteration - 1) + "/local_state.pkl";
                prevGlobalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                        + "/" + iterativeAlgorithmPhase.name() + "/" + (iteration - 1) + "/global_state.pkl";
                localStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                        + "/" + iterativeAlgorithmPhase.name() + "/" + iteration + "/local_state.pkl";
                globalStatePKLFile = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                        + "/" + iterativeAlgorithmPhase.name() + "/" + iteration + "/global_state.pkl";
            }
            String localTransferDBFilePath = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                    + "/" + (iteration - 1) + "/local/transfer.db";
            String globalTransferDBFilePath = HBPConstants.DEMO_DB_WORKING_DIRECTORY
                    + algorithmKey + "/" + iteration + "/global/transfer.db";
            String phaseChangeTransferDBFilePath = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey
                    + "/phase_change/transfer.db";

            // Format local
            if (iteration > 1)
                dflScript.append(String.format("using %s\n", prevOutputGlobalTbl));
            dflScript.append(String
                    .format("distributed create temporary table %s to 1 as virtual \n", inputGlobalTbl));
            dflScript.append("select * from (\n  call_python_script '" + localScriptPath + "' ");
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'-%s' '%s' ", parameter.getName(), parameter.getValue().replace("\"", "\\\"")));
            }
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.inputLocalDBKey, inputLocalDB));
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.dbQueryKey, dbQuery));
            for (Pair<String, String> csvDatabaseProperty : csvDatabaseProperties) {
                dflScript.append(String.format("'-%s' '%s' ", csvDatabaseProperty.getA(), csvDatabaseProperty.getB()));
            }
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.curStatePKLKey, localStatePKLFile));
            if (iteration > 1) {
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.prevStatePKLKey, prevLocalStatePKLFile));
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.globalDBKey, localTransferDBFilePath));
                dflScript.append(String.format("select * from (output '%s' select * from %s)\n);\n",
                        localTransferDBFilePath, prevOutputGlobalTbl));
            } else if (!iterativeAlgorithmPhase.equals(init)) {
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.prevStatePKLKey, prevLocalStatePKLFile));
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.globalDBKey, phaseChangeTransferDBFilePath));
                dflScript.append("\n);\n");
            } else {
                dflScript.append("\n);\n");
            }

            // Format global
            // If this is the last iteration of finalize, print the result from global.py
            // and don't create virtual last table with phase
            if ((iteration == listFiles.length) && iterativeAlgorithmPhase.equals(finalize)) {
                dflScript.append(String.format("\nusing %s \ndistributed create table %s as direct \n",
                        inputGlobalTbl, outputGlobalTbl));

                dflScript.append("select * from (\n  call_python_script '" + globalScriptPath + "' ");
                for (ParameterProperties parameter : algorithmParameters) {
                    dflScript.append(String.format("'-%s' '%s' ", parameter.getName(), parameter.getValue().replace("\"", "\\\"")));
                }
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.localDBsKey, globalTransferDBFilePath));
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.curStatePKLKey, globalStatePKLFile));
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.prevStatePKLKey, prevGlobalStatePKLFile));
                dflScript.append(
                        String.format("select * from (output '%s' select * from %s)\n);\n",
                                globalTransferDBFilePath, inputGlobalTbl));
                return dflScript.toString();
            }

            dflScript.append(String.format("\nusing %s \ndistributed create temporary table %s as direct \n",
                    inputGlobalTbl, tempOutputGlobalTbl));

            dflScript.append("select * from (\n  call_python_script '" + globalScriptPath + "' ");
            for (ParameterProperties parameter : algorithmParameters) {
                dflScript.append(String.format("'-%s' '%s' ", parameter.getName(), parameter.getValue().replace("\"", "\\\"")));
            }
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.localDBsKey, globalTransferDBFilePath));
            dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.curStatePKLKey, globalStatePKLFile));
            if (iteration > 1 || !iterativeAlgorithmPhase.equals(init)) {
                dflScript.append(String.format("'-%s' '%s' ", ComposerConstants.prevStatePKLKey, prevGlobalStatePKLFile));
            }
            dflScript.append(
                    String.format("select * from (output '%s' select * from %s)\n);\n", globalTransferDBFilePath, inputGlobalTbl));

            if (iteration == listFiles.length) {
                dflScript.append(String
                        .format("\nusing %s \ndistributed create table %s as virtual \n",
                                tempOutputGlobalTbl, outputGlobalTbl));
                dflScript.append(
                        String.format("select * from (\n  output '%s' select * from %s\n);\n",
                                phaseChangeTransferDBFilePath, tempOutputGlobalTbl));
            }
        }

        return dflScript.toString();

    }
    // Utilities --------------------------------------------------------------------------------

    /**
     * This function is used to gather and return all the properties related to the csv database
     * like table and column names.
     *
     * @return a list of pairs containing parameter names and parameter values to be passed in the algorithms
     */
    private static ArrayList<Pair<String, String>> getCSVDatabaseProperties() {
        GenericProperties properties = AdpProperties.getGatewayProperties();

        ArrayList<Pair<String, String>> csvDBProperties = new ArrayList<Pair<String, String>>();
        csvDBProperties.add(new Pair<>(ComposerConstants.csvDBTableDataKey, properties.getString("csvDatabase.table.data")));
        csvDBProperties.add(new Pair<>(ComposerConstants.csvDBTableMetadataKey, properties.getString("csvDatabase.table.metadata")));
        csvDBProperties.add(new Pair<>(ComposerConstants.csvDBTableMetadataColumnCodeKey, properties.getString("csvDatabase.table.metadata.column.code")));
        csvDBProperties.add(new Pair<>(ComposerConstants.csvDBTableMetadataColumnLabelKey, properties.getString("csvDatabase.table.metadata.column.label")));
        csvDBProperties.add(new Pair<>(ComposerConstants.csvDBTableMetadataColumnSqlTypeKey, properties.getString("csvDatabase.table.metadata.column.sql_type")));
        csvDBProperties.add(new Pair<>(ComposerConstants.csvDBTableMetadataColumnIsCategoricalKey, properties.getString("csvDatabase.table.metadata.column.isCategorical")));
        csvDBProperties.add(new Pair<>(ComposerConstants.csvDBTableMetadataColumnEnumerationsKey, properties.getString("csvDatabase.table.metadata.column.enumerations")));
        csvDBProperties.add(new Pair<>(ComposerConstants.csvDBTableMetadataColumnMinValueKey, properties.getString("csvDatabase.table.metadata.column.minValue")));
        csvDBProperties.add(new Pair<>(ComposerConstants.csvDBTableMetadataColumnMaxValueKey, properties.getString("csvDatabase.table.metadata.column.maxValue")));

        return csvDBProperties;
    }

    /**
     * Returns the path of the datasets.db file depending on the pathology.
     * If the algorithm has no pathology defined then the folder with all the datasets is returned.
     *
     * @param algorithmProperties the properties of the algorithm
     * @return the path where the data are
     */
    private static String getDataPath(AlgorithmProperties algorithmProperties) {
        String dataPath = ComposerConstants.getDataPath();
        String pathology = algorithmProperties.getParameterValue(ComposerConstants.getPathologyPropertyName());

        if (pathology == null)
            return Paths.get(dataPath).toString();

        String datasetsDBName = ComposerConstants.getDatasetsDBName();
        return Paths.get(dataPath, pathology, datasetsDBName).toString();
    }

    /**
     * Provides the folder paths for the iterative algorithms' phases.
     * If the iterativeAlgorithmPhase is null it returns the directory of the algorithms
     *
     * @param algorithmIdentifier     is the identifier of the algorithm (key or name)
     * @param iterativeAlgorithmPhase the phase of the iterative algorithm
     * @return the directory where the iterative algorithm's sql scripts are
     * @throws ComposerException if the iterativeAlgorithmPhase is not proper
     */
    private static String generateIterativeWorkingDirectoryString(
            String algorithmIdentifier,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase
    ) throws ComposerException {

        String algorithmsFolderPath = ComposerConstants.getAlgorithmsFolderPath();

        if (iterativeAlgorithmPhase == null)
            return algorithmsFolderPath;

        String algorithmPhaseWorkingDir;
        switch (iterativeAlgorithmPhase) {
            case init:
                algorithmPhaseWorkingDir = algorithmsFolderPath + "/" + algorithmIdentifier + "/" + init.name();
                break;
            case step:
                algorithmPhaseWorkingDir = algorithmsFolderPath + "/" + algorithmIdentifier + "/" + step.name();
                break;
            case termination_condition:
                algorithmPhaseWorkingDir = algorithmsFolderPath + "/" + algorithmIdentifier + "/" + termination_condition.name();
                break;
            case finalize:
                algorithmPhaseWorkingDir = algorithmsFolderPath + "/" + algorithmIdentifier + "/" + finalize.name();
                break;
            default:
                throw new ComposerException("Unsupported iterative algorithm case.");
        }
        return algorithmPhaseWorkingDir;
    }

    private static String getIterativeAlgorithmFolderPath(
            String algorithmName,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativeAlgorithmPhase,
            int iteration) {
        return ComposerConstants.getAlgorithmFolderPath(algorithmName) + "/" + iterativeAlgorithmPhase.name() + "/" + iteration;
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