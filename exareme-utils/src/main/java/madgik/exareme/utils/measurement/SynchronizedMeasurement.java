/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.measurement;

/**
 * @author herald
 */
public class SynchronizedMeasurement implements Measurement {

    private static final long serialVersionUID = 1L;
    private final Integer lock = new Integer(0);
    private Measurement m = null;

    public SynchronizedMeasurement(Measurement m) {
        this.m = m;
    }

    public long getActiveValue() {
        synchronized (lock) {
            return m.getActiveValue();
        }
    }

    public long getMaxActiveValue() {
        synchronized (lock) {
            return m.getMaxActiveValue();
        }
    }

    public long getMaxValue() {
        synchronized (lock) {
            return m.getMaxValue();
        }
    }

    public void setMaxValue(long maxValue) {
        synchronized (lock) {
            m.setMaxValue(maxValue);
        }
    }

    public long getMinValue() {
        synchronized (lock) {
            return m.getMinValue();
        }
    }

    public void setMinValue(long minValue) {
        synchronized (lock) {
            m.setMinValue(minValue);
        }
    }

    public String getName() {
        return m.getName();
    }

    public long getTotalSum() {
        synchronized (lock) {
            return m.getTotalSum();
        }
    }

    public void changeActiveValue(long value) {
        synchronized (lock) {
            m.changeActiveValue(value);
        }
    }
}
