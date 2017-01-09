package madgik.exareme.master.engine.iterations.state.exceptions;

import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;

/**
 * A type of RuntimeException thrown when a fatal error regarding the iterations logic of
 * the IterationsStateManager module occurs.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class IterationsStateFatalException extends IterationsFatalException {
    /**
     * Constructs a {@code IterationsStateFatalException} with the given detail message.
     *
     * @param message the detail message of the {@code IterationsStateFatalException}
     */
    public IterationsStateFatalException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code IterationsStateFatalException} with the given root cause.
     *
     * @param cause the root cause of the {@code IterationsStateFatalException}
     */
    public IterationsStateFatalException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a {@code IterationsStateFatalException} with the given detail message
     * and root cause.
     *
     * @param message the detail message of the {@code IterationsStateFatalException}
     * @param cause   the root cause of the {@code IterationsStateFatalException}
     */
    public IterationsStateFatalException(String message, Throwable cause) {
        super(message, cause);
    }
}
