/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containersError;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi
 */
public class ContainersErrorEventListener implements EventListener<ContainersErrorEvent> {
    public static final ContainersErrorEventListener instance = new ContainersErrorEventListener();
    private static final long serialVersionUID = 1L;

    public ContainersErrorEventListener() {
    }

    @Override public void processed(ContainersErrorEvent event, RemoteException exception,
        EventProcessor processor) {

        event.done();
    }
}
