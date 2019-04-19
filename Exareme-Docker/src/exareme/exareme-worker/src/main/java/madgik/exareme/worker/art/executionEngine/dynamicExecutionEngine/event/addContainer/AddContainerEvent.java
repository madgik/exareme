/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.addContainer;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;

/**
 * @author herald
 */
public class AddContainerEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    public String containerName = null;
    public Container container = null;
    public EntityName containerEntity = null;
    public int messageCount = 0;

    //  public ExecEngineEvent(PlanEventSchedulerState state) {
    //    this.state = state;
    //  }

    //  public AddContainerEvent(Container container,
    //                           PlanEventSchedulerState state) {
    //    super(state);
    //    this.containerName = container.containerName;
    //    this.container = container;
    //  }

    public AddContainerEvent(String containerName, EntityName containerEntity,
                             PlanEventSchedulerState state) {
        super(state);
        this.containerName = containerName;
        this.containerEntity = containerEntity;
    }
}
