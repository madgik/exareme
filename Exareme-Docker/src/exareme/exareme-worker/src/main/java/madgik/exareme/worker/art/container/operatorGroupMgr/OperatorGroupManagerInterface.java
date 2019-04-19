package madgik.exareme.worker.art.container.operatorGroupMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;

/**
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */

/*
 * Operator group manager for worker.
 * Keeps track of terminated operators per plan and per group.
 */
public interface OperatorGroupManagerInterface {
    void clear(PlanSessionID planID);

    void clear(PlanSessionID planID, ContainerSessionID csID);

    void setTerminated(PlanSessionID planID, ContainerSessionID csID, String opName);

    int getNumberOfTerminatedOperators(PlanSessionID planID, ContainerSessionID csID);
}
