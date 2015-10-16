/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.cache.time;

import madgik.exareme.utils.association.Triple;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * @author herald
 */
public class MRUComparator implements Comparator<Triple<Long, BigInteger, Boolean>>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Triple<Long, BigInteger, Boolean> o1, Triple<Long, BigInteger, Boolean> o2) {
        return o2.b.compareTo(o1.b);
    }
}
