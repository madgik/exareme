/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.recovery.retryPolicy;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface RetryPolicy {

    boolean retry(Exception exception, int retries) throws RemoteException;
}
