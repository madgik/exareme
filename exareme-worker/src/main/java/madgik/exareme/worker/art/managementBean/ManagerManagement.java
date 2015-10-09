/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtRegistryManager;
import madgik.exareme.worker.art.manager.ContainerManager;
import madgik.exareme.worker.art.manager.ExecutionEngineManager;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ManagerManagement implements ManagerManagementMBean {
    private ArtManager manager = null;
    private ArtRegistryManager artRegistryManager = null;
    private ContainerManager containerManager = null;
    private ExecutionEngineManager engineManager = null;

    public ManagerManagement(ArtManager manager) throws RemoteException {
        this.manager = manager;
        this.artRegistryManager = manager.getRegistryManager();
        this.containerManager = manager.getContainerManager();
        this.engineManager = manager.getExecutionEngineManager();
    }

    @Override public boolean isRegistryOnline() throws RemoteException {
        return artRegistryManager.isOnline();
    }

    @Override public String startRegistry() throws RemoteException {
        artRegistryManager.startArtRegistry();
        return "Registry online!";
    }

    @Override public String stopRegistry() throws RemoteException {
        artRegistryManager.stopArtRegistry();
        return "Registry offline!";
    }

    @Override public String connectToRegistry(String ip, int port) throws RemoteException {
        EntityName regName = new EntityName("ArtRegistry", ip, port);
        artRegistryManager.connectToRegistry(regName);
        return "Registry online!";
    }

    @Override public boolean isExecutionEngineOnline() throws RemoteException {
        return engineManager.isOnline();
    }

    @Override public String startExecutionEngine() throws RemoteException {
        engineManager.startExecutionEngine();
        return "Engine online!";
    }

    @Override public String stopExecutionEngine() throws RemoteException {
        engineManager.stopExecutionEngine();
        return "Engine offline!";
    }

    @Override public String connectToExecutionEngine() throws RemoteException {
        engineManager.connectToExecutionEngine();
        return "Connected to Execution Engine!";
    }

    @Override public String getExecutionEngineRegEntryName() throws RemoteException {
        return engineManager.getExecutionEngine().getRegEntryName();
    }

    @Override public boolean isContainerUp() throws RemoteException {
        return containerManager.isUp();
    }

    @Override public String startContainer() throws RemoteException {
        containerManager.startContainer();
        return "Contaner is up!";
    }

    @Override public String stopContainer() throws RemoteException {
        containerManager.stopContainer();
        return "Contaner is down!";
    }

    @Override public String getContainerRegEntryName() throws RemoteException {
        return containerManager.getContainer().getRegEntryName();
    }
}
