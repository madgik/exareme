/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.executionEngine.session.ActiveExecutionPlan;
import madgik.exareme.worker.art.executionEngine.session.ConcreteOperatorStatus;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerInterface;
import madgik.exareme.worker.art.remote.RmiObjectProxy;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author herald
 */
public class DynamicStatusManager extends EventSchedulerManipulator
        implements PlanSessionStatusManagerInterface {
    private static final Logger log = Logger.getLogger(DynamicStatusManager.class);

    public DynamicStatusManager() {
    }

    @Override
    public boolean hasFinished(PlanSessionID sessionID) throws RemoteException {
        PlanEventScheduler eventScheduler = getSchedulerWithId(sessionID);
        return eventScheduler.getState().getPlanSession().getPlanSessionStatus().hasFinished();
    }

    @Override
    public void waitUntilFinish(PlanSessionID sessionID) throws RemoteException {
        PlanEventScheduler eventScheduler = getSchedulerWithId(sessionID);
        try {
            if (eventScheduler.getState().isTerminated()) {
                return;
            }
            /* Register the listener and wait for termination */
            Semaphore wait = new Semaphore(0);
            eventScheduler.getState()
                    .registerTerminationListener(new SemaphoreTerminationListener(wait));
            wait.acquire();
        } catch (Exception e) {
            throw new RemoteException("Cannot wait until finished!", e);
        }
    }

    @Override
    public boolean hasError(PlanSessionID sessionID) throws RemoteException {
        PlanEventScheduler eventScheduler = getSchedulerWithId(sessionID);
        return eventScheduler.getState().getPlanSession().getPlanSessionStatus().hasError();
    }

    @Override
    public List<Exception> getErrorList(PlanSessionID sessionID) throws RemoteException {
        PlanEventScheduler eventScheduler = getSchedulerWithId(sessionID);
        return eventScheduler.getState().getPlanSession().getPlanSessionStatus().getExceptions();
    }

    @Override
    public ActiveExecutionPlan getActiveExecutionPlan(PlanSessionID sessionID)
            throws RemoteException {
        PlanEventScheduler eventScheduler = getSchedulerWithId(sessionID);
        return eventScheduler.getState().getPlanSession().getActiveExecutionPlan();
    }

    @Override
    public ConcreteOperatorStatus getOperatorStatus(String operatorName, PlanSessionID sessionID)
            throws RemoteException {
        PlanEventScheduler eventScheduler = getSchedulerWithId(sessionID);
        return eventScheduler.getState().getPlanSession().getPlanSessionStatus()
                .getStatus(operatorName);
    }
}
