/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.simple;

import java.util.ArrayList;

/**
 * @author Xristos
 */
public interface ContainerManagerInterfaceOld {
    ArrayList<String> get(int NumberOfDesirableContainers, int sessionID);

    ArrayList<String> get(int NumberOfDesirableContainers, int sessionID, int durationTime);

    ArrayList<String> getAtMost(int NumberOfDesirableContainers, int sessionID);

    void release(ArrayList<String> released_containers, int sessionID);

    boolean tryGet(int NumberOfDesirableContainers, int sessionID);

    void releaseAll(int sessionID);
}
