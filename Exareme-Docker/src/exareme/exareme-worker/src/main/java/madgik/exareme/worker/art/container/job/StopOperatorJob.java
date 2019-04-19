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
public class StopOperatorJob implements ContainerJob {
    public final ConcreteOperatorID opID;

    public StopOperatorJob(ConcreteOperatorID operatorID) {
        this.opID = operatorID;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.stopOperator;
    }
}
