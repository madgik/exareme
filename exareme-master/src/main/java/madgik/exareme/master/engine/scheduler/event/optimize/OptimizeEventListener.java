/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.optimize;


import madgik.exareme.master.engine.scheduler.QuerySchedulerState;
import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class OptimizeEventListener implements EventListener<OptimizeEvent> {

    private static final long serialVersionUID = 1L;
    private QuerySchedulerState schedulerState = null;

    public OptimizeEventListener(QuerySchedulerState schedulerState) {
        this.schedulerState = schedulerState;
    }

    public void processed(OptimizeEvent event, RemoteException exception,
        EventProcessor processor) {
        if (exception != null) {
            exception.printStackTrace();
        }
    }
}
