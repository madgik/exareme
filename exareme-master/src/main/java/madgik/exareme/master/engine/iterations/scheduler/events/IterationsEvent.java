package madgik.exareme.master.engine.iterations.scheduler.events;

import madgik.exareme.utils.eventProcessor.Event;

/**
 * Abstract event class which all iterations related events must subtype.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public abstract class IterationsEvent implements Event {
    // Each iterations related event, needs to be tied with an algorithm key.
    protected String algorithmKey;

    public IterationsEvent(String algorithmKey) {
        this.algorithmKey = algorithmKey;
    }
}
