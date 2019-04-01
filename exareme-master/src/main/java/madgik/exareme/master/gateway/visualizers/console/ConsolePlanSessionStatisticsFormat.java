/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.gateway.visualizers.console;

import madgik.exareme.common.art.*;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.graph.ConcreteGraphStatistics;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.utils.chart.DataFormat;
import madgik.exareme.utils.chart.DataUnit;
import madgik.exareme.utils.chart.TimeFormat;
import madgik.exareme.utils.chart.TimeUnit;
import madgik.exareme.utils.units.Metrics;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

//import madgik.exareme.db.art.container.bufferPoolMgr.BufferPoolStatistics;


/**
 * @author herald
 */
public class ConsolePlanSessionStatisticsFormat {

    DecimalFormat decimalF = new DecimalFormat("#####.##");
    DecimalFormat percentF = new DecimalFormat("###.##");
    TimeFormat timeF = new TimeFormat(TimeUnit.min);
    DataFormat dataF = new DataFormat(DataUnit.MB, decimalF);
    DecimalFormat numberFormat = new DecimalFormat("#####");

    public ConsolePlanSessionStatisticsFormat() {
        decimalF.setMinimumFractionDigits(2);
        decimalF.setMaximumFractionDigits(2);
        percentF.setMinimumFractionDigits(2);
        percentF.setMaximumFractionDigits(2);
        numberFormat.setGroupingSize(3);
    }

    public String format(PlanSessionStatistics stats, ConcreteQueryGraph graph) {
        return format(stats, graph, new RunTimeParameters());
    }

