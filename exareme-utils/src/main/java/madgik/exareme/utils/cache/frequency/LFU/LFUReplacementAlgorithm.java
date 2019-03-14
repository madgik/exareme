/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.cache.frequency.LFU;

import madgik.exareme.utils.cache.ReplacementAlgorithm;

/**
 * @author herald
 */
public class LFUReplacementAlgorithm implements ReplacementAlgorithm {

    private static final long serialVersionUID = 1L;

    @Override
    public void insert(long objectNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getNext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pin(long objectNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unpin(long objectNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(long objectNum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
