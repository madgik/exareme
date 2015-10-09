/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler;


import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.common.schema.QueryScript;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class QueryScriptStatus {

    private final AdpDBQueryID queryID;
    private final QuerySchedulerState state;

    QueryScriptStatus() {
        this.queryID = null;
        this.state = null;
    }

    public QueryScriptStatus(AdpDBQueryID queryID) {
        this.queryID = queryID;
        this.state = null;
    }

    public QueryScriptStatus(AdpDBQueryID queryID, QuerySchedulerState state) {
        this.queryID = queryID;
        this.state = state;
    }

    public AdpDBQueryID getQueryID() {
        return queryID;
    }

    public QueryScript getQueryScript() {
        return state.getExecPlan().getScript();
    }

    public QueryScriptState getState() {
        return state.getState(queryID);
    }

    public AdpDBStatus getStatus() {
        return state.getStatus();
    }

    public QueryScriptStatus createSerializableStatus() throws RemoteException {
        return new QueryScriptStatusSerialized(queryID, getQueryScript(), getState(),
            getStatus().createSerializableStatus());
    }
}
