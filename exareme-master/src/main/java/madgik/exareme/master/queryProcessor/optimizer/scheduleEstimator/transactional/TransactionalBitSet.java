/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional;

import java.util.BitSet;

/**
 * @author herald
 */
public class TransactionalBitSet implements TransactionalObject {
    private final BitSet value;
    private final BitSet commitedValue;
    private boolean changed = false;

    public TransactionalBitSet() {
        value = new BitSet();
        commitedValue = new BitSet();
    }

    public TransactionalBitSet(int size) {
        value = new BitSet(size);
        commitedValue = new BitSet(size);
    }

    public BitSet getValue() {
        return value;
    }

    public int cardinality() {
        return value.cardinality();
    }

    public void set(int idx) {
        value.set(idx);
        changed = true;
    }

    public void set(int fromIdx, int toIdx) {
        value.set(fromIdx, toIdx);
        changed = true;
    }

    @Override public void commit() {
        if (changed) {
            commitedValue.clear();
            commitedValue.or(value);
            changed = false;
        }
    }

    @Override public void rollback() {
        if (changed) {
            value.clear();
            value.or(commitedValue);
            changed = false;
        }
    }
}
