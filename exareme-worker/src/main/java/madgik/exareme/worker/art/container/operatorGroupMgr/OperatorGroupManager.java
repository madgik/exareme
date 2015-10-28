package madgik.exareme.worker.art.container.operatorGroupMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Vaggelis Nikolopoulos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */

public class OperatorGroupManager implements OperatorGroupManagerInterface {
    private static final Logger log = Logger.getLogger(OperatorGroupManager.class);
    HashMap<PlanSessionID, HashMap<ContainerSessionID, HashSet<String>>> groupTerminatedOperators =
        new HashMap<>();

    public OperatorGroupManager() {
    }

    @Override public void clear(PlanSessionID planID) {
        groupTerminatedOperators.remove(planID);
    }

    @Override public void clear(PlanSessionID planID, ContainerSessionID csID) {
        groupTerminatedOperators.get(planID).remove(csID);
    }

    @Override
    public void setTerminated(PlanSessionID planID, ContainerSessionID csID, String opName) {
        if (!groupTerminatedOperators.containsKey(planID)) {
            groupTerminatedOperators
                .put(planID, new HashMap<ContainerSessionID, HashSet<String>>());
        }
        if (!groupTerminatedOperators.get(planID).containsKey(csID)) {
            groupTerminatedOperators.get(planID).put(csID, new HashSet<String>());
        }
        HashSet<String> terminatedOPS = groupTerminatedOperators.get(planID).get(csID);
        if (terminatedOPS.contains(opName)) {
            log.error("Operator " + opName + " already terminated");
        } else {
            terminatedOPS.add(opName);
        }
    }

    @Override
    public int getNumberOfTerminatedOperators(PlanSessionID planID, ContainerSessionID csID) {
        try {
            return groupTerminatedOperators.get(planID).get(csID).size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override public String toString() {
        return "OperatorGroupManager{" +
            "groupTerminatedOperators=" + groupTerminatedOperators +
            '}';
    }
}
