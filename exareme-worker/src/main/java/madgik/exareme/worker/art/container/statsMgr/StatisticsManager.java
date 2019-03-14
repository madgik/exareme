/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.statsMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.JobExecutor;
import madgik.exareme.worker.art.container.SessionBased;
import madgik.exareme.worker.art.container.Stoppable;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface StatisticsManager extends SessionBased, JobExecutor, Stoppable {

    ContainerSessionStatistics getStatistics(ContainerSessionID containerSessionID,
                                             PlanSessionID sessionID) throws RemoteException;
}
