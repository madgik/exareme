/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;

/**
 * @author heraldkllapi
 */
public class StartOperatorJob implements ContainerJob {
    public final ConcreteOperatorID opID;
    public final ContainerSessionID contSessionID;

    public StartOperatorJob(ConcreteOperatorID operatorID, ContainerSessionID contSessionID) {
        this.opID = operatorID;
        this.contSessionID = contSessionID;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.startOperator;
    }
}
