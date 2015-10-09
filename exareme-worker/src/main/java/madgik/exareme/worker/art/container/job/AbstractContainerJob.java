/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.resources.ContainerJobResources;

/**
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 */
public class AbstractContainerJob {

    ContainerJobResources resources;
    ContainerJob job;
    ContainerSessionID contSessionID;
    PlanSessionID sessionID;

    public AbstractContainerJob(ContainerJob job, ContainerJobResources resources,
        ContainerSessionID contSessionID, PlanSessionID sessionID) {
        this.job = job;
        this.resources = resources;
        this.contSessionID = contSessionID;
        this.sessionID = sessionID;
    }

    public ContainerSessionID contSessionID() {
        return contSessionID;
    }

    public PlanSessionID sessionID() {
        return sessionID;
    }

    public ContainerJob getJob() {
        return job;
    }

    public ContainerJobResources getResources() {
        return resources;
    }

}
