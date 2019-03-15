package madgik.exareme.master.engine.iterations.handler;

import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.ComposerConstants;
import madgik.exareme.master.queryProcessor.composer.ComposerException;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.file.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static madgik.exareme.common.consts.HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY;
import static madgik.exareme.master.engine.iterations.handler.IterationsConstants.*;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition;

/**
 * Object that is responsible for SQL template file updates (i.e. adding iterations control
 * required SQL statements) and for generating DFL scripts and templates.
 * <p>
 * Templates are used for supporting context-sharing between different iterative algorithm phases
 * such as init, step, etc. (e.g. having the output of the previous step phase piped to the next
 * step phase as input).
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class IterationsHandlerDFLUtils {
    private static final Logger log = Logger.getLogger(IterationsHandlerDFLUtils.class);

    // DFL Generation ---------------------------------------------------------------------------

    /**
     * Generates the DFL scripts (for all iterative algorithm phases).
     *
     * @param demoCurrentAlgorithmDir the algorithm's current execution directory, obtained by
     *                                firstly running {@link IterationsHandlerDFLUtils#copyAlgorithmTemplatesToDemoDirectory}
     * @param algorithmKey            the algorithm's unique key
     * @param composer                the Composer instance used to generate DFL script for each
     *                                phase
     * @param algorithmProperties     the properties of this algorithm
     * @param iterativeAlgorithmState the state of iterative algorithm, only used for reading data
     * @return the generated DFL scripts (one for each phase)
     * @throws IterationsFatalException If {@link Composer#composeVirtual} fails.
     * @see AlgorithmsProperties.AlgorithmProperties
     */
    static String[] prepareDFLScripts(
            String demoCurrentAlgorithmDir,
            String algorithmKey,
            Composer composer,
            AlgorithmsProperties.AlgorithmProperties algorithmProperties,
            IterativeAlgorithmState iterativeAlgorithmState) {

        String[] dflScripts = new String[
                IterativeAlgorithmState.IterativeAlgorithmPhasesModel.values().length];

        // Assuming multiple_local_global format for each iterative phase (except for term. cond.)
        // We're changing the algorithmProperties object type but the original is already saved
        // in the iterativeAlgorithmState.
        algorithmProperties.setType(
                AlgorithmsProperties.AlgorithmProperties.AlgorithmType.multiple_local_global);

        // ------------------------------------------
        // Preparing SQLUpdates baseline.
        ArrayList<Pair<String, IterationsHandlerDFLUtils.SQLUpdateLocation>> sqlUpdates =
                IterationsHandlerDFLUtils.prepareBaselineSQLUpdates();

        // ------------------------------------------
        // Create parameterProperties array with previousPhaseOutputTbl parameter needed for step
        // and finalize iterative phases. This parameter's value is a "variable" in the format
        // ${variable_name}, which will be replaced with StrSubstitutor, during each phase.
        ArrayList<AlgorithmsProperties.ParameterProperties> parameterPropertiesArrayList =
                new ArrayList<>(Arrays.asList(algorithmProperties.getParameters()));

        AlgorithmsProperties.ParameterProperties previousPhaseOutputTblParameter =
                new AlgorithmsProperties.ParameterProperties();
        previousPhaseOutputTblParameter.setName(
                IterationsConstants.previousPhaseOutputTblVariableName);
        previousPhaseOutputTblParameter.setValue(previousPhaseOutputTblPlaceholder);

        parameterPropertiesArrayList.add(previousPhaseOutputTblParameter);
        AlgorithmsProperties.ParameterProperties[] parameterPropertiesInclPreviousPhaseOutputTbl =
                parameterPropertiesArrayList.toArray(
                        new AlgorithmsProperties.ParameterProperties[parameterPropertiesArrayList.size()]);

        // ------------------------------------------
        // Iterating through each iterative phase and:
        //      1. Apply updates to SQL template files (related to iterations control plane).
        //      2. Generate DFL.
        int dflScriptIdx = 0;
        for (IterativeAlgorithmState.IterativeAlgorithmPhasesModel phase :
                IterativeAlgorithmState.IterativeAlgorithmPhasesModel.values()) {

            // Update the output tbl name for each phase, for having a consistent naming scheme
            // for each iterative phase.
            if (!AlgorithmsProperties.AlgorithmProperties.updateParameterProperty(
                    algorithmProperties,
                    ComposerConstants.outputGlobalTblKey,
                    generateIterativePhaseOutputTblName(
                            IterationsConstants.iterationsOutputTblPrefix,
                            algorithmKey,
                            phase)
            )) {
                throw new IterationsFatalException("Failed to set output table name for iterative " +
                        "phase: " + phase.name() + " of algorithm: " + iterativeAlgorithmState.toString());
            }

            // Each update is applied to the latest global template script of a multiple local
            // global structure.
            File sqlTemplateFile;
            if (!phase.equals(termination_condition)) {
                sqlTemplateFile = IterationsHandlerDFLUtils.getLastGlobalFromMultipleLocalGlobal(
                        new File(demoCurrentAlgorithmDir + "/" + phase.name()));
            } else {
                sqlTemplateFile = new File(demoCurrentAlgorithmDir + "/"
                        + termination_condition
                        + "/" + terminationConditionTemplateSQLFilename);
                if (!sqlTemplateFile.exists()) {
                    throw new IterationsFatalException("[" + algorithmKey + "] "
                            + termination_condition.name() + " doesn't exist.");
                }
            }

            // 1. Apply updates to SQL template files
            IterationsHandlerDFLUtils.applyTemplateSQLUpdates(iterativeAlgorithmState, phase,
                    sqlTemplateFile, sqlUpdates);

            // 2. Generate DFL

            // Set algorithmProperties.Parameters to the ones containing previousPhaseOutputTbl
            // parameter (required for execnselect of step/finalize phases)
            AlgorithmsProperties.ParameterProperties parameterPropertiesBackup[] =
                    algorithmProperties.getParameters();
            if (phase.equals(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step) ||
                    phase.equals(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.finalize))
                algorithmProperties.setParameters(parameterPropertiesInclPreviousPhaseOutputTbl);

            // Termination condition is a special case of "local", due to the different
            // template sql filename.
            if (phase.equals(termination_condition))
                algorithmProperties.setType(AlgorithmsProperties.AlgorithmProperties.AlgorithmType.iterative);

            try {
                dflScripts[dflScriptIdx++] =
                        composer.composeVirtual(
                                Paths.get(demoCurrentAlgorithmDir).getParent().toString(),
                                algorithmKey,
                                algorithmProperties, phase);
                log.info("dfl: " + dflScripts[dflScriptIdx - 1]);
            } catch (ComposerException e) {
                throw new IterationsFatalException("Composer failure to generate DFL script for phase: "
                        + phase.name() + ".", e);
            }

            // Restore algorithmProperties.Parameters backup (i.e. parameters *not* containing the
            // previousPhaseOutputTbl parameter)
            if (phase.equals(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step) ||
                    phase.equals(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.finalize))
                algorithmProperties.setParameters(parameterPropertiesBackup);

            // Restore algorithm type to multiple_local_global.
            if (phase.equals(
                    termination_condition))
                algorithmProperties.setType(
                        AlgorithmsProperties.AlgorithmProperties.AlgorithmType.multiple_local_global);
        }

        return dflScripts;
    }

    // SQL Template updates ---------------------------------------------------------------------

    /**
     * Prepares the baseline of SQL updates to be applied on {@code template.sql} files.
     * <p>
     * Mainly prepares {@code requireVars} and {@code attach database}.
     *
     * @return The baseline of SQL Updates to be applied to all template.sql files.
     */
    private static ArrayList<Pair<String, IterationsHandlerDFLUtils.SQLUpdateLocation>>
    prepareBaselineSQLUpdates() {
        // Prepare requireVars for iterationsDB.
        String requireVarsIterationsDB =
                IterationsHandlerDFLUtils.generateRequireVarsString(
                        new String[]{IterationsConstants.iterationsDBName});

        Pair<String, IterationsHandlerDFLUtils.SQLUpdateLocation> requireVarsIterationsDBUpdate =
                new Pair<>(requireVarsIterationsDB, IterationsHandlerDFLUtils.SQLUpdateLocation.prefix);
        // -------------------------------------------

        ArrayList<Pair<String, IterationsHandlerDFLUtils.SQLUpdateLocation>> sqlUpdates = new ArrayList<>();
        // Updates should be gathered in reverse order, since for prefix, they are applied iteratively
        // prepending each time to the current SQL template.
        sqlUpdates.add(new Pair<>(
                IterationsConstants.attachIterationsDB,
                IterationsHandlerDFLUtils.SQLUpdateLocation.prefix));
        sqlUpdates.add(requireVarsIterationsDBUpdate);

        return sqlUpdates;
    }

    /**
     * Applies iterations-control specific updates to template SQL files.
     * <p>
     * Examples of this are queries that are specific to iterations control plane, such as
     * creating table which holds iterations counter, a table that holds whether the iterations
     * should continue.
     *
     * @param iterativeAlgorithmState the state object for the current iterative algorithm.
     * @param phase                   the current iterative phase for which the updates are
     *                                applied.
     * @param sqlTemplateFile         the SQL template file that is to be updated each time.
     * @param sqlUpdates              the baseline of SQLUpdates that are to be applied.
     * @throws UnsupportedOperationException If a phase ({@link IterativeAlgorithmState.IterativeAlgorithmPhasesModel})
     *                                       is not supported.
     */
    private static void applyTemplateSQLUpdates(
            IterativeAlgorithmState iterativeAlgorithmState,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel phase,
            File sqlTemplateFile,
            ArrayList<Pair<String, IterationsHandlerDFLUtils.SQLUpdateLocation>> sqlUpdates) {

        // Update SQL templates with iterations control related SQL
        // keeping the updates baseline intact.
        switch (phase) {
            case init:
                sqlUpdates.add(new Pair<>(
                        IterationsConstants.createIterationsCounterTbl,
                        IterationsHandlerDFLUtils.SQLUpdateLocation.suffix));
                sqlUpdates.add(new Pair<>(
                        IterationsConstants.createIterationsConditionTbl,
                        IterationsHandlerDFLUtils.SQLUpdateLocation.suffix));

                IterationsHandlerDFLUtils.updateSQLTemplate(sqlTemplateFile, sqlUpdates);

                sqlUpdates.remove(sqlUpdates.size() - 1);
                sqlUpdates.remove(sqlUpdates.size() - 1);
                break;

            case step:
                sqlUpdates.add(new Pair<>(
                        IterationsConstants.incrementIterationsCounter,
                        IterationsHandlerDFLUtils.SQLUpdateLocation.suffix));

                IterationsHandlerDFLUtils.updateSQLTemplate(sqlTemplateFile, sqlUpdates);

                sqlUpdates.remove(sqlUpdates.size() - 1);
                break;

            case termination_condition:
                // Ensure consistency among properties and template. That is, if
                // ConditionQueryProvided has been set to true, then there must be an `update` query
                // in the termination condition template file.
                String originalScriptLines;
                try {
                    originalScriptLines = FileUtil.readFile(sqlTemplateFile);
                } catch (IOException e) {
                    throw new IterationsFatalException(
                            "Failed to read original SQL template file.", e);
                }
                if (iterativeAlgorithmState.getConditionQueryProvided() &&
                        !originalScriptLines.contains("update"))
                    throw new IterationsFatalException(
                            "ConditionQueryProvided is set to true " +
                                    "in properties file, but no condition query has been provided.");
                else if (!iterativeAlgorithmState.getConditionQueryProvided() &&
                        originalScriptLines.contains("update"))
                    throw new IterationsFatalException(
                            "ConditionQueryProvided is set to false " +
                                    "in properties file, but a condition query has been provided.");

                // Prepare requireVars String for termination condition template SQL.
                String requiredVarsTermCondition = IterationsHandlerDFLUtils.generateRequireVarsString(
                        new String[]{
                                IterationsConstants.iterationsDBName,
                                iterationsPropertyMaximumNumber
                        }
                );
                Pair<String, IterationsHandlerDFLUtils.SQLUpdateLocation> requireConditionPhaseVars =
                        new Pair<>(requiredVarsTermCondition, IterationsHandlerDFLUtils.SQLUpdateLocation.prefix);

                // Setting index to 1 (and not 0) because **prefix** updates are to be gathered in
                // reverse order.
                final Pair<String, SQLUpdateLocation> requireVarsIterationsDBUpdate = sqlUpdates.remove(1);
                sqlUpdates.add(1, requireConditionPhaseVars);

                // Generate condition query depending on whether an algorithm-specific
                // condition query has been provided.
                String conditionQuery;
                if (!iterativeAlgorithmState.getConditionQueryProvided())
                    conditionQuery = IterationsConstants.checkMaxIterationsCondition;
                else
                    conditionQuery = IterationsConstants.checkBothConditionTypes;
                sqlUpdates.add(new Pair<>(
                        conditionQuery,
                        IterationsHandlerDFLUtils.SQLUpdateLocation.suffix
                ));

                IterationsHandlerDFLUtils.updateSQLTemplate(sqlTemplateFile, sqlUpdates);

                sqlUpdates.remove(sqlUpdates.size() - 1);
                sqlUpdates.set(1, requireVarsIterationsDBUpdate);
                break;

            case finalize:
                IterationsHandlerDFLUtils.updateSQLTemplate(sqlTemplateFile, sqlUpdates);
                break;

            default:
                throw new UnsupportedOperationException("Unsupported " +
                        "IterativeAlgorithmPhasesModel phase: \"" + phase.name() + "\".");
        }
    }

    // SQL Template updates utilities -----------------------------------------------------------

    /**
     * For proper iterations DFL generation, some scripts need to be updated with specific prefix
     * or suffix.
     * This enumeration defines the site of update in a DFL script.
     */
    enum SQLUpdateLocation {
        prefix, suffix
    }

    /**
     * Updates a template SQL file with the given list of updates.
     *
     * <p> An update is defined as a Pair of MadisSQL valid content/query and a location site
     * for the update (see {@link SQLUpdateLocation}. Updates are packed into an ArrayList of
     * aforementioned Pairs.
     *
     * @param templateFilename the filename of the template SQL script to be updated, i.e. <b>
     *                         absolute path</b> + filename
     * @param sqlUpdates       the list of updates to be applied
     * @throws IterationsFatalException If it failed to read original SQL template file or an
     *                                  unsupported {@link SQLUpdateLocation} was used, or it failed
     *                                  to write the update SQL template file.
     */
    private static void updateSQLTemplate(File templateFilename,
                                          ArrayList<Pair<String, SQLUpdateLocation>> sqlUpdates) {

        // Read DFL into a String, apply the updates and then rewrite its content.
        String originalScriptLines;
        try {
            originalScriptLines = FileUtil.readFile(templateFilename);
        } catch (IOException e) {
            throw new IterationsFatalException("Failed to read original SQL template file.", e);
        }

        StringBuilder updatedScriptBuilder;
        updatedScriptBuilder = new StringBuilder();
        updatedScriptBuilder.append(originalScriptLines);

        // Iterate through the updates and apply them one by one.
        String update;
        for (Pair<String, SQLUpdateLocation> p : sqlUpdates) {
            update = p.getA() + "\n";
            switch (p.getB()) {
                case prefix:
                    updatedScriptBuilder.insert(0, update);
                    break;
                case suffix:
                    /*
                    Based on the rule that each template.sql file must have an output, i.e. a select
                    query, we can search for the last select occurrence and prepend our update there.
                     */
                    updatedScriptBuilder.insert(
                            updatedScriptBuilder.lastIndexOf(selectStr) - 1,
                            '\n' + update);
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Unsupported code site for DFL editing.");
            }
        }

        try {
            FileUtil.writeFile(updatedScriptBuilder.toString(), templateFilename);
        } catch (IOException e) {
            throw new IterationsFatalException("Failed to write updated SQL template file.", e);
        }
    }

    /**
     * Generates requireVars String needed for template SQL files.
     *
     * @param variables the required variables, not null or empty
     * @return a String containing the "requireVars" statement.
     * @throws IllegalArgumentException If variables is null or empty.
     */
    private static String generateRequireVarsString(String[] variables) {
        if (variables == null)
            throw new IllegalArgumentException("variables String[] cannot be null");
        if (variables.length == 0)
            throw new IllegalArgumentException("variables String[] cannot be empty");

        StringBuilder requireVarsBuilder = new StringBuilder(IterationsConstants.requireVars);
        for (String var : variables) {
            if (var.isEmpty())
                continue;
            requireVarsBuilder.append(" '").append(var).append("'");
        }
        requireVarsBuilder.append(";");
        return requireVarsBuilder.toString();
    }

    // DFL generation related utilities ---------------------------------------------------------

    /**
     * Retrieves the last global script in a {@code multiple_local_global} directory structure.
     *
     * @param algorithmPhasePath the algorithm path with the AlgorithmPhase name <b>appended
     *                           to it</b>, not null
     * @return the last global script of the given {@code multiple_local_global} directory,
     * or null if the {@code algorithmPhasePath} doesn't contain any directories.
     */
    private static File getLastGlobalFromMultipleLocalGlobal(File algorithmPhasePath) {
        if (algorithmPhasePath == null)
            throw new IterationsFatalException("algorithmPhasePath parameter cannot be null");

        File[] listFiles = new File(algorithmPhasePath.toString())
                .listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });

        if (listFiles != null) {
            if (listFiles.length == 0)
                throw new IterationsFatalException("Directory: ["
                        + algorithmPhasePath.toString() + "] contains no files.");

            Arrays.sort(listFiles);
            File lastMultipleLocalGlobalDir = listFiles[listFiles.length - 1].getAbsoluteFile();
            return new File(
                    lastMultipleLocalGlobalDir,
                    IterationsConstants.globalTemplateSQLFilename);
        } else
            return null;
    }

    /**
     * Generates the output table name of a given iterative phase.
     *
     * @param outputTblPrefix the prefix of the output table
     * @param algorithmKey    the iterative algorithm's key
     * @param iterativePhase  the iterative phase for which the table name is generated
     */
    private static String generateIterativePhaseOutputTblName(
            String outputTblPrefix,
            String algorithmKey,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativePhase) {
        String iterativePhaseOutputTblName =
                outputTblPrefix + "_" + algorithmKey + "_" + iterativePhase.name();
        if (iterativePhase.equals(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step) ||
                iterativePhase.equals(
                        termination_condition))
            return "${" + iterativePhaseOutputTblName + "}";
        else
            return iterativePhaseOutputTblName;
    }


    // Utilities --------------------------------------------------------------------------------

    /**
     * Copies the algorithm's template structure into the demo directory under the algorithm's key.
     *
     * @param algorithmName the algorithm's name (same as in repository)
     * @param algorithmKey  the algorithm's key
     * @return the path containing the copied algorithm directory structure in the demo directory
     * @throws IterationsFatalException if it fails to retrieve algorithm's repository directory
     *                                  name, or if it fails to copy algorithm's template structure
     *                                  to demo directory.
     */
    public static String copyAlgorithmTemplatesToDemoDirectory(String algorithmName,
                                                               String algorithmKey) {
        String algorithmRepoPath;
        try {
            algorithmRepoPath = Composer.generateWorkingDirectoryString(
                    null, algorithmName, null);
        } catch (ComposerException e) {
            throw new IterationsFatalException("Failed to retrieve algorithm's repository " +
                    "directory name.", e);
        }

        String algorithmDemoDestinationDirectory =
                DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey;

        try {
            FileUtils.copyDirectory(new File(algorithmRepoPath),
                    new File(algorithmDemoDestinationDirectory));
        } catch (IOException e) {
            throw new IterationsFatalException("Failed to copy algorithm's template files from ["
                    + algorithmRepoPath + "] to demo directory ["
                    + algorithmDemoDestinationDirectory + "].", e);
        }
        return algorithmDemoDestinationDirectory;
    }


    // Public API -------------------------------------------------------------------------------

    /**
     * Generates the initPhase output table name of the current algorithm
     */
    public static String getInitPhaseOutputTblName(String algorithmKey) {
        return generateIterativePhaseOutputTblName(
                IterationsConstants.iterationsOutputTblPrefix,
                algorithmKey,
                IterativeAlgorithmState.IterativeAlgorithmPhasesModel.init);
    }

    /**
     * Generates the stepPhaseOutputTbl variable name (for later substitution)
     */
    public static String getStepPhaseOutputTblVariableName(String algorithmKey) {
        return IterationsConstants.iterationsOutputTblPrefix + "_" + algorithmKey + "_"
                + IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step.name();
    }

    /**
     * Generates the terminationConditionOutputTbl variable name (for later substitution)
     */
    public static String getTermConditionPhaseOutputTblVariableName(String algorithmKey) {
        return IterationsConstants.iterationsOutputTblPrefix + "_" + algorithmKey + "_"
                + termination_condition.name();
    }

}
