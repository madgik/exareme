package madgik.exareme.worker.art.container.operatorGroupMgr.sync;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.operatorGroupMgr.OperatorGroupManagerInterface;

/**
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */

public class SynchronizedOperatorGroupManager implements OperatorGroupManagerInterface {

    private final OperatorGroupManagerInterface manager;

    public SynchronizedOperatorGroupManager(OperatorGroupManagerInterface manager) {
        this.manager = manager;
    }

    @Override
    public void clear(PlanSessionID planID) {
        synchronized (manager) {
            manager.clear(planID);
        }
    }

    @Override
    public void clear(PlanSessionID planID, ContainerSessionID csID) {
        synchronized (manager) {
            manager.clear(planID, csID);
        }
    }

    @Override
    public void setTerminated(PlanSessionID planID, ContainerSessionID csID, String opName) {
        synchronized (manager) {
            manager.setTerminated(planID, csID, opName);
        }
    }

    @Override
    public int getNumberOfTerminatedOperators(PlanSessionID planID, ContainerSessionID csID) {
        synchronized (manager) {
            return manager.getNumberOfTerminatedOperators(planID, csID);
        }
    }
}
