/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.collections.ReadOnlyViewList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
//import madgik.exareme.utils.collections.ReadOnlyViewList;


/**
 * @author heraldkllapi
 */
public class ContainerJobs implements Serializable {

    public ContainerSessionID contSessionID;
    public PlanSessionID sessionID;
    private ReadOnlyViewList<ContainerJob> jobs = null;

    public ContainerJobs() {
        jobs = new ReadOnlyViewList<>(new ArrayList<ContainerJob>());
    }

    public void setSession(ContainerSessionID contSessionID, PlanSessionID sessionID) {
        this.contSessionID = contSessionID;
        this.sessionID = sessionID;
    }

    public void addJobs(ContainerJobs other) {
        for (ContainerJob job : other.getJobs()) {
            jobs.getList().add(job);
        }
    }

    public void addJob(ContainerJob job) {
        jobs.getList().add(job);
    }

    public List<ContainerJob> getJobs() {
        return jobs.getReadOnlyView();
    }
}
