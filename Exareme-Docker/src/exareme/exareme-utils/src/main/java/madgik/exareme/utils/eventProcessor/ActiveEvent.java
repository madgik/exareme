/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class ActiveEvent {
    private Event event = null;
    private EventHandler handler = null;
    private EventListener eventListener = null;
    private EventFuture future = null;
    private RemoteException exception = null;
    private long queuedTime = 0;
    private long startProcessTime = 0;
    private long endProcessTime = 0;

    public ActiveEvent(Event event, EventHandler handler, EventListener eventListener,
                       EventFuture future) {
        this.event = event;
        this.handler = handler;
        this.eventListener = eventListener;
        this.future = future;
        this.queuedTime = System.currentTimeMillis();
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public RemoteException getException() {
        return exception;
    }

    public void setException(RemoteException exception) {
        this.exception = exception;
    }

    public EventFuture getFuture() {
        return future;
    }

    public void setFuture(EventFuture future) {
        this.future = future;
    }

    public EventHandler getHandler() {
        return handler;
    }

    public void setHandler(EventHandler handler) {
        this.handler = handler;
    }

    public void startProcessing() {
        this.startProcessTime = System.currentTimeMillis();
    }

    public void endProcessing() {
        this.endProcessTime = System.currentTimeMillis();
    }

    public long getWaitTime() {
        return startProcessTime - queuedTime;
    }

    public long getProcessTime() {
        return endProcessTime - startProcessTime;
    }
}
