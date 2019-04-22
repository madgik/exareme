/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor.event.stop;

import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import java.rmi.RemoteException;
import java.util.concurrent.Semaphore;

/**
 * @author herald
 */
public class StopEventHandler implements EventHandler<StopEvent> {

    private Semaphore semaphore = null;

    public StopEventHandler(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void handle(StopEvent event, EventProcessor proc) throws RemoteException {
        semaphore.release();
    }
}
