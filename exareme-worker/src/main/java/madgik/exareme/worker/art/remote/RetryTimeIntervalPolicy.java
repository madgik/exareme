/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import java.rmi.RemoteException;

/**
 * This is the RetryTimeIntervalPolicy interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface RetryTimeIntervalPolicy {

    long getTime(int retries) throws RemoteException;
}
