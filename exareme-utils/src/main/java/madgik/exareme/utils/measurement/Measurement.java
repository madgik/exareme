/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.measurement;

import java.io.Serializable;

/**
 * @author herald
 */
public interface Measurement extends Serializable {

    long getActiveValue();

    long getMaxActiveValue();

    long getMaxValue();

    void setMaxValue(long maxValue);

    long getMinValue();

    void setMinValue(long minValue);

    String getName();

    long getTotalSum();

    void changeActiveValue(long value);
}
