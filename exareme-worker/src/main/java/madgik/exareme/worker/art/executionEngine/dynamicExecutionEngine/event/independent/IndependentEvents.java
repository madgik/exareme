/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent;

import madgik.exareme.utils.eventProcessor.ActiveEvent;
import madgik.exareme.utils.eventProcessor.Event;
import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;

import java.util.ArrayList;

/**
 * @author heraldkllapi
 */
public class IndependentEvents extends ExecEngineEvent {
    public ArrayList<ActiveEvent> events = new ArrayList<ActiveEvent>();
    public int messageCount = 0;
    public long queueTime = System.currentTimeMillis();

    public IndependentEvents(PlanEventSchedulerState state) {
        super(state);
    }

    public void addEvent(Event event, EventHandler handler, EventListener listener) {
        ActiveEvent active = new ActiveEvent(event, handler, listener, null);
        events.add(active);
    }
}
