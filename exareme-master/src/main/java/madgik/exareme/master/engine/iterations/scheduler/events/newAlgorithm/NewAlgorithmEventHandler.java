package madgik.exareme.master.engine.iterations.scheduler.events.newAlgorithm;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.iterations.scheduler.IterationsDispatcher;
import madgik.exareme.master.engine.iterations.scheduler.events.IterationsEventHandler;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.engine.iterations.state.exceptions.IterationsStateFatalException;
import madgik.exareme.utils.eventProcessor.EventProcessor;

import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.init;


/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class NewAlgorithmEventHandler extends IterationsEventHandler<NewAlgorithmEvent> {
    private static final Logger log = Logger.getLogger(NewAlgorithmEventHandler.class);

    public NewAlgorithmEventHandler(IterationsStateManager manager,
                                    IterationsDispatcher dispatcher) {
        super(manager, dispatcher);
    }

    /**
     * Submits the initialization query of an iterative algorithm.
     *
     * <p>
     * Retrieves the related {@code IterativeAlgorithmState}, submits the initialization phase DFL
     * query and updates the {@code currentExecutionPhase} to {@code init}.
     *
     * Each handler locks the IterativeAlgorithmState in order to process it and submit the query.
     * This is done to ensure consistency in the case which the initialization query finishes even
     * before this handler exits.)
     *
     * @throws RemoteException if submission of the query and registering of the listener fails
     * @see IterativeAlgorithmState
     * @see madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel#init
     */
    @Override
    public void handle(NewAlgorithmEvent event, EventProcessor proc) throws RemoteException {
        if (log.isDebugEnabled())
            log.debug("Received new algorithm request [" + event.getAlgorithmKey() + "].");

        IterativeAlgorithmState ias =
                iterationsStateManager.getIterativeAlgorithm(event.getAlgorithmKey());
        if (ias == null) {
            // In this type of event, it is an error if the iterative algorithm state doesn't reside
            // in IterationsStateManager.
            // This should never happen, it is simply an assertion for programming error.
            log.error(IterativeAlgorithmState.class.getSimpleName() + " for algorithmKey: ["
                    + event.getAlgorithmKey() + "] doesn't exist in "
                    + IterationsStateManager.class.getSimpleName());
            return;
        }

        AdpDBClientQueryStatus queryStatus = null;
        try {
            /*
             Here we need a blocking lock acquirement (instead of tryLock), since the
             IterativeAlgorithmState of a new algorithm is already locked and will be unlocked
             when its IOCtrl (for notifying for either algorithm completion or algorithm error)
             has been set.
             Thus, the IterativeAlgorithmState is locked by IterationsHandler and unlocked
             by NIterativeAlgorithmResultEntity#produceContent after IOCtrl has been set.
             This is required in the case where init-phase query submission fails.
             */
            ias.lock();

            queryStatus = submitQueryAndUpdateExecutionPhase(ias, init);

            iterationsStateManager.submitQueryForIterativeAlgorithm(ias.getAlgorithmKey(),
                    queryStatus.getQueryID());

            ias.setCurrentExecutionPhase(init);

            updateLog(log, queryStatus, ias);

        } catch (RemoteException | IterationsStateFatalException e) {
            String errMsg = ias.toString() + ": initiation failed";
            AdpDBQueryID queryID = queryStatus != null ? queryStatus.getQueryID() : null;
            cleanupOnFailure(ias.getAlgorithmKey(), queryID, log, errMsg);
            if (e instanceof RemoteException) {
                // In case of RemoteException we need to throw it so that EventHandlerRunnable
                // catches it.
                throw e;
            }
        } finally {
            ias.releaseLock();
        }
    }
}
