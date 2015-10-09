/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.containerMgr;

import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi<br>
 *         herald@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ContainerManager extends RemoteObject<ContainerManagerProxy> {

    ContainerManagerStatus getStatus() throws RemoteException;
}
