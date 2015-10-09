/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

import madgik.exareme.utils.embedded.db.TableInfo;

/**
 * @author herald
 */
public class MadisExecutorResult {

    private TableInfo tableInfo = null;
    private ExecutionStatistics execStats = null;

    public MadisExecutorResult() {
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public ExecutionStatistics getExecStats() {
        return execStats;
    }

    public void setExecStats(ExecutionStatistics execStats) {
        this.execStats = execStats;
    }
}
