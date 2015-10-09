package madgik.exareme.master.engine.parser;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class SemanticError extends Exception {
    private static final long serialVersionUID = 1L;

    public SemanticError(String msg) {
        super(msg);
    }
}
