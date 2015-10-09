/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor.event.error;

import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class ErrorEventHandler implements EventHandler<ErrorEvent> {

    public void handle(ErrorEvent event, EventProcessor proc) throws RemoteException {
    }
}
