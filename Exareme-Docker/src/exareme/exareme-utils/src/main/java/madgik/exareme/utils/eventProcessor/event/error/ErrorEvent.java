/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor.event.error;

import madgik.exareme.utils.eventProcessor.ActiveEvent;
import madgik.exareme.utils.eventProcessor.Event;

/**
 * @author herald
 */
public class ErrorEvent implements Event {

    private static final long serialVersionUID = 1L;
    private ActiveEvent activeEvent = null;
    private Exception exception = null;

    public ErrorEvent(ActiveEvent activeEvent, Exception exception) {
        this.activeEvent = activeEvent;
        this.exception = exception;
    }

    public ActiveEvent getActiveEvent() {
        return activeEvent;
    }

    public void setActiveEvent(ActiveEvent activeEvent) {
        this.activeEvent = activeEvent;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
