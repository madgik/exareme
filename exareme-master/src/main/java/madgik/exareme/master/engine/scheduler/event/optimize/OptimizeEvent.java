/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.scheduler.event.optimize;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.common.schema.Statistics;
import madgik.exareme.master.engine.historicalData.AdpDBHistoricalQueryData;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.eventProcessor.Event;

/**
 * @author herald
 */
public class OptimizeEvent implements Event {

    private static final long serialVersionUID = 1L;
    public final AdpDBQueryID queryID;
    public final String queryScriptString;
    public final QueryScript queryScript;
    public final Registry.Schema schema;
    public final Statistics stats;
    public final AdpDBHistoricalQueryData queryData;

    public OptimizeEvent(String queryScript, Registry.Schema schema, Statistics stats,
                         AdpDBHistoricalQueryData queryData, AdpDBQueryID queryID) {
        this.queryScriptString = queryScript;
        this.queryScript = null;
        this.schema = schema;
        this.stats = stats;
        this.queryData = queryData;
        this.queryID = queryID;
    }


    public OptimizeEvent(QueryScript queryScript, Registry.Schema schema, Statistics stats,
                         AdpDBHistoricalQueryData queryData, AdpDBQueryID queryID) {
        this.queryScriptString = null;
        this.queryScript = queryScript;
        this.schema = schema;
        this.stats = stats;
        this.queryData = queryData;
        this.queryID = queryID;
    }
}
