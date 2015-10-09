/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.supplier;


import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeConstants;
import madgik.exareme.common.app.engine.scheduler.elasticTree.client.ExponentialAdpDBClientSLA;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.SystemConstants;
import madgik.exareme.utils.r.RInterface;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class NonLinearEQSolver {
    private static final String END_CODE = "; ";
    private static final String NL_CODE = " ";

    private static final String END_HUMAN = ";\n";
    private static final String NL_HUMAN = "\n";

    private static String generateFunction(TreeStats stats, double[] containersPerLevel,
        double predictionWindow, String end, String nl) {
        double qSize = SystemConstants.SETTINGS.RUNTIME_PROPS.quantum__SEC;
        double qCost = SystemConstants.SETTINGS.FIN_PROPS.timeQuantumCost;
        double netSpeed = SystemConstants.SETTINGS.RUNTIME_PROPS.network_speed__MB_SEC;
        int numLevels = stats.CPULoadPerLevel.length;

        StringBuilder func = new StringBuilder();
        func.append("f <- function(levelsv) {").append(nl);

        // Global variables
        func.append("  qSize <- " + qSize).append(end);
        func.append("  qCost <- " + qCost).append(end);
        func.append("  hist_window <- " + stats.historyWindow).append(end);
        func.append("  pred_window <- " + predictionWindow).append(end);
        func.append("  netSpeed <- " + netSpeed).append(end + nl);

        // Data variables
        func.append("  dataContNum <- " + stats.currentDataContainers).append(end);
        func.append("  totalDataParts <- " + stats.totalDataParts).append(end);
        func.append("  dataPartSize <- " + stats.partitionSize).append(end);
        func.append("  dataReplication <- " + stats.replication).append(end);
        func.append("  dataSize <- totalDataParts * dataPartSize").append(end);
        func.append("  dataContDiff <- abs(dataContNum - levelsv[1])").append(end);
        //    func.append("  dataMove <- dataSize * (1 - (1 / 1.8 ^ dataContDiff))").append("\n\n");
        func.append(
            "  dataMove <- dataSize * (1 - min(dataContNum / levelsv[1], levelsv[1] / dataContNum))")
            .append(end + nl);

        // Load vector
        func.append("  cpuv <- c(" + stats.CPULoadPerLevel[0]);
        for (int i = 1; i < numLevels; ++i) {
            func.append(", " + stats.CPULoadPerLevel[i]);
        }
        func.append(") " + end);

        // Network cost
        func.append("  netv <- c(" + stats.dataLoadPerLevel[0]);
        for (int i = 1; i < numLevels; ++i) {
            func.append(", " + stats.dataLoadPerLevel[i]);
        }
        func.append(")" + end + nl);

        // Alpha
        ExponentialAdpDBClientSLA sla = (ExponentialAdpDBClientSLA) stats.sla[0];
        func.append("  alphav <- c(" + sla.getAlpha());
        for (int i = 1; i < stats.sla.length; ++i) {
            sla = (ExponentialAdpDBClientSLA) stats.sla[i];
            func.append(", " + sla.getAlpha());
        }
        func.append(")" + end);

        //  Gamma
        sla = (ExponentialAdpDBClientSLA) stats.sla[0];
        func.append("  gammav <- c(" + sla.getGamma());
        for (int i = 1; i < stats.sla.length; ++i) {
            sla = (ExponentialAdpDBClientSLA) stats.sla[i];
            func.append(", " + sla.getGamma());
        }
        func.append(")" + end + nl);

        // Queries
        func.append("  concurent <- " + stats.concurentQueries).append(end);
        func.append("  gueriesv <- c(" + stats.queriesPerSLA[0]);
        for (int i = 1; i < stats.sla.length; ++i) {
            func.append(", " + stats.queriesPerSLA[i]);
        }
        func.append(")" + end);
        func.append("  totalQueries <- sum(gueriesv)").append(end + nl);

        // Data repartition cost
        func.append("  dataTime <- dataReplication * dataMove / (netSpeed * levelsv[1])")
            .append(end);

        // Time
        func.append("  timev <- cpuv / levelsv + netv / (netSpeed * levelsv)").append(end);
        func.append("  time <- sum(timev)").append(end);

        func.append("  qTimeData <- (dataTime + time) * concurent / totalQueries").append(end);
        func.append("  qTime <- time * concurent / totalQueries").append(end + nl);

        // SLAs
        func.append("  futureQueriesDataV <- gueriesv * dataTime / hist_window").append(end + nl);
        func.append("  futureQueriesV <- gueriesv * (pred_window - dataTime) / hist_window")
            .append(end + nl);

        func.append("  avgQueryDataCost <- futureQueriesDataV * alphav * exp(-qTimeData / gammav)")
            .append(end);
        func.append("  avgQueryCost <- futureQueriesV * alphav * exp(-qTime / gammav)")
            .append(end + nl);

        func.append("  revQueryData <- sum(avgQueryDataCost)").append(end);
        func.append("  revQuery <- sum(avgQueryCost)").append(end);

        func.append("  revenue <- revQueryData + revQuery").append(end);
        func.append("  cost <- sum(levelsv) * qCost * pred_window / qSize").append(end);

        // Objective
        func.append("  revenue - cost" + end);
        func.append("}").append("\n");

        return func.toString();
    }

    public static double optimize(TreeStats stats, double[] containersPerLevel,
        double predictionWindow) throws RemoteException {
        String func =
            generateFunction(stats, containersPerLevel, predictionWindow, END_CODE, NL_CODE);

        if (TreeConstants.SETTINGS.VERBOSE_OUTPUT >= 2) {
            System.out.println(
                generateFunction(stats, containersPerLevel, predictionWindow, END_HUMAN, NL_HUMAN));
        }

        double max = TreeConstants.SETTINGS.MAX_NUM_CONTAINERS;
        double min = TreeConstants.SETTINGS.MIN_NUM_CONTAINERS;
        double minData = TreeConstants.SETTINGS.MIN_DATA_CONTAINERS;
        double minRoot = TreeConstants.SETTINGS.MIN_NUM_CONTAINERS_ROOT;

        //    double[] initv = new double[stats.dataLoadPerLevel.length];
        double[] minv = new double[stats.dataLoadPerLevel.length];
        double[] maxv = new double[stats.dataLoadPerLevel.length];

        for (int i = 0; i < stats.dataLoadPerLevel.length; ++i) {
            minv[i] = min;
            maxv[i] = max;
        }
        minv[0] = minData;
        minv[minv.length - 1] = minRoot;

        return RInterface.optimize(func.toString(), stats.currentContainersPerLevel, minv, maxv,
            containersPerLevel);
    }
}
