/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.globalQuantumClockTick;

import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class GlobalQuantumClockTickEventHandler
    implements ExecEngineEventHandler<GlobalQuantumClockTickEvent> {
    public static final GlobalQuantumClockTickEventHandler instance =
        new GlobalQuantumClockTickEventHandler();
    private static final long serialVersionUID = 1L;

    public GlobalQuantumClockTickEventHandler() {
    }

    @Override
    public void preProcess(GlobalQuantumClockTickEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        // ...
    }

    @Override public void handle(GlobalQuantumClockTickEvent event, EventProcessor proc)
        throws RemoteException {
        // ...
    }

    @Override
    public void postProcess(GlobalQuantumClockTickEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        LogUtils.logInfo("Total Quanta: " + state.totalQuanta);
    }
}
