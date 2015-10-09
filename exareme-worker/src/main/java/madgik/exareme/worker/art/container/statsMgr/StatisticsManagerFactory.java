/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.statsMgr;

import madgik.exareme.worker.art.container.statsMgr.simple.SimpleStatisticsManager;
import madgik.exareme.worker.art.container.statsMgr.sync.SynchronizedStatisticsManager;

/**
 * @author herald
 */
public class StatisticsManagerFactory {

    private StatisticsManagerFactory() {
    }

    public static StatisticsManagerInterface createSimpleManager(String containerName) {
        return new SynchronizedStatisticsManager(new SimpleStatisticsManager(containerName));
    }
}
