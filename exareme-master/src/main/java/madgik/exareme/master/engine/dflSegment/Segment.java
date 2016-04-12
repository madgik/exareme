package madgik.exareme.master.engine.dflSegment;

import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.common.schema.expression.SQLScript;

import java.util.List;

/**
 * Created by thomas on 18/6/2015.
 */
public abstract class Segment {
    //protected AdpDBQueryExecutionPlan plan;
    protected List<Segment> subSegments;
    protected SQLScript mainSqlScript;
    protected QueryScript script;
    private String type;

    public Segment(SQLScript sqlScript, QueryScript script, List<Segment> subSegments, String type ) {
        this.script = script;
        this.mainSqlScript = sqlScript;
        this.subSegments = subSegments;
        this.type = type;
    }

//    AdpDBQueryExecutionPlan getOptimizedPlan() throws RemoteException {
//        return plan;
//    }

    public List<Segment> getSubSegments(){
        return subSegments;
    }

    public SQLScript getSQLScript() {
        return this.mainSqlScript;
    }

    public QueryScript getQueryScript() {
        return this.script;
    }

    public void setQueryScript(QueryScript script) {
        this.script = script;
    }

//    public abstract  AdpDBQueryExecutionPlan produceOptimizedPlan(AdpDBOptimizer optimizer, Registry registry,
//                                                                    Statistics stats, AdpDBHistoricalQueryData queryData,
//                                                                    AdpDBQueryID queryID, AdpDBClientProperties props,
//                                                                    boolean schedule, boolean validate) throws RemoteException;

    public String getType() {
        return type;
    }
}
