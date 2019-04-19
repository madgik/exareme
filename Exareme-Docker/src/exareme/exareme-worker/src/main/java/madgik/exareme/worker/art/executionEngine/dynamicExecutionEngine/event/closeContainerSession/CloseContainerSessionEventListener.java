/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.closeContainerSession;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class CloseContainerSessionEventListener
        implements EventListener<CloseContainerSessionEvent> {
    public static final CloseContainerSessionEventListener instance =
            new CloseContainerSessionEventListener();
    private static final long serialVersionUID = 1L;

    public CloseContainerSessionEventListener() {
    }

    @Override
    public void processed(CloseContainerSessionEvent event, RemoteException exception,
                          EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("CloseContainerSession", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