    public String format(PlanSessionStatistics stats, ConcreteQueryGraph graph,
                         RunTimeParameters params) {
        long totalUserTime = 0;
        long totalSystemTime = 0;

        long totalCpuUserTime = 0;
        long totalCpuSystemTime = 0;

        double money = stats.computeMoney(params, true);
        double moneyNoFrag = stats.computeMoney(params, false);

        long totalDataRead = 0;
        long totalDataWrite = 0;
        long networkData = 0;
        long localData = 0;

        long bufferPoolData = 0;
        long bufferPoolRead = 0;
        long bufferPoolWrite = 0;

        long bufferCount = 0;
        long linkCount = 0;
        long localLinkCount = 0;
        long remoteLinkCount = 0;

        long controlMessagesCount = stats.controlMessagesCount();
        long independentMsgs = stats.independentMessages();
        long maxEventProcessTime = stats.maxEventProcessTime();
        long maxWaitTime = stats.maxWaitTime();
        long totalEventProcTime = stats.totalEventProcessTime();
        long maxParallelMsgs = stats.maxIndependentMsgCount();

        // Input / Time / Output
        OpCategoryStats uncategorized = new OpCategoryStats("Default", stats.containerStats);
        HashMap<String, OpCategoryStats> categoryStats = new HashMap<String, OpCategoryStats>();

        for (ContainerSessionStatistics cStats : stats.containerStats) {
            for (ConcreteOperatorStatistics opStats : cStats.operators) {
                totalUserTime += opStats.getTotalTime_ms();
                totalSystemTime += opStats.getSystemTime_ms();

                totalCpuUserTime += opStats.getTotalCpuTime_ms();
                totalCpuSystemTime += opStats.getSystemCpuTime_ms();

                String opCategory = opStats.getOperatorCategory();
                if (opCategory == null) {
                    uncategorized.update(opStats);
                } else {
                    OpCategoryStats cs = categoryStats.get(opCategory);
                    if (cs == null) {
                        cs = new OpCategoryStats(opCategory, stats.containerStats);
                        categoryStats.put(opCategory, cs);
                    }
                    cs.update(opStats);
                }
            }

            for (BufferStatistics bStats : cStats.buffer) {
                totalDataRead += bStats.getDataRead();
                totalDataWrite += bStats.getDataWrite();
                bufferCount++;
            }

            for (AdaptorStatistics aStats : cStats.adaptors) {
                if (aStats.isRemote()) {
                    networkData += aStats.getBytes();
                    remoteLinkCount++;
                } else {
                    localData += aStats.getBytes();
                    localLinkCount++;
                }
                linkCount++;
            }

            //      for (BufferPoolStatistics st : cStats.bufferPool) {
            //        bufferPoolData += st.getData();
            //        bufferPoolRead += st.getBytesRead();
            //        bufferPoolWrite += st.getBytesWrite();
            //      }
        }

        StringBuilder sb = new StringBuilder(1024);

        sb.append("\n");
        sb.append(" -- Session " + stats.sessionID.getLongId() + " statistics -- \n");
        sb.append("Operators           : " + numFormat(stats.operatorCompleted()) + "\n");
        sb.append("Buffers             : " + numFormat(stats.buffersCreated()) +
                " (" + bufferCount + ")" + "\n");
        sb.append("Links               : " + numFormat(stats.linksCreated()) +
                " (" + linkCount + " : " + localLinkCount + "+" + remoteLinkCount + ")" + "\n");
        sb.append(" -- -- \n");

        if (graph != null) {
            ConcreteGraphStatistics gStats = new ConcreteGraphStatistics(graph, params);
            sb.append("Work                : [" +
                    dFormat(gStats.getMinWork()) + ", " + dFormat(gStats.getMaxWork()) + "]\n");
            sb.append("Span                : [" +
                    dFormat(gStats.getMinSpan()) + ", " + dFormat(gStats.getMaxSpan()) + "]\n");
            sb.append("Parallelism         : [" +
                    dFormat(gStats.getMinP()) + ", " + dFormat(gStats.getMaxP()) + "]\n");
            sb.append(" -- -- \n");
        }

        sb.append("Message Phases      : " + numFormat(independentMsgs) + "\n");
        sb.append("Messages            : " + numFormat(controlMessagesCount) + "\n");
        sb.append("Ind Msg Count       : " + numFormat(maxParallelMsgs) + "\n");
        sb.append(" -- -- \n");

        sb.append("Total Time          : " + numFormat(totalEventProcTime) + "\n");
        sb.append("Max Wait Time       : " + numFormat(maxWaitTime) + "\n");
        sb.append("Max Time            : " + numFormat(maxEventProcessTime) + "\n");
        sb.append(
                "Avg Time            : " + numFormat(totalEventProcTime / independentMsgs) + "\n");
        sb.append(" -- -- \n");

        sb.append(
                "Exec Time           : " + timeFormat(stats.endTime() - stats.startTime()) + "\n");
        sb.append("Total Time          : " + timeFormat(totalUserTime) + "\n");
        sb.append("Total Time(CPU)     : " + timeFormat(totalCpuUserTime) + "\n");
        sb.append("Speedup             : " + percentageFormat(
                (double) totalUserTime / (stats.endTime() - stats.startTime())) + "\n");
        sb.append(" -- -- \n");

        double time = ((double) (stats.endTime() - stats.startTime()) / Metrics.MiliSec)
                / params.quantum__SEC;
        sb.append("Time                : " + quantaFormat(time) + "\n");
        sb.append("Money               : " + quantaFormat(money) + "\n");
        sb.append("Money no frag       : " + quantaFormat(moneyNoFrag) + "\n");
        sb.append(" -- -- \n");

        sb.append("System Time         : " + timeFormat(totalSystemTime) + "\n");
        sb.append("System Time(CPU)    : " + timeFormat(totalCpuSystemTime) + "\n");
        sb.append(
                "Percentage          : " + percentageFormat((double) totalSystemTime / totalUserTime)
                        + "\n");
        sb.append(" -- -- \n");

        sb.append("Total Data Pipes    : " + dataFormat(totalDataRead + totalDataWrite) + "\n");
        sb.append("Total Data R        : " + dataFormat(totalDataRead) + "\n");
        sb.append("Total Data W        : " + dataFormat(totalDataWrite) + "\n");
        sb.append(" -- -- \n");

        sb.append("Local Data          : " + dataFormat(localData) + "\n");
        sb.append("Net Data            : " + dataFormat(networkData) + "\n");
        sb.append("Percentage          : " + percentageFormat(
                (double) networkData / (totalDataRead + totalDataWrite)) + "\n");
        sb.append(" -- -- \n");

        sb.append("Pool Data           : " + dataFormat(bufferPoolData) + "\n");
        sb.append("Pool Data R         : " + dataFormat(bufferPoolRead) + "\n");
        sb.append("Pool Data W         : " + dataFormat(bufferPoolWrite) + "\n");
        sb.append(" -- -- \n");

        //    sb.append(uncategorized.getStats() + "\n");
        //    LinkedList<String> categories = new LinkedList<String>(categoryStats.keySet());
        //    Collections.sort(categories);
        //    for (String cat : categories) {
        //      sb.append(categoryStats.get(cat).getStats() + "\n");
        //    }
        //    sb.append(" -- -- \n");

        return sb.toString();
    }

