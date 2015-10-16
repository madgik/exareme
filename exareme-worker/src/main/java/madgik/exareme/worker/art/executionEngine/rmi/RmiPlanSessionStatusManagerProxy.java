/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionEngine.session.ActiveExecutionPlan;
import madgik.exareme.worker.art.executionEngine.session.ConcreteOperatorStatus;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManager;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerProxy;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiPlanSessionStatusManagerProxy extends RmiObjectProxy<PlanSessionStatusManager>
    implements PlanSessionStatusManagerProxy {

    private static final long serialVersionUID = 1L;
    public PlanSessionID sessionID = null;

    public RmiPlanSessionStatusManagerProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
    }

    @Override public boolean hasError() throws RemoteException {
        return super.getRemoteObject().hasError(sessionID);
    }

    @Override public boolean hasFinished() throws RemoteException {
        return super.getRemoteObject().hasFinished(sessionID);
    }

    @Override public ActiveExecutionPlan getActiveExecutionPlan() throws RemoteException {
        return super.getRemoteObject().getActiveExecutionPlan(sessionID);
    }

    @Override public ConcreteOperatorStatus getOperatorStatus(String operatorName)
        throws RemoteException {
        return super.getRemoteObject().getOperatorStatus(operatorName, sessionID);
    }

    @Override public List<Exception> getErrorList() throws RemoteException {
        return super.getRemoteObject().getErrorList(sessionID);
    }
}
