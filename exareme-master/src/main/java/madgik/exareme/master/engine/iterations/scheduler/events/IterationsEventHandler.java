package madgik.exareme.master.engine.iterations.scheduler.events;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.iterations.scheduler.IterationsDispatcher;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.utils.eventProcessor.EventHandler;

/**
 * Abstract class which all iterations events must subtype.
 *
 * <p>
 * Handlers require access to iterations state manager for updating and retrieving iterative
 * algorithm's state.
 * Additionally, access for the iterations dispatcher is also required for defining the listener
 * which notifies us for a query termination event.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public abstract class IterationsEventHandler<T extends IterationsEvent>
        implements EventHandler<T> {
    protected IterationsStateManager iterationsStateManager;
    protected IterationsDispatcher iterationsDispatcher;

    public IterationsEventHandler(IterationsStateManager manager,
                                  IterationsDispatcher dispatcher) {
        iterationsStateManager = manager;
        iterationsDispatcher = dispatcher;
    }

    /**
     * Submits a query to the engine, updates current execution phase of iterative algorithm and
     * registers dispatcher as listener.
     *
     * @param ias          the current iterativeAlgorithm state
     * @param currentPhase the current phase to be executed
     * @return the status returned by the engine
     * @throws RemoteException if submitting the query via the client fails.
     */
    protected AdpDBClientQueryStatus submitQueryAndUpdateExecutionPhase(
            IterativeAlgorithmState ias,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel currentPhase)
            throws RemoteException {

        AdpDBClientQueryStatus queryStatus = ias.getAdpDBClient().query(
                ias.getAlgorithmKey(),
                ias.getDFLScript(currentPhase));

        ias.setCurrentExecutionPhase(currentPhase);

        queryStatus.registerListener(iterationsDispatcher);
        return queryStatus;
    }

    /**
     * Updates the log (debug channel) with information on the current algorithm and which phase's
     * query was just submitted.
     *
     * @param log               the log of the concrete event handler class
     * @param clientQueryStatus the query status received from {@link IterationsEventHandler#submitQueryAndUpdateExecutionPhase}
     * @param ias               the {@code IterativeAlgorithmState} of the current algorithm
     */
    protected void updateLog(Logger log, AdpDBClientQueryStatus clientQueryStatus,
                             IterativeAlgorithmState ias) {
        if (log.isDebugEnabled() && clientQueryStatus != null)
            log.debug("Submitted [" + ias.getCurrentExecutionPhase() + "-phase] query for: "
                    + ias.toString());
    }

    /**
     * Wrapper for logging an error, cleaning-up {@link IterationsStateManager} <b>and signifying
     * an error to initiate erroneous response to client.</b>
     *
     * @param algorithmKey the key of the algorithm for which an error occurred, can be null in the
     *                     case where an {@link IterativeAlgorithmState} was already removed from
     *                     {@link IterationsStateManager}
     * @param queryID      the query ID of the query that was recently submitted (to be removed from
     *                     {@link IterationsStateManager}), can be null if the error occurred before
     *                     successful query submission
     * @param log          the log of the concrete event handler class
     * @param errMsg       the error message to be logged in the ERROR channel of the logger
     */
    protected void cleanupOnFailure(String algorithmKey, AdpDBQueryID queryID,
                                    Logger log, String errMsg) {
        if (errMsg != null)
            log.error(errMsg);

        if (algorithmKey != null) {
            IterativeAlgorithmState ias =
                    iterationsStateManager.getIterativeAlgorithm(algorithmKey);
            // Signify an error has occurred, so as to handle response to client.
            ias.signifyAlgorithmError();
            iterationsStateManager.removeIterativeAlgorithm(algorithmKey);
        }

        if (queryID != null)
            iterationsStateManager.removeQueryOfIterativeAlgorithm(queryID);
    }
}