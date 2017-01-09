package madgik.exareme.master.engine.iterations.handler;

/**
 * Contains iterations handler related constants.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class IterationsHandlerConstants {
    // Iterative algorithm parameters -----------------------------------------------------------
    static final String iterationsDBName = "iterationsDB";
    public static final String iterationsParameterIterDBKey = iterationsDBName;
    // Already defined in ComposerConstants => following same pattern.
    static final String iterationsParameterIterDBValueSuffix = iterationsDBName;

    // Iterative algorithm properties -----------------------------------------------------------
    public static final String iterationsPropertyConditionQueryProvided = "iterations_condition_query_provided";
    public static final String iterationsPropertyMaximumNumber = "iterations_max_number";
    // ------------------------------------------------------------------------------------------

    // Algorithm repository names ---------------------------------------------------------------
    private static final String iterationsRepoTerminationCond = "termination_condition";


    // ------------------------------------------------------------------------------------------
    // Template SQL Related Constants -----------------------------------------------------------

    // SQL Table names --------------------------------------------------------------------------
    // This are to be disclosed to algorithm developers, so as they can use them in their scripts.
    static final String iterationsOutputTblPrefix = "output_query";
    private static final String iterationsCounterTbl = "iterations_counter_tbl";
    private static final String iterationsCounterColName = "iterations_counter";
    private static final String iterationsConditionCheckTbl = "iterations_condition_check_result_tbl";
    private static final String iterationsConditionCheckColName = "iterations_condition_check_result";

    // SQL Scripts / Snippets -------------------------------------------------------------------
    private static final String templateSQLSuffix = ".template.sql";
    static final String globalTemplateSQLFilename = "global" + templateSQLSuffix;
    public static final String terminationConditionTemplateSQLFilename =
            iterationsRepoTerminationCond + templateSQLSuffix;

    // Related to iterations control-plane at DFL level.
    static final String requireVars = "requirevars";
    static final String attachIterationsDB =
            "attach database '%{" + iterationsDBName + "}' as " + iterationsDBName + ";";
    private static final String maxIterationsTemporaryColName = "max_iterations_condition_result";

    /*
        Master node is responsible for iterations execution.
        This snippet must be appended to an init global script.
        Desc.:   Creates the table which holds the iterations counter and init's it to 0.
     */
    static final String createIterationsCounterTbl =
            "drop table if exists " + iterationsDBName + "." + iterationsCounterTbl + ";\n" +
                    "create table " + iterationsDBName + "." + iterationsCounterTbl + " as \n" +
                    "  select 0 as " + iterationsCounterColName + " from range(1);";

    /*
        Master node handles condition checking for iterative algorithms.
        This snippet must be appended to an init global script.
        Desc.:  Creates condition check related tbl, with "should_continue" column that contains 1 if
                iterations must continue and 0 otherwise.
    */
    static final String createIterationsConditionTbl =
            "drop table if exists " + iterationsDBName + "." + iterationsConditionCheckTbl + ";\n" +
                    "create table " + iterationsDBName + "." + iterationsConditionCheckTbl + " as \n" +
                    "  select 1 as " + iterationsConditionCheckColName + " from range(1);\n";

    /*
        Master node, at the end of each step will increment iterations counter.
        This snippet must be appended to the final global step script.
        Desc.: Simply increments by 1 the iterations counter.
    */
    static final String incrementIterationsCounter =
            "update " + iterationsDBName + "." + iterationsCounterTbl + " set "
                    + iterationsCounterColName + " = 1 + " + iterationsCounterColName + ";";


    // Iterative algorithm termination condition related queries --------------------------------
    /*
        Query that asserts the max-iterations condition.
        This will be generated if the 'iterations_condition_query_provided' property is set to false.
    */
    static final String checkMaxIterationsCondition =
            "update " + iterationsDBName + "." + iterationsConditionCheckTbl + " set "
                    + iterationsConditionCheckColName + " = (\n"
                    + "\tselect " + iterationsCounterColName + " < cast(%{" + iterationsPropertyMaximumNumber + "} as decimal)\n"
                    + "\tfrom " + iterationsDBName + "." + iterationsCounterTbl
                    + "\n);";

    /*
        Query that asserts both max-iterations and algorithm-specific conditions.
        This will be generated if the 'iterations_condition_query_provided' property is set to true.
        The algorithm-specific query will have been executed before this one, and thus, the actual algorithm
        condition check result will be used in conjunction with the maximum iterations condition result
        in a bitwise AND operation to determine whether iterations must continue.
     */
    static final String checkBothConditionTypes =
            "update " + iterationsDBName + "." + iterationsConditionCheckTbl + " set "
                    + iterationsConditionCheckColName + " = (\n"
                    + "\tselect " + maxIterationsTemporaryColName + " & " + iterationsConditionCheckColName + "\n"
                    + "\tfrom (\n"
                    + "\t\tselect " + iterationsCounterColName + " < cast(%{" + iterationsPropertyMaximumNumber + "} as decimal)"
                    + " as " + maxIterationsTemporaryColName + "\n"
                    + "\t\tfrom " + iterationsDBName + "." + iterationsCounterTbl
                    + "\n\t), " + iterationsConditionCheckTbl
                    + "\n);";
}
