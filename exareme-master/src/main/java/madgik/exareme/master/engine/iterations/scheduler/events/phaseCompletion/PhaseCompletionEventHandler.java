package madgik.exareme.master.engine.iterations.scheduler.events.phaseCompletion;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.iterations.scheduler.IterationsDispatcher;
import madgik.exareme.master.engine.iterations.scheduler.events.IterationsEventHandler;
import madgik.exareme.master.engine.iterations.scheduler.exceptions.IterationsSchedulerFatalException;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.engine.iterations.state.exceptions.IterationsStateFatalException;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.*;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class PhaseCompletionEventHandler extends IterationsEventHandler<PhaseCompletionEvent> {
    private static final Logger log = Logger.getLogger(PhaseCompletionEventHandler.class);

    public PhaseCompletionEventHandler(IterationsStateManager manager,
                                       IterationsDispatcher dispatcher) {
        super(manager, dispatcher);
    }

    /**
     * Handles a phase completion of an iterative algorithm.
     *
     * <p>Specifically, if:
     * <ul>
     * <li>init phase executed, then submit step phase</li>
     * <li>step phase executed, then submit termination condition phase</li>
     * <li>termination condition phase executed, then if iterations should continue submit
     * termination condition phase, otherwise submit finalize phase</li>
     * <li>finalize phase executed, then submit an algorithm completion event</li>
     * </ul><br>
     * <p>
     * Before proceeding to submission, we ensure that the query executed has no errors. If it does,
     * this is logged as an error and the algorithm's state is cleaned from
     * {@link IterationsStateManager}.
     *
     * @throws RemoteException if submission of the query and registering of the listener fails
     */
    @Override
    public void handle(PhaseCompletionEvent event, EventProcessor proc) throws RemoteException {
        IterativeAlgorithmState ias =
                iterationsStateManager.getIterativeAlgorithm(event.getAdpDBQueryID());
        if (ias == null) {
            // In this type of event, if the iterative algorithm state doesn't reside in
            // IterationsStateManager, then it might have been deleted due to an error after
            // the query submission.
            String errMsg = IterativeAlgorithmState.class.getSimpleName() + " for algorithmKey: ["
                    + event.getAlgorithmKey() + "] doesn't exist in "
                    + IterationsStateManager.class.getSimpleName();
            cleanupOnFailure(event.getAlgorithmKey(), event.getQueryStatus().getQueryID(), log, errMsg);
            return;
        }

        AdpDBClientQueryStatus queryStatus = null;
        String errMsg = null;
        try {
            if (!ias.tryLock()) {
                log.debug("Lock was already acquired, exiting...");
                return;
            }

            iterationsStateManager.removeQueryOfIterativeAlgorithm(event.getAdpDBQueryID());
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel previousExecutionPhase =
                    ias.getCurrentExecutionPhase();

            try {
                // Ensure success of previous query
                // Prepare errMsg in case of RemoteException on hasError check.
                errMsg = "Failed to check if [" + ias.getCurrentExecutionPhase() + "-phase] query" +
                        " of " + ias.toString() + " had error";
                if (event.getQueryStatus().hasError()) {
                    // Handle algorithm execution error
                    Exception e = event.getQueryStatus().getLastException();
                    errMsg = "[" + ias.getCurrentExecutionPhase() + "-phase] query of "
                            + ias.toString() + " failed"
                            + (e != null ? ": " + e.getMessage() : ".");
                    cleanupOnFailure(event.getAlgorithmKey(), null, log, errMsg);
                    return;
                }

                switch (previousExecutionPhase) {
                    case init:
                        // Initial step phase execution
                        errMsg = "Failed to submit [step-phase] query of " + ias.toString() + ".";
                        queryStatus = submitQueryAndUpdateExecutionPhase(ias, step);
                        break;

                    case step:
                        // Increment #iterations, submit termination condition query & update current
                        // execution phase.
                        ias.incrementIterationsNumber();
                        errMsg = "Failed to submit [terminationCondition-phase] query of "
                                + ias.toString() + ".";
                        queryStatus = submitQueryAndUpdateExecutionPhase(ias, termination_condition);
                        break;

                    case termination_condition:
                        // Read from iterationsDB table to check value of termination condition
                        // If set to true, then submit finalize query, otherwise submit a step query.
                        errMsg = "Failed to read termination condition value for "
                                + ias.toString() + ".";
                        boolean shouldContinue = ias.readTerminationConditionValue();
                        String terminationConditionOutput =
                                ias.readTerminationConditionScriptOutput();
                        if (log.isDebugEnabled())
                            log.debug(ias.toString() + ": termination_condition["
                                    + (ias.getCurrentIterationsNumber() - 1) + "] output: "
                                    + "\n" + terminationConditionOutput);
                        if (shouldContinue &&
                                (ias.getCurrentIterationsNumber() < ias.getMaxIterationsNumber())) {
                            errMsg = "Failed to submit [step-phase] query of "
                                    + ias.toString() + ".";
                            queryStatus = submitQueryAndUpdateExecutionPhase(ias, step);
                        } else {
                            queryStatus = submitQueryAndUpdateExecutionPhase(ias, finalize);
                            errMsg = "Failed to submit [finalize-phase] query of "
                                    + ias.toString() + ".";
                            ias.setAdpDBClientFinalizeQueryStatus(queryStatus);
                        }
                        break;

                    case finalize:
                        iterationsDispatcher.submitAlgorithmTerminationEvent(ias.getAlgorithmKey());
                        break;

                    default:
                        errMsg = "Unsupported IterativeAlgorithm phase [" + previousExecutionPhase
                                + "] for " + ias.toString();
                        throw new IterationsSchedulerFatalException(errMsg, ias.getAlgorithmKey());
                }
            } catch (RemoteException | IterationsStateFatalException e) {
                AdpDBQueryID queryID = queryStatus != null ? queryStatus.getQueryID() : null;
                cleanupOnFailure(ias.getAlgorithmKey(), queryID, log, errMsg);
                if (e instanceof RemoteException) {
                    throw e;
                }
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
