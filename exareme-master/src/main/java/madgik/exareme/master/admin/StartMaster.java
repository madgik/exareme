package madgik.exareme.master.admin;

import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerFactory;
import madgik.exareme.master.gateway.ExaremeGateway;
import madgik.exareme.master.gateway.ExaremeGatewayFactory;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import madgik.exareme.worker.art.manager.ArtManagerProperties;
import madgik.exareme.worker.art.security.DevelopmentContainerSecurityManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Map;

public class StartMaster {

    private static final Logger log = Logger.getLogger(StartMaster.class);
    private static ArtManager manager = null;
    private static AdpDBManager dbManager = null;
    private static ExaremeGateway gateway = null;

    private StartMaster() {
    }

    public static void main(String[] args) throws Exception {
        log.info("Starting up master.");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new DevelopmentContainerSecurityManager());
        }
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            log.info(entry.getKey() + " = " + entry.getValue());
        }

        int registryPort = AdpProperties.getArtProps().getInt("art.registry.rmi.defaultPort");
        int dataTransferPort = AdpProperties.getArtProps().getInt("art.container.data.port");
        String logLevel = AdpProperties.getArtProps().getString("art.log.level");
        Logger.getRootLogger().setLevel(Level.toLevel(logLevel));

        manager = ArtManagerFactory.createRmiArtManager(
                new ArtManagerProperties(NetUtil.getIPv4(), registryPort, dataTransferPort));
        log.debug("Runtime manager created!");

        manager.getRegistryManager().startArtRegistry();
        log.debug("Registry started!");

        manager.getContainerManager().startContainer();
        log.debug("Container started!");

        manager.getExecutionEngineManager().startExecutionEngine();
        log.debug("Execution engine started!");

        manager.startGlobalQuantumClock();
        log.debug("Global quantum clock started");

        dbManager = AdpDBManagerFactory.createRmiManager(manager);
        log.debug("DB manager created");

        log.info("Starting gateway ...");
        gateway = ExaremeGatewayFactory.createHttpServer(dbManager);
        gateway.start();
        log.debug("Gateway Started.");

        log.info("Master node started.");
    }
}
