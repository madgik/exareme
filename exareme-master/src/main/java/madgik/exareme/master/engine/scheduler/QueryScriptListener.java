/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import org.apache.log4j.Logger;

/**
 * @author herald
 */
public class QueryScriptListener implements AdpDBQueryListener {
    private static final Logger log = Logger.getLogger(QueryScriptListener.class);
    private final AdpDBStatus status;
    private final QuerySchedulerState state;
    private final AdpDBQueryExecutionPlan execPlan;

    public QueryScriptListener(AdpDBStatus status, QuerySchedulerState state,
        AdpDBQueryExecutionPlan execPlan) {
        this.status = status;
        this.state = state;
        this.execPlan = execPlan;
    }

    @Override public void statusChanged(AdpDBQueryID queryID, AdpDBStatus status) {

    }

    @Override public void terminated(AdpDBQueryID queryID, AdpDBStatus status) {
        try {
            state.queryScheduler.getState().setStatus(status);
            if (status == null || status.hasError()) {
                state.queryScheduler.error(status.getLastException());
            } else {
                state.queryScheduler.success(execPlan, status);
            }
        } catch (Exception e) {
            log.error("Cannot update query termination status", e);
        }
    }
}
