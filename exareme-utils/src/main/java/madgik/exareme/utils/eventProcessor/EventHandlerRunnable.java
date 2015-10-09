/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class EventHandlerRunnable implements Runnable {

    private static Logger log = Logger.getLogger(EventHandlerRunnable.class);
    private ActiveEvent event = null;
    private EventProcessor eventProcessor = null;

    public EventHandlerRunnable(ActiveEvent event, EventProcessor eventProcessor) {
        this.event = event;
        this.eventProcessor = eventProcessor;
    }

    @Override public void run() {
        try {
            event.startProcessing();
            event.getHandler().handle(event.getEvent(), eventProcessor);
        } catch (RemoteException e) {
            event.setException(e);
        }
        event.getEventListener().processed(event.getEvent(), event.getException(), eventProcessor);
        event.endProcessing();
        log.debug("Event processed times: " +
            event.getWaitTime() + " / " + event.getProcessTime() +
            " (" + event.getEvent().getClass().getName() + ")");
    }
}
