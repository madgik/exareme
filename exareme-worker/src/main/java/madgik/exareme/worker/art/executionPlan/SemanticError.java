/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class SemanticError extends RemoteException {
    private static final long serialVersionUID = 1L;

    public SemanticError(String msg) {
        super(msg);
    }
}
