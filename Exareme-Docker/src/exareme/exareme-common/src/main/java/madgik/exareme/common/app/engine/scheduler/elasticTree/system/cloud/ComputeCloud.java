/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud;


import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.SystemConstants;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.container.Container;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.utils.check.Check;
import madgik.exareme.utils.net.NetUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author heraldkllapi
 */
public class ComputeCloud {
    private final int maxNumContainers;
    private final RunTimeParameters runTime;
    private final FinancialProperties finProps;
    private final ArrayList<EntityName> containers;
    private final LinkedList<EntityName> availableContainers = new LinkedList<>();
    private final HashMap<String, Long> entityContainerIdMap = new HashMap<>();

    private final HashMap<Long, Container> allContainers = new HashMap<>();
    private final HashMap<Long, Container> activeContainers = new HashMap<>();
    private final Object lock = new Object();
    private final LinkedList<Integer> numContainersPerStep = new LinkedList<>();
    private long idCount = 0;
    // Statistics
    private long totalContainerQuantaAllocated = 0;

    public ComputeCloud(int maxNumContainers, ArrayList<EntityName> containers) {
        this.maxNumContainers = maxNumContainers;
        this.runTime = SystemConstants.SETTINGS.RUNTIME_PROPS;
        this.finProps = SystemConstants.SETTINGS.FIN_PROPS;
        this.containers = containers;

        String localIP = NetUtil.getIPv4() + "_";

        for (int c = 0; c < maxNumContainers; c++) {
            EntityName next = containers.get(c % containers.size());
            // Do not include the master if more than one are available
            if (containers.size() > 1 && next.getName().contains(localIP)) {
                continue;
            }
            availableContainers.add(next);
        }
    }

    public boolean areAvailable(int numContainers) {
        return maxNumContainers - activeContainers.size() >= numContainers;
    }

    public boolean hasAvailable() {
        return activeContainers.size() < maxNumContainers;
    }

    public void deleteAllContainers() {
        ArrayList<Long> toDelete = new ArrayList<>(activeContainers.keySet());
        for (long id : toDelete) {
            deleteContainer(id);
        }
    }

    public int getTotalNumContainers() {
        return allContainers.size();
    }

    public long getTotalContainerQuantaAllocated() {
        return totalContainerQuantaAllocated;
    }

    public double getCostInLast(double window) {
        long totalContainerSteps = 0;
        int windowSteps = (int) (window / GlobalTime.getCurrentSec());
        for (long cont : numContainersPerStep) {
            totalContainerSteps += cont;
            windowSteps--;
            if (windowSteps <= 0) {
                break;
            }
        }
        return totalContainerSteps * finProps.timeQuantumCost *
                GlobalTime.getCurrentSec() / runTime.quantum__SEC;
    }

    public int getNumContainers() {
        return activeContainers.size();
    }

    public Collection<Container> getActiveContainers() {
        return activeContainers.values();
    }


    // BELOW ARE REQUIRED FOR SYSTEM INTEGRATION
    public Container getActiveContainer(long cID) {
        return activeContainers.get(cID);
    }

    // TODO: Sync bug here! Not a sync bug ... is deleted when containers are replicated!
    // Tune the system better
    public long getContainerId(EntityName entity) {
        synchronized (lock) {
            return entityContainerIdMap.get(entity.getName());
        }
    }

    public Container allocateNewContainer() {
        synchronized (lock) {
            if (activeContainers.size() >= maxNumContainers) {
                throw new RuntimeException("Max number of containers reached: " + maxNumContainers);
            }
            // Get one of the available containers
            Container c = new Container(idCount, availableContainers.pollFirst());
            allContainers.put(c.getId(), c);
            idCount++;

            activeContainers.put(c.getId(), c);
            entityContainerIdMap.put(c.getEntity().getName(), c.getId());
            return c;
        }
    }

    public void deleteContainer(long id) {
        synchronized (lock) {
            try {
                Container c = Check.NotNull(activeContainers.remove(id), "Container not exists!");
                c.deleteContainer(GlobalTime.getCurrentSec());
                double totalTime = c.getDeletedTime() - c.getAllocatedTime();
                totalContainerQuantaAllocated += (long) Math.ceil(totalTime / runTime.quantum__SEC);
                // Set the container available
                availableContainers.addFirst(c.getEntity());
                // THE following produces an exception! Probably ok to comment it!
                //        entityContainerIdMap.remove(c.getEntity().getName());
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public void getLoadInLastWindow(long container, double window, double wallTime_sec,
                                    double[] cpuDataLoad) {
        synchronized (lock) {
            Container c = Check.NotNull(allContainers.get(container));
            c.getLoadInLastWindow(window, wallTime_sec, cpuDataLoad);
        }
    }

    public void addContainerLoadDelta(long container, double cpuLoad_sec, double dataLoad_MB,
                                      double wallTime_sec) {
        synchronized (lock) {
            Container c = Check.NotNull(allContainers.get(container));
            c.addLoadDelta(cpuLoad_sec, dataLoad_MB, wallTime_sec);
        }
    }

    // TODO(herald): Implement
    public double getCostInLastWindow(double window, double wallTime_sec) {
        return 0.0;
    }

    public double getTotalCost() {
        double previousContainers = totalContainerQuantaAllocated * finProps.timeQuantumCost;
        double currentlyRunning = 0.0;
        for (Container c : activeContainers.values()) {
            double totalTime = GlobalTime.getCurrentSec() - c.getAllocatedTime();
            currentlyRunning += (long) Math.ceil(totalTime / runTime.quantum__SEC);
        }
        currentlyRunning *= finProps.timeQuantumCost;
        return previousContainers + currentlyRunning;
    }
}
