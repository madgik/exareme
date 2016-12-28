package madgik.exareme.master.engine.iterations.handler;

/**
 * Contains iterations handler related constants.
 *
 * @author  Christos Aslanoglou <br>
 *          caslanoglou@di.uoa.gr <br>
 *          University of Athens / Department of Informatics and Telecommunications.
 */
public class IterationsHandlerConstants {
    /**
     * Models the iterative algorithm phases/directory structure (in algorithms-dev repository).
     * <p>
     * Each enum field represents a phase of the iteration algorithm. Under each directory there
     * must be a multiple_local_global directory structure for each phase, <b>except for</b>
     * <code>termination_condition</code> phase, in which there must be a
     * <code>termination_condition.template.sql</code> file.
     */
    public enum IterativeAlgorithmModel {
        init,
        step,
        termination_condition,
        finalize
    }

    // Iterative algorithm properties -----------------------------------------------------------
    static final String iterationsMaximumNumber = "iterations_max_number";

    // Algorithm repository names ---------------------------------------------------------------
    static final String iterationsRepoTerminationCond = "termination_condition";

    // SQL Table names --------------------------------------------------------------------------
    // This are to be disclosed to algorithm developers, so as they can use them in their scripts.
    static final String iterationsDBName = "iterationsDB";
    private static final String iterationsCounterTbl = "iterations_counter_tbl";
    private static final String iterationsCounterColName = "iterations_counter";
    private static final String iterationsConditionCheckTbl = "iterations_condition_check_result_tbl";
    private static final String iterationsConditionCheckColName = "iterations_condition_check_result";

    // SQL Scripts / Snippets -------------------------------------------------------------------
    static final String templateSQLSuffix = ".template.sql";
    static final String globalTemplateSQLFilename = "global" + templateSQLSuffix;
    // Related to iterations control-plane at DFL level.
    static final String requireVars = "requirevars";
    static final String attachIterationsDB =
            "attach database '%{" + iterationsDBName + "}' as " + iterationsDBName + ";";

    // Master node is responsible for iterations execution.
    // This snippet must be appended to an init global script.
    // Desc.:   Creates the table which holds the iterations counter and init's it to 0.
    static final String createIterationsCounterTbl =
            "drop table if exists " + iterationsDBName + "." + iterationsCounterTbl + ";\n" +
            "create table " + iterationsDBName + "." + iterationsCounterTbl + " AS\n" +
            "  select 0 as " + iterationsCounterColName + " from range(1);";

    // Master node handles condition checking for iterative algorithms.
    // This snippet must be appended to an init global script.
    // Desc.:   Creates condition check related tbl, with "should_continue" column that contains 1 if
    //          iterations must continue and 0 otherwise.
    static final String createIterationsConditionTbl =
            "drop table if exists " + iterationsDBName + "." + iterationsConditionCheckTbl + ";\n" +
            "create table " + iterationsDBName + "." + iterationsConditionCheckTbl + " AS\n" +
            "  select 1 as " + iterationsConditionCheckColName + " from range(1);\n";

    // Master node, at the end of each step will increment iterations counter.
    // This snippet must be appended to the final global step script.
    // Desc.: Simply increments by 1 the iterations counter.
    static final String incrementIterationsCounter =
            "update " + iterationsDBName + "." + iterationsCounterTbl + "set "
                    + iterationsCounterColName + " = 1 + " + iterationsCounterColName + ";";
}
