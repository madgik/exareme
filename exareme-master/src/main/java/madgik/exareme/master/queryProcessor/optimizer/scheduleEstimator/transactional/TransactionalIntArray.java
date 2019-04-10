/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional;

/**
 * @author heraldkllapi
 */
public class TransactionalIntArray implements TransactionalObject {

    private final int[] value;
    private final int[] commitedValue;
    private boolean changed = false;

    public TransactionalIntArray(int size) {
        value = new int[size];
        commitedValue = new int[size];
    }

    public int[] getValue() {
        return value;
    }

    public int getValue(int idx) {
        return value[idx];
    }

    public void set(int idx, int v) {
        value[idx] = v;
        changed = true;
    }

    @Override
    public void commit() {
        if (changed) {
            System.arraycopy(value, 0, commitedValue, 0, value.length);
            changed = false;
        }
    }

    @Override
    public void rollback() {
        if (changed) {
            System.arraycopy(commitedValue, 0, value, 0, value.length);
            changed = false;
        }
    }
}
