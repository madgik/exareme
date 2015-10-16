/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ComputeSessionContainerManagerProxy
    extends ObjectProxy<ComputeSessionContainerManager> {

    ActiveContainer[] getContainers(int numOfContainers) throws RemoteException;

    ActiveContainer[] tryGetContainers(int numOfContainers) throws RemoteException;

    ActiveContainer[] getAtMostContainers(int numOfContainers) throws RemoteException;

    ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers() throws RemoteException;

    void stopContainer(ActiveContainer container) throws RemoteException;

    void closeSession() throws RemoteException;

    void setPattern(ArrayList<PatternElement> pattern) throws RemoteException;

}
