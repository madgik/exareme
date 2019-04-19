/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree;


import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.SystemConstants;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.ComputeCloud;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.container.Container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author heraldkllapi
 */
public class ContainerTopology {
    private final ComputeCloud cloud;
    private final DataPartitionLayout dataPartitioning;
    private final ArrayList<ArrayList<Container>> treeLevels;
    private int numElasticContainers = 0;

    private LinkedList<Container> dataContainersToDelete;
    private LinkedList<Container> containersToDelete;

    public ContainerTopology(ComputeCloud cloud, DataPartitionLayout dataPartitioning) {
        this.cloud = cloud;
        this.dataPartitioning = dataPartitioning;
        this.treeLevels = new ArrayList<>(TreeConstants.SETTINGS.MAX_TREE_HEIGHT);
        for (int h = 0; h < TreeConstants.SETTINGS.MAX_TREE_HEIGHT; ++h) {
            this.treeLevels.add(new ArrayList<Container>());
        }
        containersToDelete = new LinkedList<>();
        dataContainersToDelete = new LinkedList<>();
    }

    public void allocateStaticContainersPerLevel(int contPerLevel[]) {
        for (int h = 0; h < contPerLevel.length; ++h) {
            for (int i = 0; i < contPerLevel[h]; ++i) {
                treeLevels.get(h).add(cloud.allocateNewContainer());
                numElasticContainers++;
            }
        }
        ArrayList<Long> dataContainers = new ArrayList<>();
        for (Container c : treeLevels.get(0)) {
            dataContainers.add(c.getId());
        }
        dataPartitioning.initializeWithContainers(dataContainers);
    }

    public void deleteAllContainers() {
        for (int h = 0; h < treeLevels.size(); ++h) {
            for (Container c : treeLevels.get(h)) {
                cloud.deleteContainer(c.getId());
                numElasticContainers--;
            }
        }
    }

    public Container getContainer(int level, int rank) {
        return treeLevels.get(level).get(rank);
    }

    public int getContainersAtLevel(int level) {
        return treeLevels.get(level).size();
    }

    public Container getContainer(long cID) {
        return cloud.getActiveContainer(cID);
    }

    public ArrayList<Container> getAllContainersAtLevel(int level) {
        return treeLevels.get(level);
    }

    public int getNumElasticContainers() {
        return numElasticContainers;
    }

    public void getLoadInLastWindowAtLevel(int level, double window, double wallTime_sec,
                                           double[] cpuDataLoad) {
        cpuDataLoad[0] = 0.0;
        cpuDataLoad[1] = 0.0;
        ArrayList<Container> containers = treeLevels.get(level);
        double cpuDataLoadBuffer[] = new double[2];
        for (Container c : containers) {
            cloud.getLoadInLastWindow(c.getId(), window, wallTime_sec, cpuDataLoadBuffer);
            cpuDataLoad[0] += cpuDataLoadBuffer[0];
            cpuDataLoad[1] += cpuDataLoadBuffer[1];
        }
    }

    public void getLoadInLastWindowAtLevel(int level, double window, double wallTime_sec,
                                           double[] cpuDataLoad, double[] cpuDataVarLoad) {
        cpuDataLoad[0] = 0.0;
        cpuDataLoad[1] = 0.0;
        cpuDataVarLoad[0] = 0.0;
        cpuDataVarLoad[1] = 0.0;
        ArrayList<Container> containers = treeLevels.get(level);

        double minCPULoad = Double.MAX_VALUE;
        double maxCPULoad = 0.0;
        double minDataLoad = Double.MAX_VALUE;
        double maxDataLoad = 0.0;

        double cpuDataLoadBuf[] = new double[2];
        for (Container c : containers) {
            cloud.getLoadInLastWindow(c.getId(), window, wallTime_sec, cpuDataLoadBuf);
            cpuDataLoad[0] += cpuDataLoadBuf[0];
            cpuDataLoad[1] += cpuDataLoadBuf[1];

            minCPULoad = Math.min(minCPULoad, cpuDataLoadBuf[0]);
            maxCPULoad = Math.max(maxCPULoad, cpuDataLoadBuf[0]);
            minDataLoad = Math.min(minDataLoad, cpuDataLoadBuf[1]);
            maxDataLoad = Math.max(maxDataLoad, cpuDataLoadBuf[1]);
        }
        cpuDataVarLoad[0] = (maxCPULoad - minCPULoad) / minCPULoad;
        cpuDataVarLoad[1] = (maxDataLoad - minDataLoad) / minDataLoad;
    }

