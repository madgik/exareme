/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.exception;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class OperatorExceptionEventListener implements EventListener<OperatorExceptionEvent> {
    public static final OperatorExceptionEventListener instance =
            new OperatorExceptionEventListener();
    private static final long serialVersionUID = 1L;

    public OperatorExceptionEventListener() {
    }

    @Override
    public void processed(OperatorExceptionEvent event, RemoteException exception,
                          EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("OperatorException", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
