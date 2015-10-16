/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute;

import madgik.exareme.worker.arm.compute.session.ArmComputeSession;
import madgik.exareme.worker.art.registry.Registerable;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi<br>
 *         herald@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ArmComputeProxy extends ObjectProxy<ArmCompute>, Registerable {

    ArmComputeSession createSession() throws RemoteException;
}
