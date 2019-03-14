/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.manager.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.executionEngine.ExecutionEngine;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineFactory;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.manager.ExecutionEngineManager;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;

import javax.activity.ActivityRequiredException;
import java.rmi.RemoteException;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiExecutionEngineManager implements ExecutionEngineManager {
    private static RmiArtManager artManager = null;
    private static ExecutionEngine engine = null;
    private static boolean isLocalExecutionEngine = false;

    public RmiExecutionEngineManager(RmiArtManager artManager) {
        RmiExecutionEngineManager.artManager = artManager;
    }

    @Override
    public boolean isOnline() {
        return (engine != null);
    }

    @Override
    public ExecutionEngine getExecutionEngine() {
        return engine;
    }

    @Override
    public void startExecutionEngine() throws RemoteException {
        if (artManager.getRegistryManager().isOnline()) {
            String engineType = AdpProperties.getArtProps().getString("art.scheduler.mode");
            if (engineType == null) {
                engineType = "centralized";
            }
            if (engineType.equalsIgnoreCase("centralized")) {
                engine = ExecutionEngineFactory.createRmiDynamicExecutionEngine(
                        ArtRegistryLocator.getLocalRmiRegistryEntityName());
            } else {
                throw new RemoteException("Mode not supported: " + engineType);
            }

            ExecutionEngineLocator.setExecutionEngine(engine.createProxy());
            ArtRegistryLocator.getArtRegistryProxy().registerExecutionEngine(engine.createProxy());

            isLocalExecutionEngine = true;
        } else {
            throw new ActivityRequiredException("Art Registry offline!");
        }
    }

    @Override
    public void connectToExecutionEngine(EntityName epr) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connectToExecutionEngine() throws RemoteException {
        if (artManager.getRegistryManager().isOnline()) {
            try {
                ExecutionEngineProxy engineProxy =
                        ArtRegistryLocator.getArtRegistryProxy().getExecutionEngines()[0];

                engine = engineProxy.connect();
                ExecutionEngineLocator.setExecutionEngine(engine.createProxy());
                isLocalExecutionEngine = false;
            } catch (RemoteException e) {
                throw new RemoteException("Cannot connect to execution engine", e);
            }
        } else {
            throw new ActivityRequiredException("Art Registry offline!");
        }
    }

    @Override
    public void stopExecutionEngine() throws RemoteException {
        stopExecutionEngine(false);
    }

    @Override
    public void stopExecutionEngine(boolean force) throws RemoteException {
        if (isLocalExecutionEngine) {
            engine.stopExecutionEngine(force);
        }
    }
}
