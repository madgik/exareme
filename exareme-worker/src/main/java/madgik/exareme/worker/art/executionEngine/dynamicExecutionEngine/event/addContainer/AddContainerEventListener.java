/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.addContainer;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class AddContainerEventListener implements EventListener<AddContainerEvent> {
    public static final AddContainerEventListener instance = new AddContainerEventListener();
    private static final long serialVersionUID = 1L;

    public AddContainerEventListener() {
    }

    @Override public void processed(AddContainerEvent event, RemoteException exception,
        EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("AddContainer", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
