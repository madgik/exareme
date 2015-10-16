/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art;

import java.io.Serializable;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class PlanSessionID implements Comparable<PlanSessionID>, Serializable {

    private static final long serialVersionUID = 1L;
    private long id;

    public PlanSessionID(long id) {
        this.id = id;
    }

    @Override public int compareTo(PlanSessionID sessionID) {
        return (int) (id - sessionID.id);
    }

    @Override public boolean equals(Object object) {
        if (object instanceof PlanSessionID) {
            PlanSessionID sessionID = (PlanSessionID) object;
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
