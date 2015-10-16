/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.resourceScheduler;

import madgik.exareme.common.app.engine.scheduler.elasticTree.ContainerTopology;
import madgik.exareme.common.app.engine.scheduler.elasticTree.DataPartitionLayout;
import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeConstants;
import madgik.exareme.common.app.engine.scheduler.elasticTree.supplier.NonLinearEQSolver;
import madgik.exareme.common.app.engine.scheduler.elasticTree.supplier.TreeStats;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.ComputeCloud;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.data.Database;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.RunningDataflow;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.runtime.GlobalSystemState;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class NonLinearOptLoadBalanceTreeResourceScheduler
    extends StaticLoadBalanceTreeResourceScheduler {
    private final Database db;
    private final ContainerTopology topology;
    private final DataPartitionLayout dataLayout;
    private double lastSupplierRun = 0;

    public NonLinearOptLoadBalanceTreeResourceScheduler(int[] initialContainersPerLevel,
        Database db, ContainerTopology topology, DataPartitionLayout dataLayout) {
        super(initialContainersPerLevel, topology);
        this.db = db;
        this.topology = topology;
        this.dataLayout = dataLayout;
    }

    @Override public void finalizeResources(ComputeCloud cloud, GlobalSystemState state) {
        super.finalizeResources(cloud, state);
    }

    private void changeElasticLevels(GlobalSystemState state) throws RemoteException {
        double window = TreeConstants.SETTINGS.HISTORICAL_WINDOW_SEC;
        double predictionWindow = TreeConstants.SETTINGS.PREDICTION_WINDOW_SEC;

        double[] queriesPerSLA = new double[TreeConstants.SETTINGS.SLAS.length];
        double totalNumQueries = 0.0;
        // Get stats from finished queries
        totalNumQueries += state.getFinishedDataflowsInLast(window, queriesPerSLA);
        totalNumQueries += state.getRunningDataflows(queriesPerSLA);

        double[] cpuLoadPerLevel = new double[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
        double[] dataLoadPerLevel = new double[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
        double[] cpuDataLoad = new double[2];
        for (int l = 0; l < TreeConstants.SETTINGS.MAX_TREE_HEIGHT; ++l) {
            topology.getLoadInLastWindowAtLevel(l, window, GlobalTime.getCurrentSec(), cpuDataLoad);
            cpuLoadPerLevel[l] = cpuDataLoad[0];
            dataLoadPerLevel[l] = cpuDataLoad[1];
        }

        double concurent = state.getAVGRunningDataflows(window);
        double[] currentContainersPerLevel = new double[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
        for (int l = 0; l < TreeConstants.SETTINGS.MAX_TREE_HEIGHT; ++l) {
            currentContainersPerLevel[l] = topology.getContainersAtLevel(l);
        }

        TreeStats treeStats =
            new TreeStats(TreeConstants.SETTINGS.SLAS, queriesPerSLA, cpuLoadPerLevel,
                dataLoadPerLevel, concurent, topology.getContainersAtLevel(0),
                currentContainersPerLevel, db.getMaxNumOfParts(),
                db.getTotalTableSize() / db.getMaxNumOfParts(), dataLayout.getReplication(),
                window);

        double[] contaienersPerLevel = new double[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
        double objectiveFun =
            NonLinearEQSolver.optimize(treeStats, contaienersPerLevel, predictionWindow);

        if (TreeConstants.SETTINGS.VERBOSE_OUTPUT >= 1) {
            System.out.println("QUERIES: " + totalNumQueries);
            for (int l = 0; l < TreeConstants.SETTINGS.MAX_TREE_HEIGHT; l++) {
                System.out.print(contaienersPerLevel[l] + " ");
            }
            System.out.println("");
            System.out.println("OBJECTIVE: " + objectiveFun);
        }

        for (int l = 0; l < TreeConstants.SETTINGS.MAX_TREE_HEIGHT; l++) {
            int current = topology.getContainersAtLevel(l);
            int suggested = (int) Math.ceil(contaienersPerLevel[l]);
            if (current == suggested) {
                continue;
            }
            if (current < suggested) {
                topology.allocateContainerAtLevel(l, suggested - current);
            } else {
                topology.deleteContainersAtLevel(l, current - suggested);
            }
        }
    }

    @Override public void dataflowFinished(RunningDataflow runningDataflow, ComputeCloud cloud,
        GlobalSystemState state) {
        super.dataflowFinished(runningDataflow, cloud, state);
        if (GlobalTime.getCurrentSec() - lastSupplierRun
            > TreeConstants.SETTINGS.RUN_SUPPLIER_EVERY) {
            try {
                changeElasticLevels(state);
            } catch (Exception e) {
                e.printStackTrace();
            }
            lastSupplierRun = GlobalTime.getCurrentSec();
        }
    }

    @Override public void reorganizeResources(GlobalSystemState state) throws RemoteException {
        changeElasticLevels(state);
        topology.progress();
    }
}
