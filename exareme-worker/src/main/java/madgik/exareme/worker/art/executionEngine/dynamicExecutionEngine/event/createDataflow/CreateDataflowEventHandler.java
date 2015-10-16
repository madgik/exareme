/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createDataflow;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.container.*;
import madgik.exareme.worker.art.container.job.CreateDataflowJob;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveContainer;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * @author John Chronis
 */
public class CreateDataflowEventHandler
    //  implements ExecEngineEventHandler<CreateDataflowEvent> {
{
    public static final CreateDataflowEventHandler instance = new CreateDataflowEventHandler();
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(CreateDataflowEventHandler.class);

    public CreateDataflowEventHandler() {
    }

    public void preProcess(CreateDataflowEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        ContainerSession containerSession = null;
        ContainerSessionID containerSessionID = null;
        ActiveContainer activeCont;
        state.createDataflowTime = System.currentTimeMillis();
        for (String containerName : event.plan.iterateContainers()) {
            EntityName entityName = event.plan.getContainer(containerName);

            if (entityName.toString().contains("any")) {
                continue;
            }
            ContainerProxy containerProxy = state.registryProxy.lookupContainer(entityName);
            event.plan.getContainer(containerName);
            containerSessionID = state.eventScheduler.getPlanManager()
                .createContainerSession(state.getPlanSessionID());
            containerSession = state.getContainerSession(containerName, containerSessionID);

            if (containerSession == null) {
                containerSession =
                    containerProxy.createSession(containerSessionID, state.getPlanSessionID());
            }
            event.entityNameToProxy.put(entityName, containerProxy);
            event.entityNameToSessionID.put(entityName, containerSessionID);
            event.entityNameToSession.put(entityName, containerSession);
        }

        state.setEntityNameToSession(event.entityNameToSession);

        for (String containerName : event.plan.iterateContainers()) {
            if (containerName.contains("any")) {
                continue;
            }
            log.debug("DThttp11 " + containerName + " " + containerSession);
            ContainerJobs jobs = new ContainerJobs();
            jobs.addJob(new CreateDataflowJob(event.plan, event.entityNameToProxy,
                event.entityNameToSessionID, state.getPlanSessionReportID(),
                event.plan.getContainer(containerName)));
            event.jobs.put(event.plan.getContainer(containerName), jobs);
            //      event.jobs.add();
        }
        log.debug("DThttp11 createdataflow tears");
        //LOGDSlog.info("[DS] CreateDataflowEventHandler");
    }

    public void handle(CreateDataflowEvent event, EventProcessor proc) throws RemoteException {
        //TODO(DS):  get all results
        event.results = new ContainerJobResults();
        for (Map.Entry<EntityName, ContainerSession> entry : event.entityNameToSession.entrySet()) {
            log.debug("DThttp11 handler " + entry.getKey().getName() + " " + event.jobs
                .get(entry.getKey()));

            ContainerJobResults results = entry.getValue().execJobs(event.jobs.get(entry.getKey()));

            for (ContainerJobResult result : results.getJobResults()) {
                event.results.addJobResult(result);
            }
        }
        log.debug("DThttp11 handler end");
    }

    public void postProcess(CreateDataflowEvent event, PlanEventSchedulerState state)
        throws RemoteException {
    }
}
