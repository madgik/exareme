/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor;

import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.optimizer.SolutionSpace;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.ScheduleStatistics;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
import madgik.exareme.utils.chart.TimeFormat;
import madgik.exareme.utils.chart.TimeUnit;
import madgik.exareme.utils.units.Metrics;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author heraldkllapi
 */
public class ConsoleUtils {
    private static TimeFormat tf = new TimeFormat(TimeUnit.min);

    public static void printStatisticsToLog(String tag, ConcreteQueryGraph graph,
                                            RunTimeParameters runTimeParams, ScheduleStatistics stats, Logger log, Level level) {
        log.log(level, "[" + tag + "] " +
                "G(V=" + graph.getNumOfOperators() +
                ",E=" + graph.getNumOfLinks() + ") Time(" +
                tf.format(
                        (int) (stats.getTimeInQuanta() * runTimeParams.quantum__SEC * Metrics.MiliSec))
                + ")" +
                "$(" + stats.getMoneyInQuanta() + ")");
    }

    public static void printSkylineToLog(ConcreteQueryGraph graph, RunTimeParameters runTimeParams,
                                         SolutionSpace space, Logger log, Level level) {
        for (SchedulingResult sr : space.findSkyline()) {
            printStatisticsToLog("" + sr.getStatistics().getContainersUsed(), graph, runTimeParams,
                    sr.getStatistics(), log, level);
        }
    }
}
