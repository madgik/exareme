/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.measurement;

/**
 * @author herald
 */
public class MeasurementFactory {

    private MeasurementFactory() {
    }

    public static Measurement createMeasurement(String name) {
        return new SynchronizedMeasurement(new MeasurementImpl(name));
    }
}
