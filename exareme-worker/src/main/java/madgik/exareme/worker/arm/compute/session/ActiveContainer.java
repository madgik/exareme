/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.session;

import madgik.exareme.common.art.entity.EntityName;

import java.io.Serializable;

/**
 * @author herald
 */
public class ActiveContainer implements Serializable {
    private static final long serialVersionUID = 1L;
    public final long ID;
    public int containerID = 0;
    public EntityName containerName = null;

    public ActiveContainer(int containerID, EntityName containerName, int ID) {
        this.containerID = containerID;
        this.containerName = containerName;
        this.ID = ID;
    }

    @Override public boolean equals(Object obj) {

        if (obj instanceof ActiveContainer) {
            ActiveContainer container = (ActiveContainer) obj;

            return ID == container.ID;
        } else {
            throw new IllegalArgumentException();
        }

    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public int compareTo(ActiveContainer container) {
        return (int) (ID - container.ID);
    }

    @Override public int hashCode() {
        long hash = 7;
        hash = 53 * hash + this.ID;
        return (int) hash;
    }


}
