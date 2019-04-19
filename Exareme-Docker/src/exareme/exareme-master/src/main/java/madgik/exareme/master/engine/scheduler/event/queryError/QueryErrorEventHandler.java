/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.queryError;

import madgik.exareme.master.engine.scheduler.QuerySchedulerState;
import madgik.exareme.master.engine.scheduler.QueryScriptState;
import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class QueryErrorEventHandler implements EventHandler<QueryErrorEvent> {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(QueryErrorEventHandler.class);
    private QuerySchedulerState state = null;

    public QueryErrorEventHandler(QuerySchedulerState schedulerState) {
        this.state = schedulerState;
    }

    public void handle(QueryErrorEvent event, EventProcessor proc) throws RemoteException {
        try {
            log.error("Query Error", event.exception);
            state.setState(QueryScriptState.error);
        } catch (Exception e) {
            throw new RemoteException("Cannot handle set error event", e);
        }
    }
}
