/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.common.schema.Statistics;
import madgik.exareme.master.engine.historicalData.AdpDBHistoricalQueryData;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.serialization.SerializationUtil;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class InputData implements Serializable {
    private static final long serialVersionUID = 1L;
    public QueryScript script;
    public Registry.Schema schema;
    public Statistics stats;
    public AdpDBHistoricalQueryData queryData;
    public AdpDBQueryID queryID;
    public int maxNumCont;
    public boolean schedule;
    public boolean validate;

    public InputData() {
    }

    public InputData(InputData other) {
        this();
        copyFrom(other);
    }

    public InputData(QueryScript script, Registry.Schema schema, Statistics stats,
                     AdpDBHistoricalQueryData queryData, AdpDBQueryID queryID, int maxNumberOfContainers,
                     boolean schedule, boolean validate) {
        this.script = script;
        this.schema = schema;
        this.stats = stats;
        this.queryData = queryData;
        this.queryID = queryID;
        this.maxNumCont = maxNumberOfContainers;
        this.schedule = schedule;
        this.validate = validate;
    }

    public final void copyFrom(InputData other) {
        this.script = SerializationUtil.deepCopy(other.script);
        this.schema = SerializationUtil.deepCopy(other.schema);
        this.stats = SerializationUtil.deepCopy(other.stats);
        this.queryData = SerializationUtil.deepCopy(other.queryData);
        this.queryID = SerializationUtil.deepCopy(other.queryID);
        this.maxNumCont = other.maxNumCont;
        this.schedule = other.schedule;
        this.validate = other.validate;
    }
}
