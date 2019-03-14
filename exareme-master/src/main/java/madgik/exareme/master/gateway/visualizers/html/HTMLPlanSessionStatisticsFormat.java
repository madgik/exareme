/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.gateway.visualizers.html;

import com.google.gson.Gson;
import madgik.exareme.common.art.*;
import madgik.exareme.common.optimizer.OperatorCategory;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.SchedulingResult;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.chart.DataFormat;
import madgik.exareme.utils.chart.DataUnit;
import madgik.exareme.utils.chart.TimeFormat;
import madgik.exareme.utils.chart.TimeUnit;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.utils.histogram.Bucket;
import madgik.exareme.utils.histogram.Histogram;
import madgik.exareme.utils.histogram.partitionRule.PartitionClass;
import madgik.exareme.utils.histogram.partitionRule.PartitionConstraint;
import madgik.exareme.utils.histogram.partitionRule.PartitionRule;
import madgik.exareme.utils.units.Metrics;
import madgik.exareme.utils.zip.ZipUtil;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

//import madgik.exareme.db.art.container.bufferPoolMgr.BufferPoolStatistics;


/**
 * @author herald
 */
public class HTMLPlanSessionStatisticsFormat {

    private DecimalFormat decimalF = new DecimalFormat("#####.##");
    private DecimalFormat percentF = new DecimalFormat("###.##");
    private TimeFormat timeF = new TimeFormat(TimeUnit.min);
    private DataFormat dataF = new DataFormat(DataUnit.MB, decimalF);
    private long minTime = Long.MAX_VALUE;
    private long maxTime = 0;

    public HTMLPlanSessionStatisticsFormat() {
        decimalF.setMinimumFractionDigits(2);
        decimalF.setMaximumFractionDigits(2);
        percentF.setMinimumFractionDigits(2);
        percentF.setMaximumFractionDigits(2);
    }

