/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote.retryPolicy;

import madgik.exareme.worker.art.remote.RetryTimesPolicy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RetryForeverPolicy implements RetryTimesPolicy {
    private static final long serialVersionUID = 1L;

    @Override public boolean retry(Exception exception, int retries) throws RemoteException {
        return true;
    }
}
