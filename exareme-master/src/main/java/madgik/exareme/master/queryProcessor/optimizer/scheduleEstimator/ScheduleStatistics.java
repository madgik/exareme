/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The schedule statistics.
 *
 * @author herald
 * @since 1.0
 */
public class ScheduleStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DecimalFormat DF = new DecimalFormat("####.##");

    static {
        DF.setMinimumIntegerDigits(5);
        DF.setMinimumFractionDigits(2);
    }

    private final int numOfContainers;
    private final int containerUsed;
    private final int time_SEC;
    private final double time_Quanta;
    private final int money_Quanta;
    private final double money_NoFrag;

    public ScheduleStatistics(int numOfContainers, int containerUsed, int time_SEC,
        double time_Quanta, int money_quanta, double money_NoFrag) {
        this.numOfContainers = numOfContainers;
        this.containerUsed = containerUsed;
        this.time_SEC = time_SEC;
        this.time_Quanta = time_Quanta;
        this.money_Quanta = money_quanta;
        this.money_NoFrag = money_NoFrag;
    }

    public int getNumberOfContainers() {
        return numOfContainers;
    }

    public int getContainersUsed() {
        return containerUsed;
    }

    public int getTime() {
        return time_SEC;
    }

    public double getTimeInQuanta() {
        return time_Quanta;
    }

    public int getMoneyInQuanta() {
        return money_Quanta;
    }

    public double getMoneyNoFragmentation() {
        return money_NoFrag;
    }

    @Override public String toString() {
        return DF.format(money_Quanta) + "\t" + DF.format(time_Quanta);
    }
}
