/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm.compute.cloud;

import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * @author Christos
 */
public interface PriorityListInterface {

    boolean checkPriority(ArmComputeSessionID sessionID,
                          TreeMap<Integer, LinkedList<ArmComputeSessionID>> onusIndex);

}
