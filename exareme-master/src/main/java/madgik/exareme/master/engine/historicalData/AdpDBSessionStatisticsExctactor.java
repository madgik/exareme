/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.historicalData;

import madgik.exareme.common.art.BufferStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionStatistics;
import madgik.exareme.common.art.PlanSessionStatistics;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * @author heraldkllapi
 */
public class AdpDBSessionStatisticsExctactor {

    private static Logger log = Logger.getLogger(AdpDBSessionStatisticsExctactor.class);
    private PlanSessionStatistics stats = null;
    private HashMap<String, ConcreteOperatorStatistics> opStatsMap =
            new HashMap<String, ConcreteOperatorStatistics>();
    private HashMap<String, BufferStatistics> bufferStatsMap =
            new HashMap<String, BufferStatistics>();

    public AdpDBSessionStatisticsExctactor(PlanSessionStatistics stats) {
        this.stats = stats;
        buildIndexes();
    }

    private void buildIndexes() {
        // Operators
        for (ContainerSessionStatistics contStats : stats.containerStats) {
            for (ConcreteOperatorStatistics opStats : contStats.operators) {
                opStatsMap.put(extractName(opStats.getOperatorName()), opStats);
            }
        }
        // Buffers
        for (ContainerSessionStatistics contStats : stats.containerStats) {
            for (BufferStatistics bufferStats : contStats.buffer) {
                bufferStatsMap.put(extractName(bufferStats.getBufferName()), bufferStats);
            }
        }
    }

    public ConcreteOperatorStatistics getOperatorStats(String operatorName) {
        ConcreteOperatorStatistics opStats = opStatsMap.get(operatorName);
        //    if (opStats == null) {
        //      log.error("Operator stats not found : "
        //              + operatorName + " | " + opStatsMap.keySet());
        //      throw new RuntimeException("Operator not found: " + operatorName);
        //    }
        return opStats;
    }

    public BufferStatistics getBufferStats(String adaptorName, String fromOperator,
                                           String toOperator) {
        BufferStatistics adaptorStats = bufferStatsMap.get("b_" + fromOperator + "_" + toOperator);
        if (adaptorStats == null) {
            log.error("Adaptor stats not found : " + adaptorName + " | " + bufferStatsMap.keySet());
            //      throw new RuntimeException("Adaptor not found: "
            //              + adaptorName + " " + fromOperator + " -> " + toOperator);
        }
        return adaptorStats;
    }

    // The format is <name>.<session>.<try>
    // This method returns <name>
    public String extractName(String opNameWithSessions) {
        System.out.println(opNameWithSessions);
        int end = opNameWithSessions.lastIndexOf(".", opNameWithSessions.lastIndexOf(".") - 1);
        return opNameWithSessions.substring(0, end);
    }
}
