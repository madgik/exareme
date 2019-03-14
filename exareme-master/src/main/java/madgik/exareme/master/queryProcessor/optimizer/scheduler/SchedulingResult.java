/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduler;

import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.ScheduleEstimator;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.ScheduleStatistics;

import java.io.Serializable;
import java.util.List;

/**
 * @author Herald Kllapi
 * @since 1.0
 */
public class SchedulingResult implements Serializable, Comparable<SchedulingResult> {
    private static final long serialVersionUID = 1L;
    public final int containerNum;
    public final RunTimeParameters runTimeParameters;
    public final FinancialProperties financialProperties;
    // TODO: Consider indexing the assignments by op id
    public final List<OperatorAssignment> operatorAssigments;
    public final Exception exception;
    public final ScheduleStatistics statistics;
    public long time_ms = -1;
    public long total_time_ms = -1;

    public SchedulingResult(int containerNum, RunTimeParameters runTimeParameters,
                            FinancialProperties financialProperties, Exception exception, ScheduleEstimator ps) {
        this.containerNum = containerNum;
        this.runTimeParameters = runTimeParameters;
        this.financialProperties = financialProperties;
        this.exception = exception;
        if (ps != null) {
            this.operatorAssigments = ps.getAssignments();
            this.statistics = ps.getScheduleStatistics();
        } else {
            this.operatorAssigments = null;
            this.statistics = null;
        }
    }

    public ScheduleStatistics getStatistics() {
        return statistics;
    }

    @Override
    public int compareTo(SchedulingResult o) {
        return Double.compare(statistics.getTimeInQuanta(), o.statistics.getTimeInQuanta());
    }

    @Override
    public String toString() {
        return "(" + statistics.getTimeInQuanta() + "," + statistics.getMoneyInQuanta() + ")";
    }
}
