/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.scheduleQuery;

import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.engine.AdpDBExecutor;
import madgik.exareme.master.engine.scheduler.QuerySchedulerState;
import madgik.exareme.master.engine.scheduler.QueryScriptListener;
import madgik.exareme.master.engine.scheduler.QueryScriptState;
import madgik.exareme.utils.eventProcessor.EventHandler;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class ScheduleEventHandler implements EventHandler<ScheduleEvent> {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ScheduleEventHandler.class);
    private QuerySchedulerState state = null;

    public ScheduleEventHandler(QuerySchedulerState schedulerState) {
        this.state = schedulerState;
    }

    @Override public void handle(ScheduleEvent event, EventProcessor proc) throws RemoteException {
        try {
            log.debug("Scheduling query plan: " + event.queryID.getQueryID());
            AdpDBExecutor executor = state.manager.getAdpDBExecutor();
            AdpDBStatus status = executor.executeScript(event.execPlan, null);

            QueryScriptListener listener = new QueryScriptListener(status, state, event.execPlan);
            executor.registerListener(listener, event.queryID);

            state.setState(QueryScriptState.running);
        } catch (Exception e) {
            state.queryScheduler.error(e);
        }
    }
}
