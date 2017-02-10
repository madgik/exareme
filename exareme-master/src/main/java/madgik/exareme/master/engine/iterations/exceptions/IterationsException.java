package madgik.exareme.master.engine.iterations.exceptions;

/**
 * @author  Christos Aslanoglou <br>
 *          caslanoglou@di.uoa.gr <br>
 *          University of Athens / Department of Informatics and Telecommunications.
 */
public class IterationsException extends Exception {
    /**
     * Constructs a IterationsException with the given detail message.
     *
     * @param message the detail message of the IterationsException
     */
    public IterationsException(String message) {
        super(message);
    }

    /**
     * Constructs a IterationsException with the given root cause.
     *
     * @param cause the root cause of the IterationsException
     */
    public IterationsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a IterationsException with the given detail message and root cause.
     *
     * @param message the detail message of the IterationsException
     * @param cause   the root cause of the IterationsException
     */
    public IterationsException(String message, Throwable cause) {
        super(message, cause);
    }
}
