package madgik.exareme.master.engine.iterations.handler;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import madgik.exareme.master.engine.iterations.IterationsException;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.file.FileUtil;

import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerConstants.iterationsPropertyMaximumNumber;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
class IterationsHandlerDFLUtils {
    private static final Logger log = Logger.getLogger(IterationsHandlerDFLUtils.class);

    /**
     * Generates the DFL scripts (for all iterative algorithm phases).
     *
     * @param algorithmKey            The algorithm's unique key.
     * @param composer                Composer instance used to generate DFL script for each phase.
     * @param algorithmProperties     The properties of this algorithm.<br> See {@link
     *                                AlgorithmsProperties.AlgorithmProperties}.
     * @param iterativeAlgorithmState State of the iterative algorithm, only used for reading data.
     * @return DFL scripts array (one for each phase)
     * @throws Exception All exceptions that can be thrown from {@link Composer#composeVirtual(String,
     *                   AlgorithmsProperties.AlgorithmProperties, String, IterativeAlgorithmState.IterativeAlgorithmPhasesModel)}
     */
    static String[] prepareDFLScripts(
            String algorithmKey,
            Composer composer,
            AlgorithmsProperties.AlgorithmProperties algorithmProperties,
            IterativeAlgorithmState iterativeAlgorithmState) throws Exception {

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
        // Iterating through each iterative phase and:
        //      1. Apply updates to SQL template files (related to iterations control plane).
        //      2. Generate DFL.
        int dflScriptIdx = 0;
        for (IterativeAlgorithmState.IterativeAlgorithmPhasesModel phase :
                IterativeAlgorithmState.IterativeAlgorithmPhasesModel.values()) {

            // Each update is applied to the latest global template script of a multiple local
            // global structure.
            File sqlTemplateFile;
            if (!phase.equals(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition)) {
                sqlTemplateFile = IterationsHandlerDFLUtils.getLastGlobalFromMultipleLocalGlobal(
                        new File(composer.getRepositoryPath() + algorithmProperties.getName()
                                + "/" + phase.name()));
            } else {
                sqlTemplateFile = new File(composer.getRepositoryPath()
                        + algorithmProperties.getName() + "/"
                        + IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition + "/"
                        + IterationsHandlerConstants.terminationConditionTemplateSQLFilename);
            }

            // 1. Apply updates to SQL template files
            IterationsHandlerDFLUtils.applyTemplateSQLUpdates(iterativeAlgorithmState, phase,
                    sqlTemplateFile, sqlUpdates);

            // 2. Generate DFL
            // Passing as a query key, the algorithm key.

            // Termination condition is a special case of "local", due to the different
            // template sql filename.
            if (phase.equals(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition))
                algorithmProperties.setType(AlgorithmsProperties.AlgorithmProperties.AlgorithmType.iterative);

            dflScripts[dflScriptIdx++] =
                    composer.composeVirtual(algorithmKey, algorithmProperties, null, phase);

            // Restore algorithm type to multiple_local_global.
            if (phase.equals(
                    IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition))
                algorithmProperties.setType(
                        AlgorithmsProperties.AlgorithmProperties.AlgorithmType.multiple_local_global);
        }
        return dflScripts;
    }

