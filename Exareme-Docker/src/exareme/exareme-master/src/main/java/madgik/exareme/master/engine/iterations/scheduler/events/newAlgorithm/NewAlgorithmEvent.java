package madgik.exareme.master.engine.iterations.scheduler.events.newAlgorithm;

import madgik.exareme.master.engine.iterations.scheduler.events.IterationsEvent;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class NewAlgorithmEvent extends IterationsEvent {

    public NewAlgorithmEvent(String algorithmKey) {
        super(algorithmKey);
    }
}
