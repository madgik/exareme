/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.buffer;

import madgik.exareme.worker.art.container.ContainerSession;

import java.io.Serializable;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class BufferID implements Serializable {

    private static final long serialVersionUID = 1L;
    public ContainerSession session = null;
    public long id = -1;

    public BufferID(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof BufferID) {
            BufferID buffId = (BufferID) object;
            return (id == buffId.id);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    public int compareTo(BufferID buffId) {
        return (int) (id - buffId.id);
    }
}
