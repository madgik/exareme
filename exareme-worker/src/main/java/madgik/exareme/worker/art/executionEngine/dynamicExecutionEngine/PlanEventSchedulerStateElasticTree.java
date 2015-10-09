/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.app.engine.ExecuteQueryExitMessage;
import madgik.exareme.common.app.engine.scheduler.elasticTree.ContainerTopology;
import madgik.exareme.common.app.engine.scheduler.elasticTree.DataPartitionLayout;
import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeConstants;
import madgik.exareme.common.app.engine.scheduler.elasticTree.dataPartition.ConsitentHashDataPartitionLayout;
import madgik.exareme.common.app.engine.scheduler.elasticTree.generator.DatabaseGenerator;
import madgik.exareme.common.app.engine.scheduler.elasticTree.generator.tpch.TpchDatabaseGenerator;
import madgik.exareme.common.app.engine.scheduler.elasticTree.resourceScheduler.NonLinearOptLoadBalanceTreeResourceScheduler;
import madgik.exareme.common.app.engine.scheduler.elasticTree.resourceScheduler.StaticLoadBalanceTreeResourceScheduler;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.ResourceSchedulingAlgorithm;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.ComputeCloud;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.ErrorMsg;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.container.Container;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.FinishedDataflow;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.runtime.GlobalSystemState;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.units.Metrics;
import madgik.exareme.worker.art.container.ContainerProxy;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * @author heraldkllapi
 */
public class PlanEventSchedulerStateElasticTree {
    private static final Logger log = Logger.getLogger(PlanEventSchedulerStateElasticTree.class);
    // All containers available
    private final ArrayList<EntityName> containers = new ArrayList<>();
    // Active states
    private final HashMap<PlanSessionID, PlanEventSchedulerState> states = new HashMap<>();
    // Schedule container to real map
    private final HashMap<PlanSessionID, HashMap<String, EntityName>> anyContMap = new HashMap<>();
    private final int[] scheduledOps = new int[3];
    // Container layout
    private final ComputeCloud computeCloud;
    private final DataPartitionLayout dataLayout;
    private final ContainerTopology topology;
    private final GlobalSystemState systemState;
    private final ResourceSchedulingAlgorithm resourceScheduler;

    private final Random rand = new Random();
    // static_small, static_medium, static_large, dynamic
    private String resourceSchedulingAlgorithm = "dynamic";

    public PlanEventSchedulerStateElasticTree(ContainerProxy[] allContainers) {
        for (ContainerProxy cp : allContainers) {
            containers.add(cp.getEntityName());
        }
        log.info("ALL CONTAINERS: " + containers.size());
        dataLayout =
            new ConsitentHashDataPartitionLayout(128  /* numParts */, 5  /* replication */);
        // Set max num to a large number of debug when running in one container
        int maxNumContainers = 1000;
        if (allContainers.length > 1) {
            maxNumContainers = allContainers.length;
        }
        computeCloud = new ComputeCloud(maxNumContainers, containers);
        topology = new ContainerTopology(computeCloud, dataLayout);
        systemState = new GlobalSystemState(computeCloud);
        DatabaseGenerator dgBen = new TpchDatabaseGenerator(32, 128);
        if (resourceSchedulingAlgorithm.equalsIgnoreCase("dynamic")) {
            resourceScheduler = new NonLinearOptLoadBalanceTreeResourceScheduler(
                TreeConstants.SETTINGS.STATIC_CONTAINERS_MEDIUM, dgBen.generateDatabase(), topology,
                dataLayout);
        } else if (resourceSchedulingAlgorithm.equalsIgnoreCase("static_small")) {
            resourceScheduler = new StaticLoadBalanceTreeResourceScheduler(
                TreeConstants.SETTINGS.STATIC_CONTAINERS_SMALL, topology);
        } else if (resourceSchedulingAlgorithm.equalsIgnoreCase("static_medium")) {
            resourceScheduler = new StaticLoadBalanceTreeResourceScheduler(
                TreeConstants.SETTINGS.STATIC_CONTAINERS_MEDIUM, topology);
        } else if (resourceSchedulingAlgorithm.equalsIgnoreCase("static_large")) {
            resourceScheduler = new StaticLoadBalanceTreeResourceScheduler(
                TreeConstants.SETTINGS.STATIC_CONTAINERS_LARGE, topology);
        } else {
            throw new RuntimeException("Scheduler not known: " + resourceSchedulingAlgorithm);
        }
        resourceScheduler.initializeResources(computeCloud, systemState);
    }

    public int getRunningDataflows() {
        return states.size();
    }

    // STATES
    public void addState(PlanSessionID sId, PlanEventSchedulerState state) {
        states.put(sId, state);
        anyContMap.put(sId, new HashMap<String, EntityName>());
    }

    public PlanEventSchedulerState getState(PlanSessionID sId) {
        return states.get(sId);
    }

    public void removeState(PlanSessionID sId) {
        states.remove(sId);
        anyContMap.remove(sId);
    }

