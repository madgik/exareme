/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr.sync;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.netMgr.NetManagerInterface;
import madgik.exareme.worker.art.container.netMgr.NetSession;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class SynchronizedNetManager implements NetManagerInterface {

    private final NetManagerInterface netManager;

    public SynchronizedNetManager(NetManagerInterface netManager) {
        this.netManager = netManager;
    }

    @Override
    public NetSession getGlobalSession(PlanSessionID planSessionID)
            throws RemoteException {
        synchronized (netManager) {
            return netManager.getGlobalSession(planSessionID);
        }
    }

    @Override
    public NetSession getContainerSession(ContainerSessionID containerSessionID,
                                          PlanSessionID planSessionID) throws RemoteException {
        synchronized (netManager) {
            return netManager.getContainerSession(containerSessionID, planSessionID);
        }
    }

    @Override
    public NetSession getOperatorSession(ConcreteOperatorID opID,
                                         ContainerSessionID containerSessionID, PlanSessionID planSessionID) throws RemoteException {
        synchronized (netManager) {
            return netManager.getOperatorSession(opID, containerSessionID, planSessionID);
        }
    }

    @Override
    public void destroyContainerSession(ContainerSessionID containerSessionID,
                                        PlanSessionID sessionID) throws RemoteException {
        synchronized (netManager) {
            netManager.destroyContainerSession(containerSessionID, sessionID);
        }
    }

    @Override
    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        synchronized (netManager) {
            netManager.destroySessions(sessionID);
        }
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
        synchronized (netManager) {
            netManager.destroyAllSessions();
        }
    }
}
