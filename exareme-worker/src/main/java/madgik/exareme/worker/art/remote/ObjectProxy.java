/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * This is the ObjectProxy interface.
 *
 * @param <T> The remote object type.
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ObjectProxy<T> extends Serializable {

    // Connect to the remore object.
    T connect() throws RemoteException;

    // Access the remote object.
    T getRemoteObject() throws RemoteException;

    // Get the retry policy of the object.
    RetryPolicy getRetryPolicy() throws RemoteException;
}
