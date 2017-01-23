package madgik.exareme.master.engine.iterations.scheduler.events.phaseCompletion;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.iterations.scheduler.IterationsDispatcher;
import madgik.exareme.master.engine.iterations.scheduler.events.IterationsEventHandler;
import madgik.exareme.master.engine.iterations.scheduler.exceptions.IterationsSchedulerFatalException;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.finalize;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step;
import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class PhaseCompletionEventHandler extends IterationsEventHandler<PhaseCompletionEvent> {
    private static final Logger log = Logger.getLogger(PhaseCompletionEventHandler.class);

    public PhaseCompletionEventHandler(IterationsStateManager manager,
                                       IterationsDispatcher dispatcher) {
        super(manager, dispatcher);
    }

    @Override
    public void handle(PhaseCompletionEvent event, EventProcessor proc) throws RemoteException {
        IterativeAlgorithmState ias =
                iterationsStateManager.getIterativeAlgorithm(event.getAdpDBQueryID());
        try {
            if (!ias.tryLock()) {
                log.debug("Lock was already acquired, exiting...");
                return;
            }

            iterationsStateManager.removeQueryOfIterativeAlgorithm(event.getAdpDBQueryID());
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel previousExecutionPhase =
                    ias.getCurrentExecutionPhase();

            // Ensure success of previous query
            if (event.getQueryStatus().hasError()) {
                Exception e = event.getQueryStatus().getLastException();
                String errMsg = "[" + ias.getCurrentExecutionPhase() + "-phase] query of "
                        + ias.toString() + " failed.";
                log.error(errMsg);
                throw new IterationsSchedulerFatalException(errMsg, e, errMsg);
            }

            AdpDBClientQueryStatus queryStatus = null;
            switch (previousExecutionPhase) {
                case init:
                    // Initial step phase execution
                    queryStatus = submitQueryAndUpdateExecutionPhase(ias, step);
                    break;

                case step:
                    // Increment #iterations, submit termination condition query & update current
                    // execution phase.
                    ias.incrementIterationsNumber();
                    queryStatus = submitQueryAndUpdateExecutionPhase(ias, termination_condition);
                    break;

                case termination_condition:
                    // Read from iterationsDB table to check value of termination condition
                    // If set to true, then submit finalize query, otherwise submit a step query.
                    boolean shouldContinue = ias.readTerminationConditionValue();
                    if (shouldContinue &&
                            (ias.getCurrentIterationsNumber() < ias.getMaxIterationsNumber())) {
                        queryStatus = submitQueryAndUpdateExecutionPhase(ias, step);
                    } else {
                        queryStatus = submitQueryAndUpdateExecutionPhase(ias, finalize);
                    }
                    break;

                case finalize:
                    iterationsDispatcher.submitAlgorithmTerminationEvent(ias.getAlgorithmKey());
                    break;

                default:
                    String errMsg = "Unsupported IterativeAlgorithm phase ["
                            + previousExecutionPhase + "] for " + ias.toString();
                    log.warn(errMsg);
                    throw new IterationsSchedulerFatalException(errMsg, ias.getAlgorithmKey());
            }

            if (queryStatus != null && !previousExecutionPhase.equals(finalize)) {
                iterationsStateManager.submitQueryForIterativeAlgorithm(ias.getAlgorithmKey(),
                        queryStatus.getQueryID());

                updateLog(log, queryStatus, ias);
            }
        } finally {
            ias.releaseLock();
        }

    }
}
