/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote.retryPolicy;

import madgik.exareme.worker.art.remote.RetryTimeIntervalPolicy;

import java.rmi.RemoteException;
import java.util.Random;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RandomTimeIntervalPolicy implements RetryTimeIntervalPolicy {
    private final Random rand = new Random();
    private long interval_ms = 0;

    public RandomTimeIntervalPolicy(long interval_ms) {
        this.interval_ms = interval_ms;
    }

    @Override public long getTime(int retries) throws RemoteException {
        return interval_ms + rand.nextInt(2000);
    }
}
