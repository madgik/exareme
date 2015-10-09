/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.planTermination;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class PlanTerminationEventListener implements EventListener<PlanTerminationEvent> {
    public static final PlanTerminationEventListener instance = new PlanTerminationEventListener();
    private static final long serialVersionUID = 1L;

    public PlanTerminationEventListener() {
    }

    @Override public void processed(PlanTerminationEvent event, RemoteException exception,
        EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("PlanTermination", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
