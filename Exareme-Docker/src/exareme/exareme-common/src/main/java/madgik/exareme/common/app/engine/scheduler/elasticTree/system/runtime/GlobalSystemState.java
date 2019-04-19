/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.runtime;


import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.ComputeCloud;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.ErrorMsg;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.FinishedDataflow;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.RunningDataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class GlobalSystemState {
    public final List<FinishedDataflow> finishedDataflows = new ArrayList<>();
    public final List<FinishedDataflow> errorDataflows = new ArrayList<>();
    // Dataflow
    private final HashMap<Long, RunningDataflow> runningDataflows = new HashMap<>();
    private final ComputeCloud computeCloud;
    private double totalRevenue = 0.0;

    public GlobalSystemState(ComputeCloud computeCloud) {
        // Dataflows
        this.computeCloud = computeCloud;
    }

    public int getFinishedDataflows() {
        return finishedDataflows.size();
    }

    public int getErrorDataflows() {
        return errorDataflows.size();
    }

    public void addRunningDataflow(RunningDataflow dataflow) {
        runningDataflows.put(dataflow.getDataflowId(), dataflow);
    }

    public double getAVGRunningDataflows(double window) {
        double timeSumSize = 0.0;
        for (int d = finishedDataflows.size() - 1; d >= 0; d--) {
            FinishedDataflow df = finishedDataflows.get(d);
            if (df.getFinishedTime() < GlobalTime.getCurrentSec() - window) {
                break;
            }
            timeSumSize += df.getExecTime();
        }
        double dataWindow = GlobalTime.getCurrentSec() - finishedDataflows.get(0).getFinishedTime();
        if (dataWindow < window) {
            return timeSumSize / dataWindow;
        }
        return timeSumSize / window;
    }

    public void addFinishedDataflow(FinishedDataflow finished) {
        if (finished.getMsg() == ErrorMsg.SUCCESS) {
            finishedDataflows.add(finished);
            totalRevenue += finished.getChargedMoney();
            runningDataflows.remove(finished.getDataflowId());
        } else {
            errorDataflows.add(finished);
        }
    }

    public double getRunningDataflows(double[] perSlaCategories) {
        for (RunningDataflow df : runningDataflows.values()) {
            if (perSlaCategories != null) {
                perSlaCategories[df.getDataflow().getSLA().getId()] += 1.0;
            }
        }
        return runningDataflows.size();
    }

    public double getFinishedDataflowsInLast(double window) {
        return getFinishedDataflowsInLast(window, null);
    }

    public double getFinishedDataflowsInLast(double window, double[] perSlaCategories) {
        double finished = 0;
        for (int d = finishedDataflows.size() - 1; d >= 0; d--) {
            FinishedDataflow df = finishedDataflows.get(d);
            if (df.getFinishedTime() < GlobalTime.getCurrentSec() - window) {
                break;
            }
            finished++;
            if (perSlaCategories != null) {
                perSlaCategories[df.getSLA().getId()] += 1.0;
            }
        }
        return finished;
    }

    public double getRevenueInLast(double window) {
        return getRevenueInLast(window, null);
    }

    public double getRevenueInLast(double window, double[] perSlaCategories) {
        double rev = 0.0;
        for (int d = finishedDataflows.size() - 1; d >= 0; d--) {
            FinishedDataflow df = finishedDataflows.get(d);
            if (df.getFinishedTime() < GlobalTime.getCurrentSec() - window) {
                break;
            }
            rev += df.getChargedMoney();
            if (perSlaCategories != null) {
                perSlaCategories[df.getSLA().getId()] += df.getChargedMoney();
            }
        }
        return rev;
    }

    public double getCostInLast(double window) {
        return computeCloud.getCostInLast(window);
    }

    public double getAverageQueryExecTimeInLast(double window) {
        double totalTime = 0.0;
        double dataflows = 0.0;
        for (int d = finishedDataflows.size() - 1; d >= 0; d--) {
            FinishedDataflow df = finishedDataflows.get(d);
            if (df.getFinishedTime() < GlobalTime.getCurrentSec() - window) {
                break;
            }
            totalTime += df.getExecTime();
            dataflows += 1.0;
        }
        return totalTime / dataflows;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}
