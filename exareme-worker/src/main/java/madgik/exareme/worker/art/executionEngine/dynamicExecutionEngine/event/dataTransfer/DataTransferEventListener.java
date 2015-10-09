/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.dataTransfer;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi
 */
public class DataTransferEventListener implements EventListener<DataTransferEvent> {
    public static final DataTransferEventListener instance = new DataTransferEventListener();
    private static final long serialVersionUID = 1L;

    public DataTransferEventListener() {
    }

    @Override public void processed(DataTransferEvent event, RemoteException exception,
        EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("DataTransfer", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
