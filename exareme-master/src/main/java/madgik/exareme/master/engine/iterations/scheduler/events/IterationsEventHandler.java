package madgik.exareme.master.engine.iterations.scheduler.events;

import madgik.exareme.master.engine.iterations.scheduler.IterationsDispatcher;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
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
}