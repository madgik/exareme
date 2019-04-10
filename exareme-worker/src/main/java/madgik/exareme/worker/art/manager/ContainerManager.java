/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager;

import madgik.exareme.worker.art.container.Container;

import java.rmi.RemoteException;

/**
 * This is the ContainerManager interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ContainerManager {

    Container getContainer() throws RemoteException;

    void startContainer() throws RemoteException;

    void stopContainer() throws RemoteException;

    boolean isUp() throws RemoteException;
}
