package madgik.exareme.master.engine.iterations.scheduler.events.newAlgorithm;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.iterations.scheduler.IterationsDispatcher;
import madgik.exareme.master.engine.iterations.scheduler.events.IterationsEventHandler;
import madgik.exareme.master.engine.iterations.scheduler.exceptions.IterationsSchedulerFatalException;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
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
     * @throws IterationsSchedulerFatalException if submission of the query and registering of the
     *                                           listener fails with RemoteException
     * @see IterativeAlgorithmState
     * @see madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel#init
     */
    @Override
    public void handle(NewAlgorithmEvent event, EventProcessor proc) throws RemoteException {
        IterativeAlgorithmState ias =
                iterationsStateManager.getIterativeAlgorithm(event.getAlgorithmKey());
        try {
            if (!ias.tryLock()) {
                log.debug("Lock was already acquired, exiting...");
                return;
            }

            AdpDBClientQueryStatus queryStatus = submitQueryAndUpdateExecutionPhase(ias, init);

            iterationsStateManager.submitQueryForIterativeAlgorithm(ias.getAlgorithmKey(),
                    queryStatus.getQueryID());

            ias.setCurrentExecutionPhase(init);

            updateLog(log, queryStatus, ias);

        } catch (RemoteException e) {
            String errMsg = "Failed to register listener for init phase of algorithm \""
                    + ias.toString() + "\"";
            log.error(errMsg, e);
            throw new IterationsSchedulerFatalException(errMsg, ias.getAlgorithmKey());
        } finally {
            ias.releaseLock();
        }
    }
}
