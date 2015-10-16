/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.simulatedAnnealing;

/**
 * @author herald
 */
public class LogarithmicTemperature implements Temperature {

    private static final long serialVersionUID = 1L;
    private final double d;

    public LogarithmicTemperature(double d) {
        this.d = d;
    }

    @Override public double getTemperature(int step) {
        return d / Math.log((double) step);
    }
}
