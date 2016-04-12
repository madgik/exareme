package madgik.exareme.master.engine.dflSegment;

import madgik.exareme.common.schema.expression.SQLScript;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * Created by thomas on 18/6/2015.
 */
public class ScriptSegment extends Segment {
    private static Logger log = Logger.getLogger(ScriptSegment.class);

    public ScriptSegment(SQLScript sqlScript) throws RemoteException {
        super(sqlScript, null, null, "script");
    }

//
//    @Override
//    public AdpDBQueryExecutionPlan produceOptimizedPlan(AdpDBOptimizer optimizer, Registry registry,
//                                                           Statistics stats, AdpDBHistoricalQueryData queryData,
//                                                           AdpDBQueryID queryId, AdpDBClientProperties props,
//                                                           boolean schedule, boolean validate) throws RemoteException{
//
//        plan = optimizer.optimize( script, registry, null, queryData, queryId, props,
//                true  /* schedule */, true  /* validate */);
//        log.trace("Optimized" + plan.toString());
//
//        return plan;
//    }
}
