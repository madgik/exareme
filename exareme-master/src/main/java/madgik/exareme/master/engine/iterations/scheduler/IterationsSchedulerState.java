package madgik.exareme.master.engine.iterations.scheduler;

import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterationsStateManagerImpl;
import madgik.exareme.utils.eventProcessor.EventProcessor;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
class IterationsSchedulerState {
    // Instance fields --------------------------------------------------------------------------
    private final IterationsScheduler iterationsScheduler;
    private final EventProcessor eventProcessor;
    private final IterationsStateManager iterationsStateManager;
    private final IterationsDispatcher iterationsDispatcher;

    IterationsSchedulerState(IterationsScheduler iterationsScheduler) {
        this.iterationsScheduler = iterationsScheduler;
        iterationsStateManager = IterationsStateManagerImpl.getInstance();
        iterationsDispatcher = IterationsDispatcher.getInstance(iterationsScheduler);
        eventProcessor = new EventProcessor(1);
        eventProcessor.start();
    }
}
