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

import java.rmi.RemoteException;
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
        String logLevel = System.getenv("LOG_LEVEL");
        Logger.getRootLogger().setLevel(Level.toLevel(logLevel));

        try {
            manager = ArtManagerFactory.createRmiArtManager(
                    new ArtManagerProperties(NetUtil.getIPv4(), registryPort, dataTransferPort));
        } catch (RemoteException e){
            throw new RemoteException("\"Master node ["+NetUtil.getIPv4()+"] is unable to create Runtime manager.");
        }
        log.debug("Runtime manager created!");

        try {
            manager.getRegistryManager().startArtRegistry();
        }catch (RemoteException e){
            throw new RemoteException("\"Master node ["+NetUtil.getIPv4()+"] is unable to start Exareme's Registry.");
        }
        log.debug("Registry started!");

        try {
            manager.getContainerManager().startContainer();
        }catch (RemoteException e){
            throw new RemoteException("\"Master node ["+NetUtil.getIPv4()+"] is unable to start container.");
        }
        log.debug("Container started!");

        try {
            manager.getExecutionEngineManager().startExecutionEngine();
        }catch (RemoteException e){
            throw new RemoteException("\"Master node ["+NetUtil.getIPv4()+"] is unable to start Execution engine.");
        }
        log.debug("Execution engine started!");

        try{
        manager.startGlobalQuantumClock();
        }catch (RemoteException e){
            throw new RemoteException("\"Master node ["+NetUtil.getIPv4()+"] is unable to start GlobalQuantumClock.");
        }
        log.debug("Global quantum clock started");

        try{
        dbManager = AdpDBManagerFactory.createRmiManager(manager);
        }catch (RemoteException e){
            throw new RemoteException("\"Master node ["+NetUtil.getIPv4()+"] is unable to create DB Manager.");
        }
        log.debug("DB manager created");


        log.info("Starting gateway ...");
        try {
            gateway = ExaremeGatewayFactory.createHttpServer(dbManager);
            gateway.start();
        }catch (RemoteException e){
            throw new RemoteException("Gateway is unable to start.");
        }
        log.debug("Gateway Started.");

        System.out.println("Master node started.");
    }
}
