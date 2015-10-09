/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.statusMgr;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.executionEngine.session.ActiveExecutionPlan;
import madgik.exareme.worker.art.executionEngine.session.ConcreteOperatorStatus;
import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface PlanSessionStatusManager extends RemoteObject<PlanSessionStatusManagerProxy> {

    boolean hasFinished(PlanSessionID sessionID) throws RemoteException;

    boolean hasError(PlanSessionID sessionID) throws RemoteException;

    List<Exception> getErrorList(PlanSessionID sessionID) throws RemoteException;

    ActiveExecutionPlan getActiveExecutionPlan(PlanSessionID sessionID) throws RemoteException;

    ConcreteOperatorStatus getOperatorStatus(String operatorName, PlanSessionID sessionID)
        throws RemoteException;

    void stopManager() throws RemoteException;
}
