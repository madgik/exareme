/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.dataTransfer;

import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi
 */
public class DataTransferEventHandler implements ExecEngineEventHandler<DataTransferEvent> {
    public static final DataTransferEventHandler instance = new DataTransferEventHandler();
    private static final long serialVersionUID = 1L;

    public DataTransferEventHandler() {
    }

    @Override
    public void preProcess(DataTransferEvent event, PlanEventSchedulerState state)
            throws RemoteException {

    }

    @Override
    public void handle(DataTransferEvent event, EventProcessor proc)
            throws RemoteException {
        event.session.execJobs(event.jobs);
        event.messageCount = 1;
    }

    @Override
    public void postProcess(DataTransferEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
