/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.stats;

import madgik.exareme.common.schema.Statistics;

/**
 * Simple table statistics.
 *
 * @author heraldkllapi
 */
public class AdpDBStats {
    private String database = null;
    private Statistics dbStats = null;

    public AdpDBStats(String database) {
        this.database = database;
    }

    public Statistics getStatistics() {
        return dbStats;
    }
}
