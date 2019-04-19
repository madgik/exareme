/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.optimizer;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

/**
 * @author Herald Kllapi <br>
 * @since 1.0
 */
public class ConcreteQueryGraphStatistics {
    private static final Logger LOG = Logger.getLogger(ConcreteQueryGraphStatistics.class);
    public double totalDataOut = 0.0;
    public double totalRunTime = 0.0;
    public double[] dataOut = null;
    public double[] runTime = null;
    public DescriptiveStatistics dataOutStats = null;
    public DescriptiveStatistics runTimeStats = null;

    public void calcStats() {
        dataOutStats = new DescriptiveStatistics();
        runTimeStats = new DescriptiveStatistics();
        for (double out : dataOut) {
            dataOutStats.addValue(out);
        }
        for (double time : runTime) {
            runTimeStats.addValue(time);
        }
    }

    public void printStats(double networkSpeed) {
        LOG.debug("----------- " + networkSpeed + " -----------");
        LOG.debug("MeanOut : " + this.dataOutStats.getMean());
        LOG.debug("StdOut : " + this.dataOutStats.getStandardDeviation());
        LOG.debug("-----------");
        LOG.debug("MeanTime : " + this.runTimeStats.getMean());
        LOG.debug("StdTime : " + this.runTimeStats.getStandardDeviation());
        LOG.debug("-----------");
        LOG.debug("OPTime/DOTime : " + (this.totalRunTime / (this.totalDataOut / networkSpeed)));
        LOG.debug("----------------------");
    }
}
