/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class NMContainerSession implements Serializable {
    private static final long serialVersionUID = 1L;
    private ContainerSessionID containerSessionID = null;
    private PlanSessionID sessionID = null;
    private StatisticsManagerInterface statistics = null;

    public NMContainerSession(ContainerSessionID containerSessionID, PlanSessionID sessionID,
        StatisticsManagerInterface statistics) {
        this.containerSessionID = containerSessionID;
        this.sessionID = sessionID;
        this.statistics = statistics;
    }
}
