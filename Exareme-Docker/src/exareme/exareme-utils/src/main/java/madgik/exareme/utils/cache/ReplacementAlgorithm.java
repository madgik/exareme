/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.cache;

import java.io.Serializable;

/**
 * @author herald
 */
public interface ReplacementAlgorithm extends Serializable {

    void insert(long objectNum);

    void delete(long objectNum);

    long getNext();

    void pin(long objectNum);

    void unpin(long objectNum);

    void clear();
}
