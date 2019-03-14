package madgik.exareme.worker.art.container.diskMgr.sync;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;
import madgik.exareme.worker.art.container.diskMgr.DiskSession;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class SynchronizedDiskManager implements DiskManagerInterface {

    private final DiskManagerInterface diskManager;

    public SynchronizedDiskManager(DiskManagerInterface diskManager) {
        this.diskManager = diskManager;
    }

    @Override
    public DiskSession getGlobalSession(PlanSessionID planSessionID)
            throws RemoteException {
        synchronized (diskManager) {
            return diskManager.getGlobalSession(planSessionID);
        }
    }

    @Override
    public DiskSession getContainerSession(ContainerSessionID containerSessionID,
                                           PlanSessionID planSessionID) throws RemoteException {
        synchronized (diskManager) {
            return diskManager.getContainerSession(containerSessionID, planSessionID);
        }
    }

    @Override
    public DiskSession getOperatorSession(ConcreteOperatorID opID,
                                          ContainerSessionID containerSessionID, PlanSessionID planSessionID) throws RemoteException {
        synchronized (diskManager) {
            return diskManager.getOperatorSession(opID, containerSessionID, planSessionID);
        }
    }

    @Override
    public void destroyContainerSession(ContainerSessionID containerSessionID,
                                        PlanSessionID sessionID) throws RemoteException {
        synchronized (diskManager) {
            diskManager.destroyContainerSession(containerSessionID, sessionID);
        }
    }

    @Override
    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        synchronized (diskManager) {
            diskManager.destroySessions(sessionID);
        }
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
        synchronized (diskManager) {
            diskManager.destroyAllSessions();
        }
    }
}