    // TREE LAYOUT
    public EntityName getContainer(PlanSessionID sId, int level) {
        // Power of two random choices (4 choices here)!
        int choices = 4;
        double window = TreeConstants.SETTINGS.HISTORICAL_WINDOW_SEC;
        long minLoadCID = -1;
        double minLoad = Double.MAX_VALUE;
        int contNum = topology.getContainersAtLevel(level);
        double cpuDataLoad[] = new double[2];
        double wallTime_sec = GlobalTime.getCurrentSec();
        if (contNum < choices) {
            // Get all containers
            for (Container c : topology.getAllContainersAtLevel(level)) {
                computeCloud.getLoadInLastWindow(c.getId(), window, wallTime_sec, cpuDataLoad);
                double load = cpuDataLoad[0];
                if (minLoadCID < 0 || load < minLoad) {
                    minLoadCID = c.getId();
                    minLoad = load;
                }
            }
        } else {
            // Select some random containers
            for (int i = 0; i < choices; ++i) {
                Container c = topology.getContainer(level, rand.nextInt(contNum));
                computeCloud.getLoadInLastWindow(c.getId(), window, wallTime_sec, cpuDataLoad);
                double load = cpuDataLoad[0];
                if (minLoadCID < 0 || load < minLoad) {
                    minLoadCID = c.getId();
                    minLoad = load;
                }
            }
        }
        return topology.getContainer(minLoadCID).getEntity();
    }

    public EntityName getDataContainer(PlanSessionID sId, int part) {
        long cID = dataLayout.getContainer(part);
        return topology.getContainer(cID).getEntity();
    }

    // ANY CONTAINER RESOLVER
    public EntityName getRealContainer(PlanSessionID sId, String anyContainer) {
        HashMap<String, EntityName> anyMap = anyContMap.get(sId);
        return anyMap.get(anyContainer);
    }

    public void addAnyContainer(PlanSessionID sId, String anyContainer, EntityName realContainer) {
        HashMap<String, EntityName> anyMap = anyContMap.get(sId);
        anyMap.put(anyContainer, realContainer);
    }

    public void scheduleOperator(PlanSessionID sId, String operatorName, EntityName realCont,
        int level) {
        // Add the default "predicted overhead" of the operator
        long container = computeCloud.getContainerId(realCont);
        computeCloud
            .addContainerLoadDelta(container, TreeConstants.SETTINGS.OPERATOR_DEFAULT_TIME_SEC,
                TreeConstants.SETTINGS.OPERATOR_DEFAULT_DATA_MB,
                System.currentTimeMillis() / Metrics.MiliSec);
        scheduledOps[level]++;
    }

    public ElasticTreeStatistics getStatistics() {
        // Log statistics
        ElasticTreeStatistics stats = new ElasticTreeStatistics();
        System.arraycopy(scheduledOps, 0, stats.scheduledOpsPerLevel, 0, scheduledOps.length);
        double window = TreeConstants.SETTINGS.HISTORICAL_WINDOW_SEC;
        // Stats per level
        double cpuDataLoad[] = new double[2];
        double cpuDataVar[] = new double[2];
        for (int level = 0; level < TreeConstants.SETTINGS.MAX_TREE_HEIGHT; ++level) {
            topology
                .getLoadInLastWindowAtLevel(level, window, GlobalTime.getCurrentSec(), cpuDataLoad,
                    cpuDataVar);
            stats.containersPerLevel[level] = topology.getContainersAtLevel(level);
            stats.cpuLoadPerLevel[level] = cpuDataLoad[0];
            stats.dataLoadPerLevel[level] = cpuDataLoad[1];
            stats.cpuLoadVarPerLevel[level] = cpuDataVar[0];
            stats.dataLoadVarPerLevel[level] = cpuDataVar[1];
        }
        // Money
        stats.totalCost = computeCloud.getTotalCost();
        stats.windowCost = computeCloud.getCostInLast(window);
        stats.totalRevene = systemState.getTotalRevenue();
        stats.windowRevenue = systemState.getRevenueInLast(window);
        // Current
        stats.runningQueries = states.size();
        stats.totalQueries = systemState.getFinishedDataflows();
        stats.totalErrorQueries = systemState.getErrorDataflows();
        return stats;
    }

    public void operatorFinished(PlanSessionID sId, String operatorName, EntityName realCont,
        ExecuteQueryExitMessage message) {
        log.info("UPDATING STATISTICS: " + operatorName + " | " +
            message.execStats.execTime_ms + " | " +
            message.execStats.outputSize_MB);

        double time = message.execStats.execTime_ms / Metrics.MiliSec
            - TreeConstants.SETTINGS.OPERATOR_DEFAULT_TIME_SEC;
        double data =
            message.execStats.outputSize_MB - TreeConstants.SETTINGS.OPERATOR_DEFAULT_DATA_MB;
        long container = computeCloud.getContainerId(realCont);
        computeCloud.addContainerLoadDelta(container, time, data, GlobalTime.getCurrentSec());
    }

    public void dataflowQueued(PlanSessionID sId) {
        // TODO(herald): maybe this is not needed! Not sure for now!
    }

    public void dataflowStarted(PlanSessionID sId) {
        systemState.addRunningDataflow(states.get(sId).getRunningDataflow());
    }

    public void dataflowFinished(PlanSessionID sId, boolean error) {
        ErrorMsg msg = (error) ? ErrorMsg.FAIL : ErrorMsg.SUCCESS;
        systemState.addFinishedDataflow(
            new FinishedDataflow(states.get(sId).getRunningDataflow(), GlobalTime.getCurrentSec(),
                msg));
    }

    public void reorganizeResources() throws RemoteException {
        resourceScheduler.reorganizeResources(systemState);
    }
}
