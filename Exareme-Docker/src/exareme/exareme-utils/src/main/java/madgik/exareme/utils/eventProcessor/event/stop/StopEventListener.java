/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor.event.stop;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class StopEventListener implements EventListener<StopEvent> {

    public StopEventListener() {
    }

    public void processed(StopEvent event, RemoteException exception, EventProcessor processor) {
    }
}
