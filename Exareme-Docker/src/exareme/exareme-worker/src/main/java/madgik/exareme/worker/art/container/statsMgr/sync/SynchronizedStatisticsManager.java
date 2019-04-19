/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.statsMgr.sync;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class SynchronizedStatisticsManager implements StatisticsManagerInterface {

    private final Integer lock = new Integer(0);
    private StatisticsManagerInterface manager = null;

    public SynchronizedStatisticsManager(StatisticsManagerInterface manager) {
        this.manager = manager;
    }

    public ContainerSessionStatistics getStatistics(ContainerSessionID containerSessionID,
                                                    PlanSessionID sessionID) throws RemoteException {
        synchronized (lock) {
            return manager.getStatistics(containerSessionID, sessionID);
        }
    }

    public void destroyContainerSession(ContainerSessionID containerSessionID,
                                        PlanSessionID sessionID) throws RemoteException {
        synchronized (lock) {
            manager.destroyContainerSession(containerSessionID, sessionID);
        }
    }

    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        synchronized (lock) {
            manager.destroySessions(sessionID);
        }
    }

    public void destroyAllSessions() throws RemoteException {
        synchronized (lock) {
            manager.destroyAllSessions();
        }
    }
}
