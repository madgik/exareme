/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional;

/**
 * @author herald
 */
public class TransactionalInteger implements TransactionalObject {
    private int value;
    private int commitedValue;

    public TransactionalInteger() {
        this(0);
    }

    public TransactionalInteger(int initialValue) {
        this.value = initialValue;
        this.commitedValue = initialValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int v) {
        value = v;
    }

    public void delta(int delta) {
        value += delta;
    }

    @Override
    public void commit() {
        commitedValue = value;
    }

    @Override
    public void rollback() {
        value = commitedValue;
    }
}
