/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote.retryPolicy;

import madgik.exareme.worker.art.remote.RetryTimeIntervalPolicy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ConstantTimeIntervalPolicy implements RetryTimeIntervalPolicy {

    private long interval_ms = 0;

    public ConstantTimeIntervalPolicy(long interval_ms) {
        this.interval_ms = interval_ms;
    }

    @Override
    public long getTime(int retries) throws RemoteException {
        return interval_ms;
    }
}
