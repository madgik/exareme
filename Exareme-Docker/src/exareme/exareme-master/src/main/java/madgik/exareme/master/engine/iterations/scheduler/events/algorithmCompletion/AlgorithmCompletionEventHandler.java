package madgik.exareme.master.engine.iterations.scheduler.events.algorithmCompletion;

import madgik.exareme.master.engine.iterations.scheduler.IterationsDispatcher;
import madgik.exareme.master.engine.iterations.scheduler.events.IterationsEventHandler;
import madgik.exareme.master.engine.iterations.scheduler.events.newAlgorithm.NewAlgorithmEventHandler;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import org.apache.log4j.Logger;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class AlgorithmCompletionEventHandler extends IterationsEventHandler<AlgorithmCompletionEvent> {
    private static final Logger log = Logger.getLogger(NewAlgorithmEventHandler.class);


    public AlgorithmCompletionEventHandler(IterationsStateManager manager,
                                           IterationsDispatcher dispatcher) {
        super(manager, dispatcher);
    }

    /**
     * Cleans-up {@link IterationsStateManager} from {@link IterativeAlgorithmState} and signifies
     * algorithm completion.
     */
    @Override
    public void handle(AlgorithmCompletionEvent event, EventProcessor proc) {
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

        try {
            if (!ias.tryLock()) {
                log.debug("Lock was already acquired, exiting...");
                return;
            }
            // Clean up iterations state manager & signify completion.
            iterationsStateManager.removeIterativeAlgorithm(ias.getAlgorithmKey());
            ias.signifyAlgorithmCompletion();
            log.info(ias.getAlgorithmKey() + " terminated.");
        } finally {
            ias.releaseLock();
        }
    }
}
