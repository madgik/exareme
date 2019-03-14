/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class IndependentEventsListener implements EventListener<IndependentEvents> {

    public IndependentEventsListener() {
    }

    @Override
    public void processed(IndependentEvents event, RemoteException exception,
                          EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("Independent", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
