/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler;


import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.common.schema.QueryScript;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class QueryScriptStatusSerialized extends QueryScriptStatus implements Serializable {

    private static final long serialVersionUID = 1L;
    private QueryScript queryScript;
    private QueryScriptState state;
    private AdpDBStatus status;

    public QueryScriptStatusSerialized() {
    }

    public QueryScriptStatusSerialized(AdpDBQueryID queryID, QueryScript queryScript,
                                       QueryScriptState state, AdpDBStatus status) {
        super(queryID);
        this.queryScript = queryScript;
        this.state = state;
        this.status = status;
    }

    @Override
    public QueryScript getQueryScript() {
        return queryScript;
    }

    @Override
    public QueryScriptState getState() {
        return state;
    }

    @Override
    public AdpDBStatus getStatus() {
        return status;
    }

    @Override
    public QueryScriptStatus createSerializableStatus() throws RemoteException {
        return this;
    }
}
