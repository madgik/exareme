/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import java.io.Serializable;

/**
 * @author Herald Kllapi
 * @since 1.0
 */
public class ContainerID implements Comparable<ContainerID>, Serializable {
    private static final long serialVersionUID = 1L;
    private final long id;

    public ContainerID(long id) {
        this.id = id;
    }

    @Override public int compareTo(ContainerID sessionID) {
        return (int) (id - sessionID.id);
    }

    @Override public boolean equals(Object object) {
        if (object instanceof ContainerID) {
            ContainerID sessionID = (ContainerID) object;
            return (id == sessionID.id);
        }
        throw new IllegalArgumentException();
    }

    @Override public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    public long getLongId() {
        return id;
    }
}