    private String quantaFormat(double value) {
        return String.format("%9s", decimalF.format(value));
    }

    private String numFormat(long num) {
        return String.format("%6s", numberFormat.format(num));
    }

    private String dFormat(double num) {
        return String.format("%9s", decimalF.format(num));
    }

    private String dataFormat(long size_MB) {
        return String.format("%9s", dataF.format(size_MB)) + " MB \t" + size_MB;
    }

    private String timeFormat(long time_MS) {
        return String.format("%9s", timeF.format(time_MS)) + " m \t" + time_MS;
    }

    private String percentageFormat(double value) {
        return String.format("%9s", percentF.format(value)) + " %";
    }


    private class OpCategoryStats {
        String category = null;
        DescriptiveStatistics time = null;
        DescriptiveStatistics input = null;
        DescriptiveStatistics output = null;
        List<ContainerSessionStatistics> stats = null;

        OpCategoryStats(String category, List<ContainerSessionStatistics> stats) {
            this.category = category;
            this.time = new DescriptiveStatistics();
            this.input = new DescriptiveStatistics();
            this.output = new DescriptiveStatistics();
            this.stats = stats;
        }

        void update(ConcreteOperatorStatistics opStats) {
            double inputBytes = 0.0;
            double outputBytes = 0.0;

            for (ContainerSessionStatistics css : stats) {
                for (AdaptorStatistics as : css.adaptors) {
                    if (as.getTo().equals(opStats.getOperatorName())) {
                        inputBytes += as.getBytes();
                    }

                    if (as.getFrom().equals(opStats.getOperatorName())) {
                        outputBytes += as.getBytes();
                    }
                }
            }

            time.addValue(opStats.getTotalTime_ms());
            input.addValue(inputBytes);
            output.addValue(outputBytes);
        }

        String getStats() {
            String n = "" + time.getN();

            String meanTime = String.format("%9s", timeF.format((long) time.getMean()));
            String minTime = timeF.format((long) time.getMin());
            String maxTime = timeF.format((long) time.getMax());
            String stdevTime =
                    String.format("%s", timeF.format((long) time.getStandardDeviation()));

            String meanIn = String.format("%9s", dataF.format((long) input.getMean()));
            String minIn = dataF.format((long) input.getMin());
            String maxIn = dataF.format((long) input.getMax());
            String stdevIn = String.format("%s", dataF.format((long) input.getStandardDeviation()));

            String meanOut = String.format("%9s", dataF.format((long) output.getMean()));
            String minOut = dataF.format((long) output.getMin());
            String maxOut = dataF.format((long) output.getMax());
            String stdevOut =
                    String.format("%s", dataF.format((long) output.getStandardDeviation()));

            return String.format("%-26s", category) + " : " +
                    n + "\t" +
                    meanTime + "\t" + "[" + minTime + ", " + maxTime + "]@" + stdevTime + " s\t" +
                    meanIn + "\t" + "[" + minIn + ", " + maxIn + "]@" + stdevIn + " MB\t" +
                    meanOut + "\t" + "[" + minOut + ", " + maxOut + "]@" + stdevOut + " MB\t";
        }
    }
}
