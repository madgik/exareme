/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.art.executionEngine.session.ActiveExecutionPlan;
import madgik.exareme.worker.art.executionEngine.session.ConcreteOperatorStatus;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManager;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerInterface;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerProxy;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiPlanSessionStatusManager extends RmiRemoteObject<PlanSessionStatusManagerProxy>
        implements PlanSessionStatusManager {

    private PlanSessionStatusManagerInterface statusManagerInterface = null;
    private EntityName regEntityName = null;

    public RmiPlanSessionStatusManager(PlanSessionStatusManagerInterface statusManagerInterface,
                                       EntityName regEntityName) throws RemoteException {
        super(NetUtil.getIPv4() + "_planSessionManager_" + UUID.randomUUID().toString());

        this.statusManagerInterface = statusManagerInterface;
        this.regEntityName = regEntityName;

        super.register();
    }

    public PlanSessionStatusManagerProxy createProxy() throws RemoteException {
        return new RmiPlanSessionStatusManagerProxy(super.getRegEntryName(), regEntityName);
    }

    public boolean hasError(PlanSessionID sessionID) throws RemoteException {
        return statusManagerInterface.hasError(sessionID);
    }

    public boolean hasFinished(PlanSessionID sessionID) throws RemoteException {

        return statusManagerInterface.hasFinished(sessionID);
    }

    public ActiveExecutionPlan getActiveExecutionPlan(PlanSessionID sessionID)
            throws RemoteException {
        return statusManagerInterface.getActiveExecutionPlan(sessionID);
    }

    public ConcreteOperatorStatus getOperatorStatus(String operatorName, PlanSessionID sessionID)
            throws RemoteException {
        return statusManagerInterface.getOperatorStatus(operatorName, sessionID);
    }

    public List<Exception> getErrorList(PlanSessionID sessionID) throws RemoteException {
        return statusManagerInterface.getErrorList(sessionID);
    }

    public void waitUntilFinish(PlanSessionID sessionID) throws RemoteException {
        statusManagerInterface.waitUntilFinish(sessionID);
    }

    public void stopManager() throws RemoteException {
        super.unregister();
    }
}
