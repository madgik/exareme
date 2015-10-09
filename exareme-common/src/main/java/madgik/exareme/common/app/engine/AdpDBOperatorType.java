/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

/**
 * @author herald
 */
public enum AdpDBOperatorType {
    // Table
    tableInput,
    tableUnionReplicator,

    // Query
    runQuery,

    // Data manipulations
    buildIndex,
    dropTable,
    dropIndex
}
