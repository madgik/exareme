/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.destroy;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class DestroyEventListener implements EventListener<DestroyEvent> {
    public static final DestroyEventListener instance = new DestroyEventListener();
    private static final long serialVersionUID = 1L;

    public DestroyEventListener() {
    }

    @Override
    public void processed(DestroyEvent event, RemoteException exception, EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("Destroy", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
