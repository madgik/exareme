/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.rmi.RmiContainer;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ContainerSession implements Serializable {
    private static final Logger log = Logger.getLogger(ContainerSession.class);

    private static final long serialVersionUID = 1L;
    private ContainerSessionID containerSessionID = null;
    private PlanSessionID sessionID = null;
    private ContainerProxy containerProxy = null;

    public ContainerSession(ContainerProxy containerProxy, ContainerSessionID containerSessionID,
                            PlanSessionID sessionID) {
        this.containerProxy = containerProxy;
        this.containerSessionID = containerSessionID;
        this.sessionID = sessionID;
    }

    public PlanSessionID getSessionID() {
        return sessionID;
    }

    public ContainerSessionID getContainerSessionID() {
        return containerSessionID;
    }

    public ContainerJobResults execJobs(ContainerJobs jobs) throws RemoteException {
        jobs.setSession(containerSessionID, sessionID);
        log.info("Executing jobs for session ID: " + sessionID.getLongId());
        log.info("Executing " + jobs.getJobs().size() + " Jobs!");
        for (ContainerJob job : jobs.getJobs()) {
            log.info("Job: " + job.getType().name() + " " + job.toString());
        }
        ContainerJobResults results = containerProxy.getRemoteObject().execJobs(jobs);
        log.info("Returning results for jobs from sessionID: " + sessionID.getLongId());
        return results;
    }

    public void closeSession() throws RemoteException {
        containerProxy.getRemoteObject().destroyContainerSession(containerSessionID, sessionID);
    }
}
