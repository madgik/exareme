/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.LogUtils;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class OperatorTerminatedEventListener implements EventListener<OperatorTerminatedEvent> {
    public static final OperatorTerminatedEventListener instance =
        new OperatorTerminatedEventListener();
    private static final long serialVersionUID = 1L;

    public OperatorTerminatedEventListener() {
    }

    @Override public void processed(OperatorTerminatedEvent event, RemoteException exception,
        EventProcessor processor) {
        //    log.trace("$$ TERMINATED: " + event.operatorID + " :"
        //            + state.getTerminatedOperatorCount() + "/"
        //            + state.getStatistics().totalProcessingOperators() + " ACTIVE: "
        //            + state.getStatistics().processingOperatorsInstantiated());
        //
        //    long waitingFor = state.getStatistics().processingOperatorsInstantiated()
        //            - state.getStatistics().processingOperatorsCompleted();
        //    log.trace("Waiting for: " + waitingFor);
        //
        //    if (waitingFor < 3) {
        //      for (ActiveObject ao : state.getAllActiveObjects()) {
        //        if (ao instanceof ActiveOperator == false) {
        //          continue;
        //        }
        //        ActiveOperator op = (ActiveOperator)ao;
        //        if (op.hasTerminated == false) {
        //          log.trace("\tWaiting for: " + op.operatorEntity.operatorName);
        //        }
        //      }
        //    }
        if (exception != null) {
            LogUtils.logException("Termination", exception);
            PlanEventScheduler.engineInternalException(event, exception);
        }
        event.done();
    }
}
