/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManager;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManagerInterface;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManagerProxy;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.UUID;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiPlanSessionReportManager extends RmiRemoteObject<PlanSessionReportManagerProxy>
    implements PlanSessionReportManager {

    private PlanSessionReportManagerInterface reportManagerInterface = null;
    private EntityName regEntityName = null;

    public RmiPlanSessionReportManager(PlanSessionReportManagerInterface reportManagerInterface,
        EntityName regEntityName) throws RemoteException {
        super(NetUtil.getIPv4() + "_planSessionReportManager_" + UUID.randomUUID().toString());

        this.reportManagerInterface = reportManagerInterface;
        this.regEntityName = regEntityName;

        super.register();
    }

    @Override public PlanSessionReportManagerProxy createProxy() throws RemoteException {
        return new RmiPlanSessionReportManagerProxy(super.getRegEntryName(), regEntityName);
    }

    @Override
    public void planStart(Date time, ContainerID containerID, PlanSessionReportID sessionID)
        throws RemoteException {
        reportManagerInterface.planStart(time, containerID, sessionID);
    }

    @Override public void planInstantiationException(RemoteException exception, Date time,
        ContainerID containerID, PlanSessionReportID sessionID) throws RemoteException {
        reportManagerInterface.planInstantiationException(exception, time, containerID, sessionID);
    }

    @Override public void operatorSuccess(ConcreteOperatorID operatorID, int exidCode,
        Serializable exitMessage, Date time, ContainerID containerID, PlanSessionReportID sessionID,
        boolean terminateGroup) throws RemoteException {
        reportManagerInterface
            .operatorSuccess(operatorID, exidCode, exitMessage, time, containerID, sessionID,
                terminateGroup);
    }

    @Override
    public void operatorError(ConcreteOperatorID operatorID, RemoteException exception, Date time,
        ContainerID containerID, PlanSessionReportID sessionID) throws RemoteException {
        reportManagerInterface.operatorError(operatorID, exception, time, containerID, sessionID);
    }

    @Override public void quantumFinished(int quantumNum, ContainerID containerID,
        PlanSessionReportID sessionID) throws RemoteException {
        reportManagerInterface.quantumFinished(quantumNum, containerID, sessionID);
    }

    @Override public void stopManager() throws RemoteException {
        super.unregister();
    }
}
