/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.master.engine.*;
import madgik.exareme.master.engine.executor.remote.AdpDBExecutorRemote;
import madgik.exareme.master.engine.queryCache.AdpDBQueryCache;
import madgik.exareme.master.engine.queryCache.inMemory.AdpDBInMemoryQueryCache;
import madgik.exareme.master.engine.statusMgr.inMemory.AdpDBStatusManagerInMemory;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class RmiAdpDBManager implements AdpDBManager {

    private static Logger log = Logger.getLogger(RmiAdpDBManager.class);
    private ArtManager manager = null;
    private AdpDBExecutor adpDBExecutor = null;
    private AdpDBOptimizer optimizer = null;
    private AdpDBStatusManager statusManager = null;
    private AdpDBQueryCache queryCache = null;

    public RmiAdpDBManager(ArtManager artManager) throws RemoteException {
        log.trace(" Develop mode : " + AdpProperties.getEnvProps().getString("run_level")
            .equals("develop"));
        if (AdpProperties.getEnvProps().getString("run_level").equals("develop")) {
            log.trace("StartingADPEngine...");
            startADPEngine();
        } else {
            log.trace("ConnectingADPEngine...");
            this.manager = artManager;
        }
        init();
    }

    public RmiAdpDBManager(String artRegistry, int artRegistryPort, int dtPort)
        throws RemoteException {
        if (AdpProperties.getEnvProps().getString("run_level").equals("develop")) {
            log.trace("StartingADPEngine...");
            startADPEngine();
        } else {
            log.trace("ConnectingADPEngine...");
            connectToADPEngine(artRegistry, artRegistryPort, dtPort);
        }
        init();
    }

    private void init() throws RemoteException {
        log.info("Initialize local managers.");
        this.statusManager = new AdpDBStatusManagerInMemory();
        this.adpDBExecutor = new AdpDBExecutorRemote(this.statusManager, manager);
        this.queryCache = new AdpDBInMemoryQueryCache();
        this.optimizer = new RmiAdpDBOptimizer(statusManager, manager, queryCache);
        AdpDBStatusManagerLocator.setStatusManager(statusManager);
        AdpDBManagerLocator.setDBManager(this);
    }

    @Override public AdpDBOptimizer getAdpDBOptimizer() throws RemoteException {
        return optimizer;
    }

    @Override public AdpDBExecutor getAdpDBExecutor() throws RemoteException {
        return adpDBExecutor;
    }

    @Override public AdpDBStatusManager getStatusManager() throws RemoteException {
        return statusManager;
    }

    @Override public void stopManager() throws RemoteException {
        log.debug("Stopping adp db executor ...");
        adpDBExecutor.stop();
        if (AdpProperties.getEnvProps().getString("run_level").equals("develop")) {
            log.debug("Stopping local adp engine ...");
            manager.stopManager(true);
            log.info("Local adp engine stopped!");
        }
    }

    private void startADPEngine() throws RemoteException {
        log.debug("Starting local adp engine ...");
        manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager().startArtRegistry();
        manager.getContainerManager().startContainer();
        manager.getExecutionEngineManager().startExecutionEngine();
        log.info("Local adp engine started!");
    }

    private void connectToADPEngine(String artRegistry, int artRegistryPort, int dtPort)
        throws RemoteException {
        log.debug("Connecting to remote adp engine ...");
        manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager()
            .connectToRegistry(new EntityName("ArtRegistry", artRegistry, artRegistryPort, dtPort));
        manager.getExecutionEngineManager().connectToExecutionEngine();
        log.info("Connected to remote adp engine!");
    }
}
