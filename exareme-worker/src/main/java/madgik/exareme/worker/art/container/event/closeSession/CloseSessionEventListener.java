/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.event.closeSession;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class CloseSessionEventListener implements EventListener<CloseSessionEvent> {
    public static CloseSessionEventListener instance = new CloseSessionEventListener();
    private static Logger log = Logger.getLogger(CloseSessionEventListener.class);

    @Override public void processed(CloseSessionEvent event, RemoteException exception,
        EventProcessor processor) {
        if (exception != null) {
            log.error("Cannot close session", exception);
        }
    }
}
