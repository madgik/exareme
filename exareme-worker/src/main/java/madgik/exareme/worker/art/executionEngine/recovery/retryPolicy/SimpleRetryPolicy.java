/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.recovery.retryPolicy;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class SimpleRetryPolicy implements RetryPolicy {

    private int numOfFailures = 0;

    public SimpleRetryPolicy(int numOfFailures) {
        this.numOfFailures = numOfFailures;
    }

    @Override public boolean retry(Exception exception, int retries) throws RemoteException {
        return retries < numOfFailures;
    }
}
