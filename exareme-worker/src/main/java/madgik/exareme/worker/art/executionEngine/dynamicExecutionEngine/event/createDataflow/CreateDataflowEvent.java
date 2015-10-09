/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createDataflow;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;

import java.util.HashMap;

/**
 * @author John Chronis
 */
public class CreateDataflowEvent extends ExecEngineEvent {

    private static final long serialVersionUID = 1L;

    public ExecutionPlan plan;
    //public ContainerJobs jobs;
    public HashMap<EntityName, ContainerJobs> jobs;
    public ContainerSession session = null;
    public HashMap<EntityName, ContainerProxy> entityNameToProxy;
    public HashMap<EntityName, ContainerSessionID> entityNameToSessionID;
    public HashMap<EntityName, ContainerSession> entityNameToSession;
    ContainerJobResults results;

    public CreateDataflowEvent(ExecutionPlan plan, PlanEventScheduler aThis,
        PlanEventSchedulerState pstate) {
        super(pstate);
        this.entityNameToProxy = new HashMap<EntityName, ContainerProxy>();
        this.entityNameToSessionID = new HashMap<EntityName, ContainerSessionID>();
        this.entityNameToSession = new HashMap<EntityName, ContainerSession>();
        this.jobs = new HashMap<EntityName, ContainerJobs>();
        this.plan = plan;
    }
}
