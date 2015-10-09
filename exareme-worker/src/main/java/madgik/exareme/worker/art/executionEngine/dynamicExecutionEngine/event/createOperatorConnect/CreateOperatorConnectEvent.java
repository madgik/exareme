/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperatorConnect;

import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionPlan.entity.OperatorLinkEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.OperatorLink;

/**
 * @author John Chronia
 */
public class CreateOperatorConnectEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    public OperatorLink connect = null;
    public OperatorLinkEntity connectEntity = null;
    // Pre-process
    public ContainerSession session = null;
    public ContainerJobs jobs = null;
    public ActiveOperatorGroup activeGroup = null;
    // Process
    public ContainerJobResults results = null;
    public int messageCount = 0;

    public CreateOperatorConnectEvent(OperatorLink connect, PlanEventSchedulerState state) {
        super(state);
        this.connect = connect;
    }

    public CreateOperatorConnectEvent(OperatorLinkEntity linkEntity,
        PlanEventSchedulerState state) {
        super(state);
        this.connectEntity = linkEntity;
    }
}
