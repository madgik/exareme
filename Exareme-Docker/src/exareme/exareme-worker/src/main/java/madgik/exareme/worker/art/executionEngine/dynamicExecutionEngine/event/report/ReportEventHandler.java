/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.report;

import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;

import java.rmi.RemoteException;

/**
 * @author johnchronis
 */
public class ReportEventHandler implements ExecEngineEventHandler<ReportEvent> {
    public static final ReportEventHandler instance = new ReportEventHandler();
    private static final long serialVersionUID = 1L;

    public ReportEventHandler() {
    }

    @Override
    public void preProcess(ReportEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        state.addOperatorExitMessage(event.operatorID, event.exidCode);
        System.out.println("ReportEventpreProcess  " + state);
    }

    @Override
    public void handle(ReportEvent event, EventProcessor proc) throws RemoteException {
        //System.out.println("CCCCCCCCCCCCCCCC");
    }

    @Override
    public void postProcess(ReportEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        //System.out.println("DDDDDDDDDDDDDDDDDD");
    }
}
