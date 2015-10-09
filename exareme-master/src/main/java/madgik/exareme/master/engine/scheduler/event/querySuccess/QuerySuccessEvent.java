/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.querySuccess;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.eventProcessor.Event;

/**
 * @author herald
 */
public class QuerySuccessEvent implements Event {

    private static final long serialVersionUID = 1L;
    public final AdpDBQueryExecutionPlan execPlan;
    public final Registry.Schema schema;
    public final AdpDBStatus status;
    public final AdpDBQueryID queryID;

    public QuerySuccessEvent(AdpDBQueryExecutionPlan execPlan, Registry.Schema schema,
        AdpDBStatus status, AdpDBQueryID queryID) {
        this.execPlan = execPlan;
        this.schema = schema;
        this.status = status;
        this.queryID = queryID;
    }
}
