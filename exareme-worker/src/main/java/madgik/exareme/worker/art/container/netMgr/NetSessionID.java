/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr;

import java.io.Serializable;

/**
 * @author herald
 */
public class NetSessionID implements Serializable {

    private static final long serialVersionUID = 1L;
    public long id = -1;

    public NetSessionID(long id) {
        this.id = id;
    }

    @Override public boolean equals(Object object) {
        if (object instanceof NetSessionID) {
            NetSessionID buffId = (NetSessionID) object;
            return (id == buffId.id);
        }
        throw new IllegalArgumentException();
    }

    @Override public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    public int compareTo(NetSessionID buffId) {
        return (int) (id - buffId.id);
    }
}
