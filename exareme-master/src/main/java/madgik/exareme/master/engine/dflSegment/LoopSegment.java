package madgik.exareme.master.engine.dflSegment;

import madgik.exareme.common.schema.expression.SQLScript;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by thomas on 18/6/2015.
 */
public class LoopSegment extends Segment{


    public LoopSegment(SQLScript whileSqlScript, List<Segment> subsegments) throws RemoteException {
        super(whileSqlScript, null, subsegments, "loop");
    }


//    @Override
//    public AdpDBQueryExecutionPlan produceOptimizedPlan(AdpDBOptimizer optimizer, Registry registry, Statistics stats, AdpDBHistoricalQueryData queryData, AdpDBQueryID queryID, AdpDBClientProperties props, boolean schedule, boolean validate) throws RemoteException {
//
//        System.out.println("xa");
//        return null;
//    }
}
