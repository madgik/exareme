/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class ExecutionStatistics implements Serializable {

    public final String sqlScript;
    public final String stats;

    public long execTime_ms;
    public double outputSize_MB;

    public ExecutionStatistics(String sqlScript, String stats) {
        this.sqlScript = sqlScript;
        this.stats = stats;
    }
}
