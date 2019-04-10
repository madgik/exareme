/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import java.util.ArrayList;

/**
 * @author herald
 */
public class QuerySchedulerState {

    public final EventProcessor eventProcessor;
    public final AdpDBQueryScheduler queryScheduler;
    public final AdpDBManager manager;
    private final AdpDBQueryID queryID;
    private final ArrayList<AdpDBQueryListener> listeners;
    private AdpDBQueryExecutionPlan execPlan = null;
    private AdpDBStatus status = null;
    private QueryScriptState state = QueryScriptState.queued;

    public QuerySchedulerState(EventProcessor eventProcessor, AdpDBQueryScheduler queryScheduler,
                               AdpDBManager manager, AdpDBQueryID queryID) {
        this.eventProcessor = eventProcessor;
        this.queryScheduler = queryScheduler;
        this.manager = manager;
        this.listeners = new ArrayList<AdpDBQueryListener>();
        this.queryID = queryID;
    }

    public void registerListener(AdpDBQueryListener listener) {
        listeners.add(listener);
    }

    public AdpDBQueryExecutionPlan getExecPlan() {
        return execPlan;
    }

    public void setExecPlan(AdpDBQueryExecutionPlan execPlan) {
        this.execPlan = execPlan;
    }

    public AdpDBStatus getStatus() {
        return status;
    }

    public void setStatus(AdpDBStatus status) {
        this.status = status;
    }

    public void setState(QueryScriptState state) {
        this.state = state;

        if (state == QueryScriptState.success || state == QueryScriptState.error) {
            for (AdpDBQueryListener l : listeners) {
                l.terminated(queryID, status);
            }
        }
    }

    public QueryScriptState getState(AdpDBQueryID queryID) {
        return state;
    }
}
