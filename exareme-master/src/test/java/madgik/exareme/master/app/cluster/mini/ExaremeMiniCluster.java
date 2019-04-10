package madgik.exareme.master.app.cluster.mini;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.master.app.cluster.ExaremeCluster;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerFactory;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import madgik.exareme.worker.art.manager.ArtManagerProperties;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import madgik.exareme.worker.art.security.DevelopmentContainerSecurityManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;

/**
 * env.properties
 * run_level=testing
 *
 * @author alex
 */
public class ExaremeMiniCluster implements ExaremeCluster {
    private static final Logger log = Logger.getLogger(ExaremeMiniCluster.class);
    // configuration
    String dbsPath;
    private int port;
    private int dtport;
    private boolean running;
    // master
    private ArtManager master;
    private AdpDBManager manager;
    // workers
    private int nworkers;
    private ArtManager[] workers;

    public ExaremeMiniCluster(int port, int dtport, int nworkers) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new DevelopmentContainerSecurityManager());
        }
        Path madisPath = Paths
                .get(System.getProperty("user.dir") + "/../exareme-tools/madis/src/mterm.py");
        if (!Files.exists(madisPath)) {
            madisPath = Paths.get(System.getProperty("user.dir") + "/exareme-tools/madis/src/mterm.py");
        }
        String relMadisPath = madisPath.toString();
        if (System.getenv("EXAREME_MADIS") != null) {
            log.info("**--" + System.getenv("EXAREME_MADIS"));
        } else if (new File(relMadisPath).exists()) {
            log.info("Relative madis Path : " + relMadisPath);
            System.setProperty("EXAREME_PYTHON", "python");
            System.setProperty("EXAREME_MADIS", relMadisPath);
            System.setProperty("MADIS_PATH", relMadisPath);
            log.info("**--" + System.getProperty("EXAREME_MADIS"));
            log.info("**--" + relMadisPath);


        } else
            throw new RuntimeException("Provide valid engine path.(" + relMadisPath + ").");
        this.port = port;
        this.dtport = dtport;
        this.nworkers = nworkers;
        this.master = null;
        this.workers = null;
        this.running = false;
        this.dbsPath = null;
    }

    @Override
    public void start() throws RemoteException {
        if (this.running) {
            log.info("Already running!");
            return;
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new DevelopmentContainerSecurityManager());
        }
        log.info("Starting...");
        String iPv4 = NetUtil.getIPv4();

        // master init
        this.master =
                ArtManagerFactory.createRmiArtManager(new ArtManagerProperties(iPv4, port, dtport));
        log.debug("Master created.");

        master.getRegistryManager().startArtRegistry();
        log.debug("Master Registry started.");

        master.getContainerManager().startContainer();
        log.debug("Master Container started.");

        master.getExecutionEngineManager().startExecutionEngine();
        log.debug("Master Engine stared");

        master.startGlobalQuantumClock();
        log.debug("Master GlobalClock started.");

        manager = AdpDBManagerFactory.createRmiManager(this.master);
        log.debug("Master DBManager created.");

        //workers init
        if (nworkers > 0) {
            this.workers = new ArtManager[nworkers];
            for (int i = 0; i < nworkers; i++) {
                this.workers[i] = ArtManagerFactory.createRmiArtManager(
                        new ArtManagerProperties(iPv4 + "_" + String.valueOf(i), port, dtport + i + 1));
                log.debug("Worker(" + i + ") created.");
                this.workers[i].getRegistryManager()
                        .connectToRegistry(new EntityName("ArtRegistry", iPv4, port, dtport));
                log.debug("Worker(" + i + ") connected to Registry.");
                this.workers[i].getContainerManager().startContainer();
                log.debug("Worker(" + i + ") Container started.");

            }
        }
        this.running = true;
        log.info("Started.");
    }

    @Override
    public boolean isUp() {
        return running;
    }

    @Override
    public void stop(boolean force) throws RemoteException {
        if (!this.running) {
            log.info("Already stopped.");
            return;
        }

        this.manager.stopManager();
        log.debug("Master DBManager stopped.");

        this.master.stopManager(force);
        log.debug("Master stopped.");

        if (nworkers > 0) {
            for (int i = 0; i < nworkers; i++) {
                this.workers[i].stopManager(force);
                log.debug("Worker (" + i + ") stopped.");
            }
        }
    }


    @Override
    public AdpDBClient getExaremeClusterClient(AdpDBClientProperties properties)
            throws RemoteException {
        if (!this.running) {
            log.info("Nothing is running.");
            return null;
        }
        return AdpDBClientFactory.createDBClient(this.manager, properties);
    }

    @Override
    public Container[] getContainers() throws RemoteException {
        ContainerProxy[] proxies = ArtRegistryLocator.getArtRegistryProxy().getContainers();
        Container[] containers = new Container[proxies.length];

        for (int i = 0; i < proxies.length; i++) {
            containers[i] =
                    new Container(String.format("c%1d", i), proxies[i].getEntityName().getName(),
                            proxies[i].getEntityName().getPort(),
                            proxies[i].getEntityName().getDataTransferPort());
            log.info(String.format("--+ c%1d", i));
        }

        return containers;
    }

    @Override
    public AdpDBManager getDBManager() throws RemoteException {
        return manager;
    }

}
