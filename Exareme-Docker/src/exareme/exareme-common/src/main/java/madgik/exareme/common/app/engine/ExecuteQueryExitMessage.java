/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

import madgik.exareme.utils.embedded.db.TableInfo;

import java.io.Serializable;

/**
 * @author herald
 */
public class ExecuteQueryExitMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    public final TableInfo outTableInfo;
    public final ExecutionStatistics execStats;
    public final int queryID;
    public final int serialNumber;
    public final AdpDBOperatorType type;

    public ExecuteQueryExitMessage(TableInfo outTableInfo, ExecutionStatistics execStats,
                                   int queryID, int serialNumber, AdpDBOperatorType type) {
        this.outTableInfo = outTableInfo;
        this.execStats = execStats;
        this.queryID = queryID;
        this.serialNumber = serialNumber;
        this.type = type;
    }
}