    public void allocateContainerAtLevel(int level, int addContainers) {
        deleteEmptyContainers();
        ArrayList<Long> dataContainers = new ArrayList<>();
        for (int i = 0; i < addContainers; ++i) {
            Container c = null;
            if (level == 0) { // Data level
                // Try data container first
                if (dataContainersToDelete.size() > 0) {
                    c = dataContainersToDelete.removeLast();
                } else {
                    // if no data container exists, try internal
                    if (containersToDelete.size() > 0) {
                        c = containersToDelete.removeLast();
                    } else {
                        // If none exist, allocate new container
                        if (cloud.hasAvailable()) {
                            c = cloud.allocateNewContainer();
                        }
                    }
                }
            } else {
                // Try to re-use container
                if (containersToDelete.size() > 0) {
                    c = containersToDelete.removeLast();
                } else {
                    // If none exist, allocate
                    if (cloud.hasAvailable()) {
                        c = cloud.allocateNewContainer();
                    }
                }
            }
            if (c != null) {
                treeLevels.get(level).add(c);
                numElasticContainers++;
                if (level == 0) {
                    dataContainers.add(c.getId());
                }
            }
        }
        if (level == 0 && dataContainers.size() > 0) {
            dataPartitioning.addContainers(dataContainers);
        }
    }

    public int deleteContainersAtLevel(int level, int deleteContainers) {
        // Delete containers that are empty
        deleteEmptyContainers();
        // Compute the load of containers
        ArrayList<Container> containers = treeLevels.get(level);
        Load[] containersLoad = new Load[containers.size()];
        long window = TreeConstants.SETTINGS.HISTORICAL_WINDOW_SEC;
        double network_MB_SEC = SystemConstants.SETTINGS.RUNTIME_PROPS.network_speed__MB_SEC;
        double[] cpuDataLoad = new double[2];
        for (int c = 0; c < containers.size(); ++c) {
            Container cont = containers.get(c);
            cont.getLoadInLastWindow(window, GlobalTime.getCurrentSec(), cpuDataLoad);
            double loadMomentum = (cpuDataLoad[0] + (cpuDataLoad[1] / network_MB_SEC)) / window;
            containersLoad[c] = new Load(c, loadMomentum);
        }
        Arrays.sort(containersLoad);

        // Find number of actual containers deleted
        int actualDelete = deleteContainers;
        int minContainers = TreeConstants.SETTINGS.MIN_NUM_CONTAINERS;
        if (level == 0) {
            minContainers *= 2;
        }
        if (containers.size() - deleteContainers < minContainers) {
            actualDelete = containers.size() - minContainers;
        }

        // Delete the least loaded containers
        ArrayList<Container> newLevelContainers = new ArrayList<>();
        ArrayList<Long> currentDeletedContainers = new ArrayList<>();
        for (int i = 0; i < containers.size(); ++i) {
            //      System.out.println(containersLoad[i]);
            if (i < actualDelete) {
                // Delete only the not used containers
                Container toDelete = containers.get(containersLoad[i].getId());
                //        cloud.deleteContainer(toDelete.getId());
                if (level == 0) {
                    dataContainersToDelete.add(toDelete);
                } else {
                    containersToDelete.add(toDelete);
                }
                currentDeletedContainers.add(toDelete.getId());
                numElasticContainers--;
            } else {
                newLevelContainers.add(containers.get(containersLoad[i].getId()));
            }
        }
        if (level == 0) {
            dataPartitioning.removeContainers(currentDeletedContainers);
        }
        //    System.out.println("--");
        treeLevels.set(level, newLevelContainers);
        return containers.size() - newLevelContainers.size();
    }

    private void deleteEmptyContainers() {
        double quantum = SystemConstants.SETTINGS.RUNTIME_PROPS.quantum__SEC;
        { // Internal containers
            LinkedList<Container> remainingContainersToDelete = new LinkedList<>();
            for (Container c : containersToDelete) {
                double idleTimeSeconds = GlobalTime.getCurrentSec() - c.getLastActivityTime();
                double percentCurrentQuantum = c.getTimeInCurrentQuantum() / quantum;
                //        System.out.println("> " + c.getRunningJobs() + " - " + percentCurrentQuantum + " - " +
                //                           (GlobalTime.time - c.getLastActivityTime()));
                if (idleTimeSeconds >= quantum || percentCurrentQuantum > 0.95) {
                    cloud.deleteContainer(c.getId());
                } else {
                    remainingContainersToDelete.add(c);
                }
            }
            containersToDelete = remainingContainersToDelete;
        }
        { // Data containers
            LinkedList<Container> remainingContainersToDelete = new LinkedList<>();
            for (Container c : dataContainersToDelete) {
                double idleTimeSeconds = GlobalTime.getCurrentSec() - c.getLastActivityTime();
                double percentCurrentQuantum = c.getTimeInCurrentQuantum() / quantum;
                //        System.out.println("> " + c.getRunningJobs() + " - " + percentCurrentQuantum + " - " +
                //                           (GlobalTime.time - c.getLastActivityTime()));
                if (idleTimeSeconds >= quantum || percentCurrentQuantum > 0.95) {
                    cloud.deleteContainer(c.getId());
                } else {
                    remainingContainersToDelete.add(c);
                }
            }
            dataContainersToDelete = remainingContainersToDelete;
        }
    }

    public void progress() {
        deleteEmptyContainers();
    }
}
