package madgik.exareme.master.engine.iterations;

/**
 * @author  Christos Aslanoglou <br>
 *          caslanoglou@di.uoa.gr <br>
 *          University of Athens / Department of Informatics and Telecommunications.
 */
public class IterationsException extends RuntimeException {
    /**
     * Constructs a IterationsException with the given detail message.
     *
     * @param message The detail message of the IterationsException.
     */
    public IterationsException(String message) {
        super(message);
    }

    /**
     * Constructs a IterationsException with the given root cause.
     *
     * @param cause The root cause of the IterationsException.
     */
    public IterationsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a IterationsException with the given detail message and root cause.
     *
     * @param message The detail message of the IterationsException.
     * @param cause   The root cause of the IterationsException.
     */
    public IterationsException(String message, Throwable cause) {
        super(message, cause);
    }
}
