/*
 * Copyright MaDgIK Group 2010-2104.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;

import java.util.HashMap;

/**
 * @author John Chronis
 */
public class CreateDataflowJob implements ContainerJob {

    public ExecutionPlan plan;
    public HashMap<EntityName, ContainerProxy> entityNameToProxy;
    public HashMap<EntityName, ContainerSessionID> entityNameToSessionID;
    public PlanSessionReportID planSessionReportID;
    public PlanEventScheduler planEventScheduler;
    public EntityName entityName;

    public CreateDataflowJob(ExecutionPlan plan, HashMap<EntityName, ContainerProxy> NameToProxy,
                             HashMap<EntityName, ContainerSessionID> NameToSessionID,
                             PlanSessionReportID planSessionReportID, EntityName entityName) {
        this.plan = plan;
        this.entityNameToProxy = NameToProxy;
        this.entityNameToSessionID = NameToSessionID;
        this.planSessionReportID = planSessionReportID;
        this.entityName = entityName;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.distributedJobCreateDataflow;
    }


}
