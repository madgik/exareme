/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.statusMgr;

import madgik.exareme.worker.art.executionEngine.session.ActiveExecutionPlan;
import madgik.exareme.worker.art.executionEngine.session.ConcreteOperatorStatus;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface PlanSessionStatusManagerProxy extends ObjectProxy<PlanSessionStatusManager> {

    boolean hasFinished() throws RemoteException;

    boolean hasError() throws RemoteException;

    List<Exception> getErrorList() throws RemoteException;

    ActiveExecutionPlan getActiveExecutionPlan() throws RemoteException;

    ConcreteOperatorStatus getOperatorStatus(String operatorName) throws RemoteException;
}
