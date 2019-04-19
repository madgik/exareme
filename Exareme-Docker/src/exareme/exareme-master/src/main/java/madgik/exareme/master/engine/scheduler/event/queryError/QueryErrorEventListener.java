/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.queryError;


import madgik.exareme.master.engine.scheduler.QuerySchedulerState;
import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class QueryErrorEventListener implements EventListener<QueryErrorEvent> {

    private static final long serialVersionUID = 1L;
    private QuerySchedulerState schedulerState = null;

    public QueryErrorEventListener(QuerySchedulerState schedulerState) {
        this.schedulerState = schedulerState;
    }

    public void processed(QueryErrorEvent event, RemoteException exception,
                          EventProcessor processor) {
        if (exception != null) {
            exception.printStackTrace();
        }
    }
}
