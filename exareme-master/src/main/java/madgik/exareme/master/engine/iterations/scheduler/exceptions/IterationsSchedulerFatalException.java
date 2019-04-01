package madgik.exareme.master.engine.iterations.scheduler.exceptions;

import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;

/**
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class IterationsSchedulerFatalException extends IterationsFatalException {
    /**
     * Constructs a {@code IterationsSchedulerFatalException} with the given detail message.
     *
     * @param message               the detail message of the {@code IterationsSchedulerFatalException}
     * @param erroneousAlgorithmKey the key of the algorithm tied to the exception (used for removal
     *                              from {@code IterationsStateManager}, can be null
     */
    public IterationsSchedulerFatalException(String message, String erroneousAlgorithmKey) {
        super(message);
        this.erroneousAlgorithmKey = erroneousAlgorithmKey;
    }

    /**
     * Constructs a {@code IterationsSchedulerFatalException} with the given root cause.
     *
     * @param cause                 the root cause of the {@code IterationsSchedulerFatalException}
     * @param erroneousAlgorithmKey the key of the algorithm tied to the exception (used for removal
     *                              from {@code IterationsStateManager}, can be null
     */
    public IterationsSchedulerFatalException(Throwable cause, String erroneousAlgorithmKey) {
        super(cause);
        this.erroneousAlgorithmKey = erroneousAlgorithmKey;
    }

    /**
     * Constructs a {@code IterationsSchedulerFatalException} with the given detail message
     * and root cause.
     *
     * @param message               the detail message of the {@code IterationsSchedulerFatalException}
     * @param cause                 the root cause of the {@code IterationsSchedulerFatalException}
     * @param erroneousAlgorithmKey the key of the algorithm tied to the exception (used for removal
     *                              from {@code IterationsStateManager}, can be null
     */
    public IterationsSchedulerFatalException(String message, Throwable cause,
                                             String erroneousAlgorithmKey) {
        super(message, cause);
        this.erroneousAlgorithmKey = erroneousAlgorithmKey;
    }
}
