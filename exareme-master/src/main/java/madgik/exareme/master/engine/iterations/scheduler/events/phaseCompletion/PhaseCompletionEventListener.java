package madgik.exareme.master.engine.iterations.scheduler.events.phaseCompletion;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class PhaseCompletionEventListener implements EventListener<PhaseCompletionEvent> {
    private static final Logger log = Logger.getLogger(PhaseCompletionEventListener.class);

    @Override
    public void processed(PhaseCompletionEvent event, RemoteException exception, EventProcessor processor) {
    }
}
