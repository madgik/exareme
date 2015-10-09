/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager.rmi;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.managementBean.ManagerManagement;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtRegistryManager;
import madgik.exareme.worker.art.manager.ContainerManager;
import madgik.exareme.worker.art.manager.ExecutionEngineManager;
import madgik.exareme.worker.art.quantumClock.GlobalQuantumClock;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiArtManager implements ArtManager {
    private static ArtRegistryManager artRegistryManager = null;
    private static ContainerManager containerManager = null;
    private static ExecutionEngineManager engineManager = null;
    private static GlobalQuantumClock globalQuantumClock = null;
    private static Logger log = Logger.getLogger(RmiArtManager.class);
    private static int dataTransferPort;

    public RmiArtManager(String containerName, long cId, int localRegistryPort,
        int dataTransferPort) throws RemoteException {
        RmiArtManager.dataTransferPort = dataTransferPort;
        artRegistryManager = new RmiArtRegistryManager(localRegistryPort, this);
        containerManager = new RmiContainerManager(containerName, cId, this, dataTransferPort);
        engineManager = new RmiExecutionEngineManager(this);
        globalQuantumClock =
            new GlobalQuantumClock(AdpProperties.getCloudProps().getLong("cloud.warnTime") * 1000,
                AdpProperties.getCloudProps().getLong("cloud.quantum") * 1000);
        try {
            ManagerManagement artManager = new ManagerManagement(this);
            madgik.exareme.utils.managementBean.ManagementUtil
                .registerMBean(artManager, "ArtManager");
            log.info("Art manager bean registered!");
        } catch (Exception e) {
            log.error("Art manager registration error (already running?)!", e);
        }
    }

    @Override public ArtRegistryManager getRegistryManager() {
        return artRegistryManager;
    }

    @Override public ExecutionEngineManager getExecutionEngineManager() {
        return engineManager;
    }

    @Override public ContainerManager getContainerManager() {
        return containerManager;
    }

    @Override public void startGlobalQuantumClock() throws RemoteException {
        globalQuantumClock.startDeamon();
    }

    @Override public void stopManager() throws RemoteException {
        stopManager(false);
    }

    @Override public void stopManager(boolean force) throws RemoteException {
        if (engineManager.isOnline()) {
            engineManager.stopExecutionEngine(force);
        }
        if (containerManager.isUp()) {
            containerManager.stopContainer();
        }
        if (artRegistryManager.isOnline()) {
            artRegistryManager.stopArtRegistry();
        }
        globalQuantumClock.stopDeamon();
    }
}
