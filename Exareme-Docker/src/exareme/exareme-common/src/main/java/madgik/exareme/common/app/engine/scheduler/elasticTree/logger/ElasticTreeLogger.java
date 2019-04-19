/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.logger;

import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeQuery;
import madgik.exareme.common.art.PlanSessionStatistics;
import madgik.exareme.utils.units.Metrics;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author heraldkllapi
 */
public class ElasticTreeLogger {
    public static final String SEP = "\t";

    private static final long start = System.currentTimeMillis();
    private static final Object lock = new Object();
    private static final Logger log = Logger.getLogger(ElasticTreeLogger.class);
    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private static FileWriter writer = null;
    private static Map<Long, TreeQuery> issuedQueries = null;

    static {
        try {
            issuedQueries = Collections.synchronizedMap(new HashMap<Long, TreeQuery>());
            writer = new FileWriter(new File("TreeLog.log-" + start));
        } catch (IOException e) {
            // Ignore!
        }
    }

    public static void init() {
        write(Code.INIT + SEP + System.currentTimeMillis());
    }

    public static void registerQuery(TreeQuery query) {
        issuedQueries.put(query.id, query);
    }

    private static long time() {
        return System.currentTimeMillis() - start;
    }

    private static void write(String msg) {
        synchronized (lock) {
            try {
                writer.write(time() + "\t" + msg + "\n");
                writer.flush();
            } catch (IOException e) {
                log.error("Cannot write to log: " + msg, e);
            }
        }
    }

    public static void queryIssued(long id, TreeQuery query) {
        log.info("START");
        write(Code.QSTART + SEP + id);
    }

    public static void querySuccess(long id, PlanSessionStatistics stats) {
        log.info("SUCCESS");
        double execTime = 0;
        if (stats != null) {
            execTime = stats.endTime() - stats.startTime();
            execTime /= Metrics.MiliSec;
        } else {
            log.error("STATS IS NULL");
        }
        TreeQuery treeQuery = issuedQueries.get(id);
        write(Code.QSUCCESS + SEP + id + SEP + DF.format(execTime) + SEP +
                DF.format(treeQuery.getSLA().getBudget(execTime)));
    }

    public static void queryError(long id, PlanSessionStatistics stats) {
        log.info("ERROR");
        double execTime = 0;
        if (stats != null) {
            execTime = stats.endTime() - stats.startTime();
        } else {
            log.error("STATS IS NULL");
        }
        write(Code.QERROR + SEP + id);
    }

    public static void elasticTreeLevel(int level, double numCont, double numOps, double cpuLoad,
                                        double dataLoad, double cpuVar, double dataVar) {
        write(Code.ELASTIC_TREE + SEP + level + SEP +
                DF.format(numCont) + SEP +
                DF.format(numOps) + SEP +
                DF.format(cpuLoad) + SEP + DF.format(cpuVar) + SEP +
                DF.format(dataLoad) + SEP + DF.format(dataVar));
    }

    public static void money(double totalCost, double totalRevenue, double windowCost,
                             double windowRevenue) {
        write(Code.MONEY + SEP +
                DF.format(totalCost) + SEP +
                DF.format(totalRevenue) + SEP +
                DF.format(totalRevenue - totalCost) + SEP +
                DF.format(windowCost) + SEP +
                DF.format(windowRevenue));
    }

    public static void queries(int totalQueries, int errorQueries, int runningQueries,
                               int queuedQueries) {
        write(Code.QUERIES + SEP +
                totalQueries + SEP +
                errorQueries + SEP +
                runningQueries + SEP +
                queuedQueries);
    }

    public static void close() {
    }


    public static class Code {
        public static String INIT = "INIT";
        // Queries
        public static String QSTART = "QSTART";
        public static String QSUCCESS = "QSUCCESS";
        public static String QERROR = "QERROR";
        public static String QUERIES = "QUERIES";
        // Tree levels
        public static String ELASTIC_TREE = "ELASTIC";
        // Cost and profit
        public static String MONEY = "MONEY";
    }
}
