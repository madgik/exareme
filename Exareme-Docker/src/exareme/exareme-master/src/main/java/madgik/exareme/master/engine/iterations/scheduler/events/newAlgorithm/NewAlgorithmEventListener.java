package madgik.exareme.master.engine.iterations.scheduler.events.newAlgorithm;

import madgik.exareme.utils.eventProcessor.EventListener;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class NewAlgorithmEventListener implements EventListener<NewAlgorithmEvent> {
    private static final Logger log = Logger.getLogger(NewAlgorithmEventListener.class);

    @Override
    public void processed(NewAlgorithmEvent event, RemoteException exception, EventProcessor processor) {

    }
}
