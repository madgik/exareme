//package madgik.exareme.master.app.engine;
//
//import java.io.Serializable;
//import java.util.UUID;
//
///**
// * @author alex
// */
//public class AdpDBQueryID implements Comparable<AdpDBQueryID>, Serializable {
//    private static final long serialVersionUID = 1L;
//    private UUID id;
//
//    public AdpDBQueryID() {
//        this.id = UUID.randomUUID();
//    }
//
//    @Override
//    public int compareTo(AdpDBQueryID queryID) {
//        return this.compareTo(queryID);
//    }
//
//    public long getQueryID() {
//        return this.id.getMostSignificantBits();
//    }
//
//    public long getLongId() {
//        return id;
//    }
//
//}

/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

import java.io.Serializable;

/**
 * @author herald
 */
public class AdpDBQueryID implements Comparable<AdpDBQueryID>, Serializable {

    private static final long serialVersionUID = 1L;
    private long id;

    public AdpDBQueryID(long id) {
        this.id = id;
    }

    @Override public int compareTo(AdpDBQueryID queryID) {
        return (int) (id - queryID.id);
    }

    @Override public boolean equals(Object object) {
        if (object instanceof AdpDBQueryID) {
            AdpDBQueryID queryID = (AdpDBQueryID) object;

            return id == queryID.id;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    public long getLongId() {
        return id;
    }

    public long getQueryID() {
        return id;
    }
}


