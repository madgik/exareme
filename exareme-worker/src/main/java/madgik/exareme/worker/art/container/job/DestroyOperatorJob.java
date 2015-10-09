/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;

/**
 * @author heraldkllapi
 */
public class DestroyOperatorJob implements ContainerJob {
    public final ConcreteOperatorID opID;

    public DestroyOperatorJob(ConcreteOperatorID operatorID) {
        this.opID = operatorID;
    }

    @Override public ContainerJobType getType() {
        return ContainerJobType.destroyOperator;
    }
}
