/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.measurement;

/**
 * @author herald
 */
public class MeasurementImpl implements Measurement {

    private static final long serialVersionUID = 1L;
    private String name = null;
    private long maxValue = 0;
    private long minValue = 0;
    private long activeValue = 0;
    private long maxActiveValue = 0;
    private long totalSum = 0;

    public MeasurementImpl(String name) {
        this.name = name;
    }

    public long getActiveValue() {
        return activeValue;
    }

    public long getMaxActiveValue() {
        return maxActiveValue;
    }

    public long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }

    public long getMinValue() {
        return minValue;
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public String getName() {
        return name;
    }

    public long getTotalSum() {
        return totalSum;
    }

    public void changeActiveValue(long value) {
        this.activeValue += value;

        if (value > 0) {
            this.totalSum += value;
            if (this.maxActiveValue < this.activeValue) {
                this.maxActiveValue = this.activeValue;
            }
        }
    }
}
