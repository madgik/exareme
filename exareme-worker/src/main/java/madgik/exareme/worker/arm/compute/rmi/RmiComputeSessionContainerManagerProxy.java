/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.ComputeSessionContainerManager;
import madgik.exareme.worker.arm.compute.ComputeSessionContainerManagerProxy;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiComputeSessionContainerManagerProxy
    extends RmiObjectProxy<ComputeSessionContainerManager>
    implements ComputeSessionContainerManagerProxy {

    public ArmComputeSessionID sessionID = null;

    public RmiComputeSessionContainerManagerProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
    }

    @Override public ActiveContainer[] getContainers(int numOfContainers) throws RemoteException {
        return super.getRemoteObject().getContainers(numOfContainers, sessionID);
    }

    @Override public ActiveContainer[] tryGetContainers(int numOfContainers)
        throws RemoteException {
        return super.getRemoteObject().tryGetContainers(numOfContainers, sessionID);
    }

    @Override public ActiveContainer[] getAtMostContainers(int numOfContainers)
        throws RemoteException {
        return super.getRemoteObject().getAtMostContainers(numOfContainers, sessionID);
    }

    @Override public void stopContainer(ActiveContainer container) throws RemoteException {
        super.getRemoteObject().stopContainer(container, sessionID);
    }

    @Override public void closeSession() throws RemoteException {
        super.getRemoteObject().closeSession(sessionID);
    }

    @Override public ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers()
        throws RemoteException {
        return super.getRemoteObject().getAtMostContainers(sessionID);
    }

    @Override public void setPattern(ArrayList<PatternElement> pattern) throws RemoteException {
        super.getRemoteObject().setPattern(pattern, sessionID);
    }
}
