/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createDataflow;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;

import java.rmi.RemoteException;

/**
 * @author John Chronis
 */
public class CreateDataflowEventListener implements EventListener<CreateDataflowEvent> {

    public static final CreateDataflowEventListener instance = new CreateDataflowEventListener();
    private static final long serialVersionUID = 1L;

    public CreateDataflowEventListener() {
    }

    @Override
    public void processed(CreateDataflowEvent event, RemoteException exception,
                          EventProcessor processor) {
        if (exception != null) {
            LogUtils.logException("createDataflow", exception);
        }
        event.done();
    }
}
