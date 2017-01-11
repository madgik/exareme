package madgik.exareme.master.engine.iterations.exceptions;

/**
 * A type of RuntimeException thrown when a fatal error regarding iterations logic occurs.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 *         Informatics and Telecommunications.
 */
public class IterationsFatalException extends RuntimeException {
    protected String erroneousAlgorithmKey;

    /**
     * Constructs a {@code IterationsFatalException} with the given detail message.
     *
     * @param message the detail message of the {@code IterationsFatalException}
     */
    public IterationsFatalException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code IterationsFatalException} with the given root cause.
     *
     * @param cause the root cause of the {@code IterationsFatalException}
     */
    public IterationsFatalException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a {@code IterationsFatalException} with the given detail message and root cause.
     *
     * @param message the detail message of the {@code IterationsFatalException}
     * @param cause   the root cause of the {@code IterationsFatalException}
     */
    public IterationsFatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErroneousAlgorithmKey() {
        return erroneousAlgorithmKey;
    }
}
