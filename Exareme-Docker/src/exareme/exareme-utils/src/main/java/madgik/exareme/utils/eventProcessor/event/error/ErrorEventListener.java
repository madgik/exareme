/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor.event.error;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class ErrorEventListener implements EventListener<ErrorEvent> {

    public void processed(ErrorEvent event, RemoteException exception, EventProcessor processor) {
    }
}
