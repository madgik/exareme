/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.local;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.arm.compute.cluster.PatternElement;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerInterface;
import madgik.exareme.worker.arm.compute.containerMgr.ContainerManagerStatus;
import madgik.exareme.worker.arm.compute.session.ActiveContainer;
import madgik.exareme.worker.arm.compute.session.ArmComputeSessionID;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import madgik.exareme.worker.art.manager.ContainerManager;
import org.apache.log4j.Logger;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * @author herald
 */
public class LocalArmComputeInterface implements ContainerManagerInterface {

    private static Logger log = Logger.getLogger(LocalArmComputeInterface.class);
    private final Object lock = new Object();
    private ContainerManagerStatus managerStatus = null;
    private ContainerManager manager = null;
    private boolean created = false;
    private HashMap<Long, Boolean> sessionStatus = new HashMap<Long, Boolean>();
    private Semaphore sem = new Semaphore(1);

    public LocalArmComputeInterface(ContainerManagerStatus managerStatus) throws RemoteException {
        this.managerStatus = managerStatus;
        try {
            this.manager = ArtManagerFactory.createRmiArtManager().getContainerManager();
            log.debug("Local arm compute created!");
        } catch (Exception e) {
            throw new ServerException("Cannot create local compute interface", e);
        }
    }

    @Override
    public ActiveContainer[] getContainers(int numOfContainers, ArmComputeSessionID sessionID)
        throws RemoteException {
        try {
            sem.acquire();
            sessionStatus.put(sessionID.getId(), true);
            if (created == true) {
                throw new AccessException("Container limit reached!");
            }
            manager.startContainer();

            ActiveContainer activeContainer =
                new ActiveContainer(0, manager.getContainer().createProxy().getEntityName(), 0);

            created = true;
            return new ActiveContainer[] {activeContainer};
        } catch (Exception e) {
            throw new AccessException("Cannot get containers", e);
        }
    }

    @Override
    public ActiveContainer[] tryGetContainers(int numOfContainers, ArmComputeSessionID sessionID)
        throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ActiveContainer[] getAtMostContainers(int numOfContainers, ArmComputeSessionID sessionID)
        throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void stopContainer(ActiveContainer container, ArmComputeSessionID sessionID)
        throws RemoteException {
        manager.stopContainer();
        created = false;
    }

    @Override public ContainerManagerStatus getStatus() throws RemoteException {
        return managerStatus;
    }

    @Override public void closeSession(ArmComputeSessionID sessionID) throws RemoteException {
        synchronized (lock) {
            if (sessionStatus.containsKey(sessionID.getId())) {
                sessionStatus.remove(sessionID.getId());
                log.debug("Close session");
                sem.release();
            }
        }
    }

    @Override public void startManager() throws RemoteException {
    /* Do nothing */
    }

    @Override public void stopManager() throws RemoteException {
    /* Do nothing */
    }

    @Override
    public void setPattern(ArrayList<PatternElement> pattern, ArmComputeSessionID sessionID)
        throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public ArrayList<Pair<PatternElement, ActiveContainer>> getAtMostContainers(
        ArmComputeSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