    /**
     * Prepares the baseline of SQL updates to be applied on template.sql files.
     * <p>
     * Mainly prepares <code>requireVars</code> and <code>attach database</code>.
     *
     * @return The baseline of SQL Updates to be applied to all template.sql files.
     */
    static ArrayList<Pair<String, IterationsHandlerDFLUtils.SQLUpdateLocation>> prepareBaselineSQLUpdates() {
        // Prepare requireVars for iterationsDB.
        String requireVarsIterationsDB =
                IterationsHandlerDFLUtils.generateRequireVarsString(new String[]{IterationsHandlerConstants.iterationsDBName});
        Pair<String, IterationsHandlerDFLUtils.SQLUpdateLocation> requireVarsIterationsDBUpdate =
                new Pair<>(requireVarsIterationsDB, IterationsHandlerDFLUtils.SQLUpdateLocation.prefix);
        // -------------------------------------------

        ArrayList<Pair<String, IterationsHandlerDFLUtils.SQLUpdateLocation>> sqlUpdates = new ArrayList<>();
        // Updates should be gathered in reverse order, since for prefix, they are applied iteratively
        // prepending each time to the current SQL template.
        sqlUpdates.add(new Pair<>(
                IterationsHandlerConstants.attachIterationsDB,
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
     * @param iterativeAlgorithmState The state object for the current iterative algorithm.
     * @param phase                   The current iterative phase for which the updates are
     *                                applied.
     * @param sqlTemplateFile         The SQL template file that is to be updated each time.
     * @param sqlUpdates              The baseline of SQLUpdates that are to be applied.
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
                        IterationsHandlerConstants.createIterationsCounterTbl,
                        IterationsHandlerDFLUtils.SQLUpdateLocation.suffix));
                sqlUpdates.add(new Pair<>(
                        IterationsHandlerConstants.createIterationsConditionTbl,
                        IterationsHandlerDFLUtils.SQLUpdateLocation.suffix));

                IterationsHandlerDFLUtils.updateSQLTemplate(sqlTemplateFile, sqlUpdates);

                sqlUpdates.remove(sqlUpdates.size() - 1);
                sqlUpdates.remove(sqlUpdates.size() - 1);
                break;

            case step:
                sqlUpdates.add(new Pair<>(
                        IterationsHandlerConstants.incrementIterationsCounter,
                        IterationsHandlerDFLUtils.SQLUpdateLocation.suffix));

                IterationsHandlerDFLUtils.updateSQLTemplate(sqlTemplateFile, sqlUpdates);

                sqlUpdates.remove(sqlUpdates.size() - 1);
                break;

            case termination_condition:
                // Prepare requireVars String for termination condition template SQL.
                String requiredVarsTermCondition = IterationsHandlerDFLUtils.generateRequireVarsString(
                        new String[]{
                                IterationsHandlerConstants.iterationsDBName,
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
                    conditionQuery = IterationsHandlerConstants.checkMaxIterationsCondition;
                else
                    conditionQuery = IterationsHandlerConstants.checkBothConditionTypes;
                sqlUpdates.add(new Pair<>(
                        conditionQuery,
                        IterationsHandlerDFLUtils.SQLUpdateLocation.suffix
                ));

                IterationsHandlerDFLUtils.updateSQLTemplate(sqlTemplateFile, sqlUpdates);

                sqlUpdates.remove(sqlUpdates.size() - 1);
                sqlUpdates.set(1, requireVarsIterationsDBUpdate);
                break;

            case finalize:
                // Nothing to do here ...
                break;

            default:
                throw new IterationsException("Unsupported IterativeAlgorithmPhasesModel phase: \""
                        + phase.name() + "\".");
        }
    }

    /**
     * For proper iterations DFL generation, some scripts need to be updated with specific prefix
     * or suffix.
     * This enumeration defines the site of update in a DFL script.
     */
    enum SQLUpdateLocation {
        prefix, suffix
    }

    /**
     * Updates a template SQL file defined by <code>templateFilename</code> with the given list of
     * updates.
     *
     * <p> An update is defined as a Pair of MadisSQL valid content and a location for the update.
     * Updates are packed into an ArrayList of aforementioned Pairs.
     *
     * @param templateFilename Filename of the template SQL script to be updated, i.e. <b>absolute
     *                         path</b> + filename.
     * @param sqlUpdates       List of updates to be applied.
     * @throws IterationsException If it cannot read from or write to <code>templateFilename</code>.
     */
    private static void updateSQLTemplate(File templateFilename,
                                          ArrayList<Pair<String, SQLUpdateLocation>> sqlUpdates)
            throws IterationsException {

        // Read DFL into a String, apply the updates and then rewrite its content.
        String originalScriptLines;
        try {
            originalScriptLines = FileUtil.readFile(templateFilename);
        } catch (IOException e) {
            throw new IterationsException("Failed to read original DLF script file.", e);
        }

        StringBuilder updatedScriptBuilder;
        if (originalScriptLines != null) {
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
                        updatedScriptBuilder.append("\n").append(update);
                        break;
                    default:
                        throw new IterationsException("Unsupported code site for DFL editing.");
                }
            }

            try {
                FileUtil.writeFile(updatedScriptBuilder.toString(), templateFilename);
            } catch (IOException e) {
                throw new IterationsException("Failed to write updated DFL script file.", e);
            }
        }
    }

    /**
     * Generates requireVars String needed for template SQL files.
     *
     * @param variables The required variables
     * @return Null if variables array is empty, a String otherwise.
     */
    private static String generateRequireVarsString(String[] variables) {
        if (variables.length == 0)
            return null;
        StringBuilder requireVarsBuilder = new StringBuilder(IterationsHandlerConstants.requireVars);
        for (String var : variables) {
            if (var.isEmpty())
                continue;
            requireVarsBuilder.append(" '").append(var).append("'");
        }
        requireVarsBuilder.append(";");
        return requireVarsBuilder.toString();
    }


    /**
     * Finds the last global script in a <code>multiple_local_global</code> directory structure.
     *
     * @param algorithmPhasePath Expects the algorithm path with the AlgorithmPhase name <b>appended
     *                           to it</b>.
     * @return The last global script of the given <code>multiple_local_global</code> directory
     * structure
     */
    private static File getLastGlobalFromMultipleLocalGlobal(File algorithmPhasePath) {
        File[] listFiles = new File(algorithmPhasePath.toString())
                .listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
        if (listFiles != null) {
            Arrays.sort(listFiles);
            File lastMultLocalGlobalDir = listFiles[listFiles.length - 1].getAbsoluteFile();
            return new File(
                    lastMultLocalGlobalDir,
                    IterationsHandlerConstants.globalTemplateSQLFilename);
        } else
            throw new IterationsException("Failed to retrieve last global DFL script.");
    }
}
