/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.statsMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.art.PlanSessionID;

import java.io.Serializable;

/**
 * @author herald
 */
public class StatsMContainerSession implements Serializable {

    private static final long serialVersionUID = 1L;
    private ContainerSessionStatistics statistics = null;

    public StatsMContainerSession(ContainerSessionID containerSessionID, PlanSessionID sessionID,
                                  String containerName) {
        statistics = new ContainerSessionStatistics(containerSessionID, sessionID, containerName);
    }

    public ContainerSessionStatistics getStatistics() {
        return statistics;
    }

    public void destroySession() {
        statistics.destroy();
    }
}
