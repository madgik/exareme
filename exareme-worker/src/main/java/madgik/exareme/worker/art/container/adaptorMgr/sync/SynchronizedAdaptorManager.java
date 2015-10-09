/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptorMgr.sync;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.association.Triple;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptor;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptor;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManagerInterface;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author herald
 */
public class SynchronizedAdaptorManager implements AdaptorManagerInterface {

    private final AdaptorManagerInterface manager;

    public SynchronizedAdaptorManager(AdaptorManagerInterface manager) {
        this.manager = manager;
    }

    @Override public AdaptorID addReadAdaptor(CombinedReadAdaptor adaptor,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            return manager.addReadAdaptor(adaptor, containerSessionID, sessionID);
        }
    }

    @Override public AdaptorID addWriteAdaptor(CombinedWriteAdaptor adaptor,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            return manager.addWriteAdaptor(adaptor, containerSessionID, sessionID);
        }
    }

    @Override public CombinedReadAdaptor getReadAdaptor(AdaptorID adaptorID,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            return manager.getReadAdaptor(adaptorID, containerSessionID, sessionID);
        }
    }

    @Override public CombinedWriteAdaptor getWriteAdaptor(AdaptorID adaptorID,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            return manager.getWriteAdaptor(adaptorID, containerSessionID, sessionID);
        }
    }

    @Override
    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroyContainerSession(
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            return manager.destroyContainerSession(containerSessionID, sessionID);
        }
    }

    @Override
    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroySessions(
        PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            return manager.destroySessions(sessionID);
        }
    }

    @Override
    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroyAllSessions()
        throws RemoteException {
        synchronized (manager) {
            return manager.destroyAllSessions();
        }
    }
}
