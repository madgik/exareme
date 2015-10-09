/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.historicalData;

import madgik.exareme.common.art.BufferStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.PlanSessionStatistics;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.Link;
import madgik.exareme.master.queryProcessor.graph.LinkData;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
import madgik.exareme.utils.string.StringUtils;
import madgik.exareme.utils.units.Metrics;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author heraldkllapi
 */
public class AdpDBHistoricalQueryData implements Serializable {

    private static Logger log = Logger.getLogger(AdpDBHistoricalQueryData.class);
    private AdpDBQueryExecutionPlan plan = null;
    private PlanSessionStatistics stats = null;
    // TODO remove categorymap and calculate from the op+link maps
    private Map<String, String> categorymap = null;
    private HashMap<String, ConcreteOperator> operatorMap = new HashMap<String, ConcreteOperator>();
    private HashMap<String, LinkData> linkDataMap = new HashMap<String, LinkData>();

    public AdpDBHistoricalQueryData() {
    }

    public AdpDBHistoricalQueryData(AdpDBQueryExecutionPlan plan, PlanSessionStatistics stats) {
        this.plan = plan;
        this.stats = stats;
        extractStats();
    }

    private void extractStats() {
        log.info("Extracting statistics ...");
        AdpDBSessionStatisticsExctactor extractor = new AdpDBSessionStatisticsExctactor(stats);
        extractOperatorStats(extractor);
        extractLinkStats(extractor);
    }

    private void extractOperatorStats(AdpDBSessionStatisticsExctactor extractor) {
        ConcreteQueryGraph graph = plan.getGraph();
        for (ConcreteOperator op : graph.getOperators()) {
            ConcreteOperatorStatistics opStats = extractor.getOperatorStats(op.operatorName);
            if (opStats == null)
                continue;
            ConcreteOperator newOp = new ConcreteOperator(op.operatorName,
                (double) opStats.getTotalTime_ms() / Metrics.MiliSec, op.cpuUtilization,
                op.memory_MB, op.behavior);
            log.debug("Operator : '" + newOp.operatorName + "' ...");
            double runTimeDiff = 100.0 * Math.abs(newOp.runTime_SEC - op.runTime_SEC) / Math
                .max(newOp.runTime_SEC, op.runTime_SEC);
            log.debug(
                "    Time : " + newOp.runTime_SEC + " old[" + op.runTime_SEC + "] -> " + runTimeDiff
                    + "%");
            double cpuUtilDiff = 100.0 * Math.abs(newOp.cpuUtilization - op.cpuUtilization) / Math
                .max(newOp.cpuUtilization, op.cpuUtilization);
            log.debug("     CPU : " + newOp.cpuUtilization + " old[" + op.cpuUtilization + "] -> "
                + cpuUtilDiff + "%");
            operatorMap.put(newOp.operatorName, newOp);
        }
        log.info("Operators : " + operatorMap.size());
    }

    private void extractLinkStats(AdpDBSessionStatisticsExctactor extractor) {
        ConcreteQueryGraph graph = plan.getGraph();
        for (Link link : graph.getLinks()) {
            log.debug("Link   : '" + link.from.operatorName + "[" + link.data.name + "]"
                + link.to.operatorName + "' ...");
            BufferStatistics adaptorStats = extractor
                .getBufferStats(link.data.name, link.from.operatorName, link.to.operatorName);
            LinkData newLinkData =
                new LinkData(link.data.name, (double) adaptorStats.getDataRead() / Metrics.MB);
            double dataDiff = 100.0 * Math.abs(newLinkData.size_MB - link.data.size_MB) / Math
                .max(newLinkData.size_MB, link.data.size_MB);
            log.debug(
                "  Data : " + newLinkData.size_MB + " old[" + link.data.size_MB + "] -> " + dataDiff
                    + "%");

            linkDataMap
                .put(StringUtils.concatenateUnique(link.from.operatorName, link.to.operatorName),
                    newLinkData);
        }
        log.info("Links : " + linkDataMap.size());
    }

    public void combineWith(AdpDBHistoricalQueryData other) {
        if (this == other) {
            throw new RuntimeException("Cannot combine with itself");
        }
        // TODO(herald): For now, use the latest
        this.plan = other.plan;
        this.stats = other.stats;
        this.operatorMap = other.operatorMap;
        this.linkDataMap = other.linkDataMap;
    }

    public ConcreteOperator getOperator(String operatorName) {
        ConcreteOperator op = operatorMap.get(operatorName);
        if (op == null) {
            return null;
        }
        return new ConcreteOperator(operatorName, op.runTime_SEC, op.cpuUtilization, op.memory_MB,
            op.behavior);
    }

    public LinkData getLinkData(String name, String fromOperator, String toOperator) {
        LinkData data = linkDataMap.get(StringUtils.concatenateUnique(fromOperator, toOperator));
        if (data == null) {
            return null;
        }
        return new LinkData(data);
    }

    public SchedulingResult getSchedulingResult() {
        return plan.getSchedulingResult();
    }

    public PlanSessionStatistics getPlanSessionStatistics() {
        return stats;
    }


    public Map<String, String> getCategorymap() {
        return categorymap;
    }

    public void setCategorymap(Map<String, String> categorymap) {
        this.categorymap = categorymap;
    }

    public AdpDBQueryExecutionPlan getPlan() {
        return plan;
    }
}
