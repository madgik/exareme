/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow;


import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class Dataflow implements Serializable {
    public final long id;
    private final double queueTime;
    private SLA sla;

    public Dataflow(long id, SLA sla) {
        this.id = id;
        this.sla = sla;
        this.queueTime = GlobalTime.getCurrentSec();
    }

    public double getQueueTime() {
        return queueTime;
    }

    public SLA getSLA() {
        return sla;
    }

    public void setSla(SLA sla) {
        this.sla = sla;
    }
}
