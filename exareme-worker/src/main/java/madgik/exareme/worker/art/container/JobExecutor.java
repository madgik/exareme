/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public interface JobExecutor {

    ContainerJobResult prepareJob(ContainerJob job, ContainerSessionID containerSessionID,
                                  PlanSessionID sessionID) throws RemoteException;

    void execJob(ContainerJob job, ContainerSessionID containerSessionID, PlanSessionID sessionID)
            throws RemoteException;

    boolean hasExec(ContainerJob job);
}
