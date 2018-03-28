/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containersError;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.Iterator;

/**
 * @author Herald Kllapi
 */
public class ContainersErrorEventHandler implements ExecEngineEventHandler<ContainersErrorEvent> {
    public static final ContainersErrorEventHandler instance = new ContainersErrorEventHandler();
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(ContainersErrorEventHandler.class);

    public ContainersErrorEventHandler() {
    }

    @Override public void preProcess(ContainersErrorEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        for (EntityName containerName : event.containers) {
            try {
                ContainerProxy containerProxy = state.registryProxy.lookupContainer(containerName);
                log.debug(
                    "Container status: " + (containerProxy.connect().execJobs(new ContainerJobs())
                        != null));
            } catch (Exception e) {
                log.error(e);
                if (AdpProperties.getArtProps().getString("art.container.errorBehavior").equals("returnError")){
                    event.faultyContainers.add(containerName);
                    state.getPlanSession().getPlanSessionStatus().getExceptions().add(
                            new RemoteException("Containers: " + event.faultyContainers + " not responding"));
                    log.error("Containers: " + event.faultyContainers + " not responding");

                } else {
//                    log.debug("Removing container: " + containerProxy.getEntityName());
//                    event.faultyContainers.add(containerProxy.getEntityName());
//                    ArtRegistryLocator.getArtRegistryProxy()
//                            .removeContainer(containerProxy.getEntityName());
                }

            }
        }
    }

    @Override public void handle(ContainersErrorEvent event, EventProcessor proc)
        throws RemoteException {

    }

    @Override public void postProcess(ContainersErrorEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        if (!event.faultyContainers.isEmpty()) {
            if (AdpProperties.getArtProps().getString("art.container.errorBehavior").equals("returnError")) {
                state.eventScheduler.destroyPlanWithError();
            } else {
//            for (Iterator<EntityName> it = event.containers.iterator(); it.hasNext(); ) {
//                EntityName containerName = it.next();
//                if (event.faultyContainers.contains(containerName)) {
//                    it.remove();
//                }
//            }
//            state.eventScheduler.destroyPlanWithError();
            }
        }



    }
}
