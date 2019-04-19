package madgik.exareme.master.engine.iterations.scheduler.events.algorithmCompletion;

import madgik.exareme.master.engine.iterations.scheduler.events.IterationsEvent;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class AlgorithmCompletionEvent extends IterationsEvent {
    public AlgorithmCompletionEvent(String algorithmKey) {
        super(algorithmKey);
    }
}
