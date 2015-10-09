/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface Container extends RemoteObject<ContainerProxy>, SessionBased {


    ContainerJobResults execJobs(ContainerJobs jobs) throws RemoteException;

    ContainerStatus getStatus() throws RemoteException;

    void stopContainer() throws RemoteException;
}