    public void format(SchedulingResult schedulingResult, PlanSessionStatistics stats,
                       Map<String, String> categoryToolMap, File folder, int numberOfBuckets) throws Exception {
        long totalTime = 0;
        long totalProcessingTime = 0;
        long totalSystemTime = 0;

        long totalCpuUserTime = 0;
        long totalCpuSystemTime = 0;

        long pipeData = 0;
        long networkData = 0;
        long localData = 0;

        long bufferPoolData = 0;

        long bufferCount = 0;
        long linkCount = 0;
        long localLinkCount = 0;
        long remoteLinkCount = 0;

        // Input / Time / Output
        OpCategoryStats uncategorized = new OpCategoryStats("Default", stats.containerStats);
        HashMap<String, OpCategoryStats> categoryStats = new HashMap<String, OpCategoryStats>();

        for (ContainerSessionStatistics cStats : stats.containerStats) {
            for (ConcreteOperatorStatistics opStats : cStats.operators) {
                if (opStats.getOperatorType() == OperatorType.processing) {
                    totalProcessingTime += opStats.getTotalTime_ms();
                }

                totalTime += opStats.getTotalTime_ms();
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
                pipeData += bStats.getDataRead();
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
            //      }
        }

        // Data file
        StringBuilder data = new StringBuilder(1024);

        // JSON converter
        Gson gson = new Gson();

        // Session ID
        data.append("sessionID = " + gson.toJson(stats.sessionID.getLongId()) + ";\n");

        // Time Data
        {
            data.append("end = " + gson.toJson((maxTime - minTime) / Metrics.MiliSec) + ";\n");

            Map timeData = new HashMap<String, List<Object>>();

            { // aoColumns
                List<Map<String, String>> aoColumns = new ArrayList<Map<String, String>>();
                aoColumns.add(makeColumn("Measure", false));
                aoColumns.add(makeColumn("Time", true));

                timeData.put("aoColumns", aoColumns);
            }

            { // aaData
                List<String[]> aaData = new ArrayList<String[]>();
                aaData.add(new String[]{"Total",
                        timeF.format(schedulingResult.time_ms + stats.endTime() - stats.startTime())});

                aaData.add(new String[]{"Optimization", timeF.format(schedulingResult.time_ms)});
                aaData.add(new String[]{"Total Optimization",
                        timeF.format(schedulingResult.total_time_ms)});

                aaData.add(
                        new String[]{"Execution", timeF.format(stats.endTime() - stats.startTime())});
                aaData.add(new String[]{"Total Execution", timeF.format(totalProcessingTime)});

                aaData.add(new String[]{"Execution (CPU)", timeF.format(totalCpuUserTime)});
                aaData.add(new String[]{"System", timeF.format(totalSystemTime)});
                aaData.add(new String[]{"System (CPU)", timeF.format(totalCpuSystemTime)});

                double speedup =
                        (double) totalProcessingTime / (stats.endTime() - stats.startTime());
                aaData.add(new String[]{"Speedup", decimalF.format(speedup)});

                timeData.put("aaData", aaData);
            }

            data.append("timeData = " + gson.toJson(timeData) + ";\n");
        }

        // Time Chart
        {
            ArrayList<Double> time = new ArrayList<Double>();
            time.add((double) (schedulingResult.time_ms + stats.endTime() - stats.startTime())
                    / Metrics.MiliSec);
            time.add((double) totalProcessingTime / Metrics.MiliSec);

            List<List<Double>> timeChart = new ArrayList<List<Double>>();
            timeChart.add(time);

            data.append("timeChart = " + gson.toJson(timeChart) + ";\n");
        }

        { // Opt Time Chart
            ArrayList<Double> time = new ArrayList<Double>();

            time.add((double) (schedulingResult.time_ms) / Metrics.MiliSec);
            time.add((double) (stats.endTime() - stats.startTime()) / Metrics.MiliSec);

            List<List<Double>> optTimeChart = new ArrayList<List<Double>>();
            optTimeChart.add(time);

            data.append("optTimeChart = " + gson.toJson(optTimeChart) + ";\n");
        }

        { // Opt Pie Chart
            List optPieChart = new ArrayList();

            ArrayList execution = new ArrayList();
            execution.add("Exec");
            execution.add(stats.endTime() - stats.startTime());

            ArrayList optimization = new ArrayList();
            optimization.add("Opt");
            optimization.add(schedulingResult.time_ms);

            optPieChart.add(optimization);
            optPieChart.add(execution);

            data.append("optPieChart = " + gson.toJson(optPieChart) + ";\n\n");
        }

        // Dataflow
        {
            Map dataflowData = new HashMap<String, List<Object>>();

            { // aoColumns
                List<Map<String, String>> aoColumns = new ArrayList<Map<String, String>>();
                aoColumns.add(makeColumn("Type", false));
                aoColumns.add(makeColumn("Count", true));

                dataflowData.put("aoColumns", aoColumns);
            }

            { // aaData
                List<String[]> aaData = new ArrayList<String[]>();
                aaData.add(new String[]{"Operators", stats.operatorCompleted() + ""});
                aaData.add(new String[]{"Buffers", stats.buffersCreated() + ""});
                aaData.add(new String[]{"Links", stats.linksCreated() + ""});

                dataflowData.put("aaData", aaData);
            }

            data.append("dataflowData = " + gson.toJson(dataflowData) + ";\n");
        }

        // Dataflow Chart
        {
            List dataflowChart = new ArrayList();

            ArrayList local = new ArrayList();
            local.add("Local");
            local.add(localLinkCount);

            ArrayList remote = new ArrayList();
            remote.add("Remote");
            remote.add(remoteLinkCount);

            dataflowChart.add(local);
            dataflowChart.add(remote);

            data.append("dataflowChart = " + gson.toJson(dataflowChart) + ";\n\n");
        }

        { // Data
            Map dataData = new HashMap<String, List<Object>>();
            { // aoColumns
                List<Map<String, String>> aoColumns = new ArrayList<Map<String, String>>();
                aoColumns.add(makeColumn("Type", false));
                aoColumns.add(makeColumn("Data (MB)", true));

                dataData.put("aoColumns", aoColumns);
            }
            { // aaData
                List<String[]> aaData = new ArrayList<String[]>();
                aaData.add(new String[]{"Pipes", dataF.format(pipeData) + ""});
                aaData.add(new String[]{"Pool Data", dataF.format(bufferPoolData) + ""});
                aaData.add(new String[]{"Local Data", dataF.format(localData) + ""});
                aaData.add(new String[]{"Net Data", dataF.format(networkData) + ""});

                dataData.put("aaData", aaData);
            }
            data.append("dataData = " + gson.toJson(dataData) + ";\n");
        }

        { // Data Chart
            List dataChart = new ArrayList<List<String>>();

            ArrayList local = new ArrayList();
            local.add("Local");
            local.add(localData);

            ArrayList net = new ArrayList();
            net.add("Net");
            net.add(networkData);

            dataChart.add(local);
            dataChart.add(net);

            data.append("dataChart = " + gson.toJson(dataChart) + ";\n\n");
        }

        { // Categories
            Map categoriesData = new HashMap<String, List<Object>>();

            { // aoColumns
                List<Map<String, String>> aoColumns = new ArrayList<Map<String, String>>();
                aoColumns.add(makeColumn("ID", true));
                aoColumns.add(makeColumn("Type", true));
                aoColumns.add(makeColumn("Name", false));
                aoColumns.add(makeColumn("Count", true));

                aoColumns.add(makeColumn("Start", true));
                aoColumns.add(makeColumn("Start M-M", false));
                aoColumns.add(makeColumn("Start H", false));

                aoColumns.add(makeColumn("Time", true));
                aoColumns.add(makeColumn("Time M-M", false));
                aoColumns.add(makeColumn("Time H", false));

                aoColumns.add(makeColumn("In(MB)", true));
                aoColumns.add(makeColumn("In M-M", false));
                aoColumns.add(makeColumn("In Hist", false));

                aoColumns.add(makeColumn("Out(MB)", true));
                aoColumns.add(makeColumn("Out M-M", false));
                aoColumns.add(makeColumn("Out H", false));

                categoriesData.put("aoColumns", aoColumns);
            }

            ArrayList<String> categories = new ArrayList<String>(categoryStats.keySet());
            int uncategorizedID = categories.size() + 1;
            Collections.sort(categories);

            { // aaData
                List<String[]> aaData = new ArrayList<String[]>();
                for (int catID = 0; catID < categories.size(); catID++) {
                    OpCategoryStats catStats = categoryStats.get(categories.get(catID));

                    String tooltip = categoryToolMap.get(catStats.category);
                    if (tooltip != null) {
                        tooltip = tooltip.replaceAll("\\n", "<br>");
                    }

                    aaData.add(catStats.getStats(catID + 1, findType(catStats.category), tooltip,
                            numberOfBuckets));
                }
                aaData.add(uncategorized
                        .getStats(uncategorizedID, CategoryType.exec, null, numberOfBuckets));

                categoriesData.put("aaData", aaData);
            }

            data.append("categoriesData = " + gson.toJson(categoriesData) + ";\n");

            { // categoriesTimeChart
                List time = new LinkedList();
                for (int catID = 0; catID < categories.size(); catID++) {
                    OpCategoryStats catStats = categoryStats.get(categories.get(catID));
                    time.add(new Object[]{catStats.time.getMean() / Metrics.MiliSec,
                            catStats.category});
                }

                double timeMean = 0.0;
                if (uncategorized.time.getN() != 0) {
                    timeMean = uncategorized.time.getMean();
                }
                time.add(new Object[]{timeMean / Metrics.MiliSec, uncategorized.category});

                List categoriesTimeChart = new LinkedList();
                categoriesTimeChart.add(time);

                data.append("categoriesTimeChart = " + gson.toJson(categoriesTimeChart) + ";\n");
            }

            { // categoriesThroughputChart
                List input = new LinkedList();
                List output = new LinkedList();

                for (int catID = 0; catID < categories.size(); catID++) {
                    OpCategoryStats catStats = categoryStats.get(categories.get(catID));
                    double meanTime = catStats.time.getMean() / Metrics.MiliSec;
                    double inputMean = catStats.input.getMean() / Metrics.MB;
                    double outputMean = catStats.output.getMean() / Metrics.MB;

                    input.add(new Object[]{inputMean / meanTime, catStats.category});
                    output.add(new Object[]{outputMean / meanTime, catStats.category});
                }

                double inputMeanThroughtput = 0.0;
                double outputMeanThroughtput = 0.0;

                if (uncategorized.time.getN() != 0) {
                    double meanTime = uncategorized.time.getMean() / Metrics.MiliSec;
                    inputMeanThroughtput = (uncategorized.input.getMean() / Metrics.MB) / meanTime;
                    outputMeanThroughtput =
                            (uncategorized.output.getMean() / Metrics.MB) / meanTime;
                }

                input.add(new Object[]{inputMeanThroughtput, uncategorized.category});
                output.add(new Object[]{outputMeanThroughtput, uncategorized.category});

                List categoriesThroughputChart = new LinkedList();
                categoriesThroughputChart.add(input);
                categoriesThroughputChart.add(output);

                data.append("categoriesThroughputChart = " + gson.toJson(categoriesThroughputChart)
                        + ";\n");
            }

            { // categoriesDataChart
                List input = new LinkedList();
                List output = new LinkedList();

                for (int catID = 0; catID < categories.size(); catID++) {
                    OpCategoryStats catStats = categoryStats.get(categories.get(catID));

                    input.add(
                            new Object[]{catStats.input.getMean() / Metrics.MB, catStats.category});
                    output.add(
                            new Object[]{catStats.output.getMean() / Metrics.MB, catStats.category});
                }

                double inputMean = 0.0;
                double outputMean = 0.0;
                if (uncategorized.input.getN() != 0) {
                    inputMean = uncategorized.input.getMean() / Metrics.MB;
                    outputMean = uncategorized.output.getMean() / Metrics.MB;
                }

                input.add(new Object[]{inputMean, uncategorized.category});
                output.add(new Object[]{outputMean, uncategorized.category});

                List categoriesDataChart = new LinkedList();
                categoriesDataChart.add(input);
                categoriesDataChart.add(output);

                data.append("categoriesDataChart = " + gson.toJson(categoriesDataChart) + ";\n");
            }

            { // categoriesGanttChart
                List offset = new LinkedList();

                List inDuration = new LinkedList();
                List outDuration = new LinkedList();
                List fromDuration = new LinkedList();
                List toDuration = new LinkedList();
                List execDuration = new LinkedList();

                for (int catID = 0; catID < categories.size(); catID++) {
                    OpCategoryStats catStats = categoryStats.get(categories.get(catID));
                    double offsetDur = (catStats.start.getMin() - minTime) / Metrics.MiliSec;
                    double dur =
                            (catStats.end.getMax() - catStats.start.getMin()) / Metrics.MiliSec;

                    offset.add(new Object[]{offsetDur, catID + 1});
                    CategoryType type = findType(catStats.category);

                    set(catID + 1, dur, type.ordinal(), execDuration, inDuration, outDuration,
                            fromDuration, toDuration);
                }

                double offsetDur = 0.0;
                double exeDur = 0.0;

                if (uncategorized.start.getN() != 0) {
                    offsetDur = (uncategorized.start.getMin() - minTime) / Metrics.MiliSec;
                    exeDur = (uncategorized.end.getMax() - uncategorized.start.getMin())
                            / Metrics.MiliSec;
                }

                offset.add(new Object[]{offsetDur, uncategorizedID});

                set(uncategorizedID, exeDur, 0, execDuration, inDuration, outDuration, fromDuration,
                        toDuration);

                List categoriesGanttChart = new LinkedList();
                categoriesGanttChart.add(offset);
                categoriesGanttChart.add(execDuration);
                categoriesGanttChart.add(inDuration);
                categoriesGanttChart.add(outDuration);
                categoriesGanttChart.add(fromDuration);
                categoriesGanttChart.add(toDuration);

                data.append("categoriesGanttChart = " + gson.toJson(categoriesGanttChart) + ";\n");
            }

            { // Exec Operators Details
                Map<String, Map> categoriesExec = new HashMap<String, Map>();
                int[] zeros = new int[50];
                int categoriesExecBuckets = 50;

                int id = 0;
                for (int catID = 0; catID < categories.size(); catID++) {
                    OpCategoryStats catStats = categoryStats.get(categories.get(catID));
                    int ordinal = findType(catStats.category).ordinal();

                    Map catMap = new HashMap();
                    catMap.put("name", catStats.category);

                    ArrayList<int[]> catData = new ArrayList<int[]>();
                    int max = 0;
                    for (int i = 0; i < 5; ++i) {
                        if (i == ordinal) {
                            int[] timeLine = catStats.getTimeline(categoriesExecBuckets);
                            for (int tl : timeLine) {
                                max = Math.max(max, tl);
                            }
                            catData.add(timeLine);
                        } else {
                            catData.add(zeros);
                        }
                    }

                    catMap.put("data", catData);
                    catMap.put("max", max);

                    categoriesExec.put("" + id, catMap);
                    id++;
                }

                data.append(
                        "categoriesExecQueriesCount = " + gson.toJson(categoriesExec.size()) + ";\n");
                data.append("categoriesExec = " + gson.toJson(categoriesExec) + ";\n");
                data.append(
                        "categoriesExecBuckets = " + gson.toJson(categoriesExecBuckets) + ";\n");
            }

            { // categoriesResourceUtilization
                int numOfResourceUtilBuckets = 500;

                HashMap<String, BitSet> containerMaskMap = new HashMap<String, BitSet>();

                //        int categoriesMaxResourceUtilization = 0;
                int[] categoriesResourceUtilization = new int[numOfResourceUtilBuckets];
                double step = (maxTime - minTime) / (double) categoriesResourceUtilization.length;
                for (ContainerSessionStatistics cStats : stats.containerStats) {
                    BitSet cMask = containerMaskMap.get(cStats.containerName);
                    if (cMask == null) {
                        cMask = new BitSet(numOfResourceUtilBuckets);
                        containerMaskMap.put(cStats.containerName, cMask);
                    }
                    for (ConcreteOperatorStatistics opStats : cStats.operators) {
                        int s = (int) ((opStats.getStartTime_ms() - minTime) / step);
                        int e = (int) ((opStats.getEndTime_ms() - minTime) / step);
                        for (int f = s; f < e; ++f) {
                            cMask.set(f);
                        }
                    }
                }
                for (BitSet cMask : containerMaskMap.values()) {
                    for (int i = cMask.nextSetBit(0); i >= 0; i = cMask.nextSetBit(i + 1)) {
                        ++categoriesResourceUtilization[i];
                        //            categoriesMaxResourceUtilization =
                        //                    Math.max(categoriesMaxResourceUtilization, categoriesResourceUtilization[i]);
                    }
                }
                data.append(
                        "categoriesResourceUtilization = " + gson.toJson(categoriesResourceUtilization)
                                + ";\n");
                data.append(
                        "categoriesMaxResourceUtilization = " + gson.toJson(containerMaskMap.size())
                                + ";\n");
                data.append("categoriesResourceUtilizationBuckets = " + gson
                        .toJson(numOfResourceUtilBuckets) + ";\n");
            }
        }

        // Write data file.
        FileUtil.writeFile(data.toString(), new File(folder, "data.js"));

        // Write index.html file.
        String template = FileUtil.readFile(this.getClass().getResource("template.html"));
        FileUtil.writeFile(template, new File(folder, "index.html"));

        // Write library.
        ZipUtil
                .extract(this.getClass().getResource("plotLibs.zip"), folder.getAbsolutePath() + "/");
    }

    private Map<String, String> makeColumn(String name, boolean alignRight) {
        Map<String, String> column = new HashMap<String, String>();
        column.put("sTitle", name);
        if (alignRight) {
            column.put("sClass", "alignRight");
        }
        return column;
    }

    private CategoryType findType(String category) {
        if (category.endsWith("" + OperatorCategory.dt) == false) {
            return CategoryType.exec;
        }

        if (category.endsWith("R_" + OperatorCategory.dt)) {
            return CategoryType.in;
        }

        if (category.endsWith("W_" + OperatorCategory.dt)) {
            return CategoryType.out;
        }

        if (category.endsWith("F_" + OperatorCategory.dt)) {
            return CategoryType.netIn;
        }

        if (category.endsWith("T_" + OperatorCategory.dt)) {
            return CategoryType.netOut;
        }

        return null;
    }

    private void set(int id, double duration, int list, List... lists) {
        for (int i = 0; i < lists.length; ++i) {
            if (i == list) {
                lists[i].add(new Object[]{duration, id});
            } else {
                lists[i].add(new Object[]{0, id});
            }
        }
    }

    private enum CategoryType {

        exec,
        in,
        out,
        netIn,
        netOut
    }


    private class OpCategoryStats {

        String category = null;
        DescriptiveStatistics time = null;
        DescriptiveStatistics input = null;
        DescriptiveStatistics output = null;
        DescriptiveStatistics start = null;
        DescriptiveStatistics end = null;
        List<ContainerSessionStatistics> stats = null;

        OpCategoryStats(String category, List<ContainerSessionStatistics> stats) {
            this.category = category;
            this.time = new DescriptiveStatistics();
            this.input = new DescriptiveStatistics();
            this.output = new DescriptiveStatistics();
            this.start = new DescriptiveStatistics();
            this.end = new DescriptiveStatistics();
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

            if (minTime > opStats.getStartTime_ms()) {
                minTime = opStats.getStartTime_ms();
            }

            if (maxTime < opStats.getEndTime_ms()) {
                maxTime = opStats.getEndTime_ms();
            }

            start.addValue(opStats.getStartTime_ms());
            end.addValue(opStats.getEndTime_ms());
            time.addValue(opStats.getTotalTime_ms());
            input.addValue(inputBytes);
            output.addValue(outputBytes);
        }

        int[] getTimeline(int numberOfBuckets) {
            double minStart = start.getMin();
            double maxEnd = end.getMax();
            double step = (maxEnd - minStart) / numberOfBuckets;

            int[] values = new int[numberOfBuckets];
            for (int i = 0; i < start.getN(); ++i) {
                int s = (int) ((start.getElement(i) - minStart) / step);
                int e = (int) ((end.getElement(i) - minStart) / step);

                for (int f = s; f < e; ++f) {
                    values[f]++;
                }
            }

            return values;
        }

        String[] getStats(int catID, CategoryType type, String tooltip, int numberOfBuckets)
                throws Exception {
            long offset = minTime;
            if (start.getN() == 0) {
                offset = 0;
            }

            String meanStart = timeF.format((long) start.getMean() - offset);
            String minStart = timeF.format((long) start.getMin() - offset);
            String maxStart = timeF.format((long) start.getMax() - offset);
            String startHistogramCSV = builHistogram(start, numberOfBuckets);

            String meanTime = timeF.format((long) time.getMean());
            String minTime = timeF.format((long) time.getMin());
            String maxTime = timeF.format((long) time.getMax());
            String timeHistogramCSV = builHistogram(time, numberOfBuckets);

            String meanIn = dataF.format((long) input.getMean());
            String minIn = dataF.format((long) input.getMin());
            String maxIn = dataF.format((long) input.getMax());
            String inputHistogramCSV = builHistogram(input, numberOfBuckets);

            String meanOut = dataF.format((long) output.getMean());
            String minOut = dataF.format((long) output.getMin());
            String maxOut = dataF.format((long) output.getMax());
            String outputHistogramCSV = builHistogram(output, numberOfBuckets);

            String categoryString = category;
            if (tooltip != null) {
                categoryString = "<u class='tooltip'>" + category + "<span> <code>" + tooltip
                        + "</code> </span></u>";
            }

            return new String[]{catID + "", type.toString() + "/", categoryString,
                    time.getN() + "", meanStart, "[" + minStart + "-" + maxStart + "]",
                    "<span class=\"inlinebar_" + type + "\">" + startHistogramCSV + "</span>", meanTime,
                    "[" + minTime + "-" + maxTime + "]",
                    "<span class=\"inlinebar_" + type + "\">" + timeHistogramCSV + "</span>", meanIn,
                    "[" + minIn + "-" + maxIn + "]",
                    "<span class=\"inlinebar_" + type + "\">" + inputHistogramCSV + "</span>", meanOut,
                    "[" + minOut + "-" + maxOut + "]",
                    "<span class=\"inlinebar_" + type + "\">" + outputHistogramCSV + "</span>"};
        }

        private String builHistogram(DescriptiveStatistics stats, int numberOfBuckets)
                throws Exception {
            Histogram histogram = new Histogram(
                    new PartitionRule(PartitionClass.serial, PartitionConstraint.equi_width));

            ArrayList<Pair<?, Double>> timeData =
                    new ArrayList<Pair<?, Double>>((int) stats.getN());
            int i = 0;
            for (double v : stats.getSortedValues()) {
                timeData.add(new Pair<Integer, Double>(i, v));
                ++i;
            }

            LinkedList<Bucket> timeBucketList =
                    histogram.createHistogram(timeData, numberOfBuckets);
            StringBuilder sb = new StringBuilder();
            for (int bNum = 0; bNum < numberOfBuckets; bNum++) {
                Bucket bucket = timeBucketList.get(bNum);
                if (bNum == 0) {
                    sb.append(bucket.data.size());
                } else {
                    sb.append("," + bucket.data.size());
                }
            }
            return sb.toString();
        }
    }
}
