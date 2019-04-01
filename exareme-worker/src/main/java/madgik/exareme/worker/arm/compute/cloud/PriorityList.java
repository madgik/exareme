/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm.compute.cloud;

import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;

import java.util.*;

/**
 * @author Christos
 */
public abstract class PriorityList implements PriorityListInterface {

    @Override
    public boolean checkPriority(ArmComputeSessionID sessionID,
                                 TreeMap<Integer, LinkedList<ArmComputeSessionID>> onusIndex) {

        int counter, i;
        LinkedList<ArmComputeSessionID> currentSessions, prioritySessions;
        Set set;
        Iterator it;
        Map.Entry map;

        prioritySessions = new LinkedList<ArmComputeSessionID>();
        counter = 0;

        set = onusIndex.entrySet();
        it = set.iterator();

        while (it.hasNext()) {
            map = (Map.Entry) it.next();
            currentSessions = (LinkedList<ArmComputeSessionID>) map.getValue();

            for (i = 0; i < currentSessions.size(); i++) {
                prioritySessions.add(currentSessions.get(i));
            }
            counter += currentSessions.size();
            if (counter >= 10) {
                break;
            }
        }

        return prioritySessions.contains(sessionID);
    }
}
