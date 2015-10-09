/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.globalQuantumClockTick;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class GlobalQuantumClockTickEventListener
    implements EventListener<GlobalQuantumClockTickEvent> {
    public static final GlobalQuantumClockTickEventListener instance =
        new GlobalQuantumClockTickEventListener();
    private static final long serialVersionUID = 1L;

    public GlobalQuantumClockTickEventListener() {
    }

    @Override public void processed(GlobalQuantumClockTickEvent event, RemoteException exception,
        EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("Global Quantum Clock", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
