/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class ContainerJobsEventListener implements EventListener<ContainerJobsEvent> {
    public static final ContainerJobsEventListener instance = new ContainerJobsEventListener();

    @Override public void processed(ContainerJobsEvent event, RemoteException exception,
        EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("Create", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
