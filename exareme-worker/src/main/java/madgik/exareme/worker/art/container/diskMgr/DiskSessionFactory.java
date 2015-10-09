/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.diskMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;

/**
 * @author herald
 */
public class DiskSessionFactory {

    private DiskSessionFactory() {
    }

    public static DiskSession createInMemorySession(DiskSessionQoS qoS, String sessionName,
        ContainerSessionID containerSessionID, PlanSessionID planSessionID) {
        return null;
    }
}
