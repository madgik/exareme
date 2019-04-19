/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.containerQuantumClockTick;

import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class ContainerQuantumClockTickEventHandler
        implements ExecEngineEventHandler<ContainerQuantumClockTickEvent> {
    public static final ContainerQuantumClockTickEventHandler instance =
            new ContainerQuantumClockTickEventHandler();
    private static final long serialVersionUID = 1L;

    public ContainerQuantumClockTickEventHandler() {
    }

    @Override
    public void preProcess(ContainerQuantumClockTickEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        if (event.warning == false) {
            state.totalQuanta++;
        }
    }

    @Override
    public void handle(ContainerQuantumClockTickEvent event, EventProcessor proc)
            throws RemoteException {
        // ...
    }

    @Override
    public void postProcess(ContainerQuantumClockTickEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        // ...
    }
}
