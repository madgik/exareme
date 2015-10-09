/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.session;

import java.io.Serializable;

/**
 * @author Herald Kllapi<br> herald@di.uoa.gr<br> University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ArmComputeSessionID implements Serializable {

    private static final long serialVersionUID = 1L;
    private long id;

    public ArmComputeSessionID(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public int compareTo(ArmComputeSessionID sessionID) {
        return (int) (id - sessionID.id);
    }

    @Override @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public boolean equals(Object object) {
        if (object instanceof ArmComputeSessionID) {
            ArmComputeSessionID sessionID = (ArmComputeSessionID) object;
            return (id == sessionID.id);
        }
        throw new IllegalArgumentException();
    }

    @Override public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
