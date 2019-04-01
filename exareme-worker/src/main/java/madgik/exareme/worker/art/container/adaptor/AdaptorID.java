/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import java.io.Serializable;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class AdaptorID implements Comparable<AdaptorID>, Serializable {

    private static final long serialVersionUID = 1L;
    private long id = -1;

    public AdaptorID(long id) {
        this.id = id;
    }

    @Override
    public int compareTo(AdaptorID adaptorID) {
        return (int) (id - adaptorID.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AdaptorID) {
            AdaptorID adaptorID = (AdaptorID) obj;
            return this.id == adaptorID.id;
        }

        throw new ClassCastException("Cannot cast to AdaptorID");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
