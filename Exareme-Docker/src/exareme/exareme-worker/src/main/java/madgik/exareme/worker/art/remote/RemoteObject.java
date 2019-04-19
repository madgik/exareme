/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is the RemoteObject interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface RemoteObject<T> extends Remote {

    String getRegEntryName() throws RemoteException;

    T createProxy() throws RemoteException;
}
