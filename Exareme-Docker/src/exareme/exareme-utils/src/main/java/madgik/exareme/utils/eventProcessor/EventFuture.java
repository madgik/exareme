/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.eventProcessor;

/**
 * @author herald
 */
public class EventFuture {

    private Status status = null;
    private Event event = null;

    public EventFuture(Event event) {
        this.event = event;
        this.status = Status.queued;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
