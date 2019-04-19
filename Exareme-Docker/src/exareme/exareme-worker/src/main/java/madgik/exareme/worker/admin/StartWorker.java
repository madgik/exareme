package madgik.exareme.worker.admin;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import madgik.exareme.worker.art.manager.ArtManagerProperties;
import madgik.exareme.worker.art.security.DevelopmentContainerSecurityManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class StartWorker {

    private static final Logger log = Logger.getLogger(StartWorker.class);

    private static ArtManager manager = null;

    private StartWorker() {
    }

    public static void main(String[] args) throws Exception {
        log.info("Starting up Worker");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new DevelopmentContainerSecurityManager());
        }

        String masterRegistryIP = args[0];
        int registryPort = AdpProperties.getArtProps().getInt("art.registry.rmi.defaultPort");
        int dataTransferPort = AdpProperties.getArtProps().getInt("art.container.data.port");
        String logLevel = AdpProperties.getArtProps().getString("art.log.level");
        Logger.getRootLogger().setLevel(Level.toLevel(logLevel));

        manager = ArtManagerFactory.createRmiArtManager(
                new ArtManagerProperties(NetUtil.getIPv4(), registryPort, dataTransferPort));
        log.debug("Manager created!");

        manager.getRegistryManager().connectToRegistry(
                new EntityName("ArtRegistry", masterRegistryIP, registryPort, dataTransferPort));
        log.debug("Connected to registry!");

        manager.getContainerManager().startContainer();
        log.debug("Container started!");

        //        for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy()
        //            .getContainers()) {
        //            log.debug(containerProxy.getEntityName().getName());
        //        }

        log.info("Worker node started.");
        System.gc();
    }
}
