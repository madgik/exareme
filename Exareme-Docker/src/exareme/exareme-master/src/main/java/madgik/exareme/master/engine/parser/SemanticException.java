package madgik.exareme.master.engine.parser;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class SemanticException extends RemoteException {

    private static final long serialVersionUID = 1L;

    public SemanticException(String msg) {
        super(msg);
    }

    public SemanticException(String msg, Exception e) {
        super(msg, e);
    }
}
