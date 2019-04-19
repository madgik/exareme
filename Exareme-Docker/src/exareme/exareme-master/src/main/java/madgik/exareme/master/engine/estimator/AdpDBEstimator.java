/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.estimator;

/**
 * @author heraldkllapi
 */
public class AdpDBEstimator {

    // TODO(herald): use simple statistics for this.
    private static final double tablePartitionInputSize_mb = 200;
    private static final double reduceFactor = 0.3;

    private static double getEstimatedSizeOfTable(int level) {
        double reduceBy = Math.pow(reduceFactor, level);
        return tablePartitionInputSize_mb * reduceBy;
    }
}
