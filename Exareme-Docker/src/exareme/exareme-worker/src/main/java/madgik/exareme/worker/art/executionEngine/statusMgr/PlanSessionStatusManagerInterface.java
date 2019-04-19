/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.statusMgr;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.executionEngine.session.ActiveExecutionPlan;
import madgik.exareme.worker.art.executionEngine.session.ConcreteOperatorStatus;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author herald
 */
public interface PlanSessionStatusManagerInterface {

    boolean hasFinished(PlanSessionID sessionID) throws RemoteException;

    void waitUntilFinish(PlanSessionID sessionID) throws RemoteException;

    boolean hasError(PlanSessionID sessionID) throws RemoteException;

    List<Exception> getErrorList(PlanSessionID sessionID) throws RemoteException;

    ActiveExecutionPlan getActiveExecutionPlan(PlanSessionID sessionID) throws RemoteException;

    ConcreteOperatorStatus getOperatorStatus(String operatorName, PlanSessionID sessionID)
            throws RemoteException;
}
