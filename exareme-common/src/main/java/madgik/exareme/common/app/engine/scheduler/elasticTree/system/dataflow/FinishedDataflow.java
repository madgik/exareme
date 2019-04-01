/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow;


import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.cloud.ErrorMsg;
import madgik.exareme.utils.check.Check;

/**
 * @author heraldkllapi
 */
public class FinishedDataflow {
    private final long id;
    private final SLA sla;
    private final double finishedTime;
    private final double execTime;
    private final double chargedMoney;
    private final ErrorMsg msg;

    public FinishedDataflow(RunningDataflow dataflow, double finishedTime, ErrorMsg msg) {
        this.finishedTime = finishedTime;
        if (msg == ErrorMsg.SUCCESS) {
            Check.NotNull(dataflow.getDataflow().getSLA(), "SLA should not be null");
            this.chargedMoney =
                    dataflow.getDataflow().getSLA().getBudget(finishedTime - dataflow.getQueuedTime());
        } else {
            this.chargedMoney = 0;
        }
        this.execTime = finishedTime - dataflow.getQueuedTime();
        this.msg = msg;
        this.sla = dataflow.getDataflow().getSLA();
        this.id = dataflow.getDataflowId();
    }

    public long getDataflowId() {
        return id;
    }

    public SLA getSLA() {
        return sla;
    }

    public double getFinishedTime() {
        return finishedTime;
    }

    public double getChargedMoney() {
        return chargedMoney;
    }

    public double getExecTime() {
        return execTime;
    }

    public ErrorMsg getMsg() {
        return msg;
    }
}
