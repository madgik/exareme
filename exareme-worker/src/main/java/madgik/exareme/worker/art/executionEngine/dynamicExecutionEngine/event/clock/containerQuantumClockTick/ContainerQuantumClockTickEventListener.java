/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.containerQuantumClockTick;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class ContainerQuantumClockTickEventListener
    implements EventListener<ContainerQuantumClockTickEvent> {
    public static final ContainerQuantumClockTickEventListener instance =
        new ContainerQuantumClockTickEventListener();
    private static final long serialVersionUID = 1L;

    public ContainerQuantumClockTickEventListener() {
    }

    @Override public void processed(ContainerQuantumClockTickEvent event, RemoteException exception,
        EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("Container Quantum Clock", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
