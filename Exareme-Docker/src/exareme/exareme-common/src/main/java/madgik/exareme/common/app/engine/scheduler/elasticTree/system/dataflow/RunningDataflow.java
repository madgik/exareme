/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow;


import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;

/**
 * @author heraldkllapi
 */
public class RunningDataflow {
    private final Dataflow dataflow;
    private final double queuedTime;
    private final double startExecTime;

    public RunningDataflow(Dataflow dataflow) {
        this.dataflow = dataflow;
        this.queuedTime = dataflow.getQueueTime();
        this.startExecTime = GlobalTime.getCurrentSec();
    }

    public long getDataflowId() {
        return dataflow.id;
    }

    public Dataflow getDataflow() {
        return dataflow;
    }

    public double getQueuedTime() {
        return queuedTime;
    }

    public double getStartExecTime() {
        return startExecTime;
    }
}
