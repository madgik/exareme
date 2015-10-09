/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.container;


import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.SystemConstants;
import madgik.exareme.common.art.entity.EntityName;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author heraldkllapi
 */
public class Container implements Comparable<Container> {
    private final long id;
    private final EntityName entity;
    private final ArrayList<ContainerLoad> loadHistory = new ArrayList<>();
    private double allocatedTime = 0;
    private double deletedTime = 0;
    private boolean deleted = false;
    private double lastActivityTime = 0;

    public Container(long id, EntityName entity) {
        this.id = id;
        this.entity = entity;
        allocatedTime = GlobalTime.getCurrentSec();
        lastActivityTime = allocatedTime;
    }

    public long getId() {
        return id;
    }

    public EntityName getEntity() {
        return entity;
    }

    public double getAllocatedTime() {
        return allocatedTime;
    }

    public double getLastActivityTime() {
        return lastActivityTime;
    }

    public double getTimeInCurrentQuantum() {
        double quantumSize = SystemConstants.SETTINGS.RUNTIME_PROPS.quantum__SEC;
        double allocatedDuration = GlobalTime.getCurrentSec() - allocatedTime;
        int fullQuanta = (int) (allocatedDuration / quantumSize);
        return allocatedDuration - fullQuanta * quantumSize;
    }

    public void addLoadDelta(double cpuLoad_sec, double dataLoad_MB, double wallTime_sec) {
        loadHistory.add(new ContainerLoad(cpuLoad_sec, dataLoad_MB, wallTime_sec));
        lastActivityTime = GlobalTime.getCurrentSec();
    }

    public void getLoadInLastWindow(double window, double wallTime_sec, double[] cpuDataLoad) {
        ListIterator<ContainerLoad> li = loadHistory.listIterator(loadHistory.size());
        cpuDataLoad[0] = 0.0;
        cpuDataLoad[1] = 0.0;
        while (li.hasPrevious()) {
            ContainerLoad load = li.previous();
            if (load.wallTime_sec < wallTime_sec - window) {
                break;
            }
            cpuDataLoad[0] += load.cpuLoadDelta_sec;
            cpuDataLoad[1] += load.dataLoadDelta_mb;
        }
    }

    public void deleteContainer(double deletedTime) {
        deleted = true;
        this.deletedTime = deletedTime;
    }

    public double getDeletedTime() {
        return (deleted) ? deletedTime : -1.0;
    }

    @Override public int compareTo(Container o) {
        return Long.compare(id, o.id);
    }
}
