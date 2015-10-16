/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.statsMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.art.PlanSessionID;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface StatisticsManagerInterface {

    ContainerSessionStatistics getStatistics(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;

    void destroyContainerSession(ContainerSessionID containerSessionID, PlanSessionID sessionID)
        throws RemoteException;

    void destroySessions(PlanSessionID sessionID) throws RemoteException;

    void destroyAllSessions() throws RemoteException;
}
