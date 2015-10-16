/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote.retryPolicy;

import madgik.exareme.worker.art.remote.RetryTimeIntervalPolicy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExponentialTimeIntervalPolicy implements RetryTimeIntervalPolicy {

    public long getTime(int retries) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
