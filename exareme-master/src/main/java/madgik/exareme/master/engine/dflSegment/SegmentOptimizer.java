package madgik.exareme.master.engine.dflSegment;

import madgik.exareme.common.schema.Statistics;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBOptimizer;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.master.registry.Registry;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by thomas on 29/6/2015.
 */
public class SegmentOptimizer {

    private AdpDBOptimizer scriptOptimizer;
    private Registry registry;
    private Statistics stats;
    private String segmentId;
    private AdpDBClientProperties props;

    public SegmentOptimizer(AdpDBOptimizer scriptOptimizer, Registry registry, Statistics stats,
                            String segmentId, AdpDBClientProperties props) {
        this.scriptOptimizer = scriptOptimizer;
        this.registry = registry;
        this.stats = stats;
        this.segmentId = segmentId;
        this.props = props;
    }

    public AdpDBQueryExecutionPlan optimize(List<Segment> segments) throws RemoteException {
        List<AdpDBQueryExecutionPlan> segmentPlans = new ArrayList<>();
        AdpDBQueryID queryId = createNewQueryID();

        for (Segment seg : segments) {
            if (seg.getType().equals("script"))
                segmentPlans.add(scriptOptimizer.optimize(seg.getQueryScript(), registry, null, null, queryId,
                        props, true  /* schedule */, true  /* validate */));
            else{
//
//                InputData input =
//                        new InputData(script, registry.getSchema(), stats, null, queryId, 1,
//                                true, true);


            }

        }


        return mergePlans(segmentPlans);
    }

    private AdpDBQueryExecutionPlan mergePlans(List<AdpDBQueryExecutionPlan> plans) {
        return plans.get(0);
    }

    private AdpDBQueryID createNewQueryID() {
        return new AdpDBQueryID(UUID.randomUUID().getLeastSignificantBits());
    }
}