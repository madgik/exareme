/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * This is the RetryTimesPolicy interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface RetryTimesPolicy extends Serializable {

    boolean retry(Exception exception, int retries) throws RemoteException;
}
