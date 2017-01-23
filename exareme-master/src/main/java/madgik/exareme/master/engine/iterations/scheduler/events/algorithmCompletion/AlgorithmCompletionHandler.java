package madgik.exareme.master.engine.iterations.scheduler.events.algorithmCompletion;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import madgik.exareme.master.engine.iterations.scheduler.IterationsDispatcher;
import madgik.exareme.master.engine.iterations.scheduler.events.IterationsEventHandler;
import madgik.exareme.master.engine.iterations.scheduler.events.newAlgorithm.NewAlgorithmEventHandler;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.utils.eventProcessor.EventProcessor;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class AlgorithmCompletionHandler extends IterationsEventHandler<AlgorithmCompletionEvent> {
    private static final Logger log = Logger.getLogger(NewAlgorithmEventHandler.class);


    public AlgorithmCompletionHandler(IterationsStateManager manager,
                                      IterationsDispatcher dispatcher) {
        super(manager, dispatcher);
    }

    @Override
    public void handle(AlgorithmCompletionEvent event, EventProcessor proc) throws RemoteException {
        IterativeAlgorithmState ias =
                iterationsStateManager.getIterativeAlgorithm(event.getAlgorithmKey());
        try {
            if (!ias.tryLock()) {
                log.debug("Lock was already acquired, exiting...");
                return;
            }
            if (log.isDebugEnabled())
                log.info("Iterative algorithm " + ias.getAlgorithmKey() + " terminated.");
        } finally {
            ias.releaseLock();
        }
    }
}
