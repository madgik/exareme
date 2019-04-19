/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.containerMgr;

import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.arm.compute.session.ContainerManagerSession;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi<br>
 * herald@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ContainerManagerProxy extends ObjectProxy<ContainerManager> {

    ContainerManagerSession createSession(ArmComputeSessionID sessionID) throws RemoteException;
}
