package madgik.exareme.worker.art.executionPlan;

import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.entity.StateEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author herald
 */
public class StateEntityGraph {
    // Operator to state map
    private Map<String, Map<String, StateEntity>> opStateMap =
        new HashMap<String, Map<String, StateEntity>>();

    // State to operator map
    private Map<String, Map<String, OperatorEntity>> stateOpMap =
        new HashMap<String, Map<String, OperatorEntity>>();

    public StateEntityGraph() {

    }
}
