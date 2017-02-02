///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.worker.art.registry;
//
//import madgik.exareme.worker.art.container.ContainerProxy;
//import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
//import madgik.exareme.worker.art.manager.ArtManager;
//import madgik.exareme.worker.art.manager.ArtManagerFactory;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//
///**
// * @author Dimitris Paparas<br>
// *         University of Athens /
// *         Department of Informatics and Telecommunications.
// * @since 1.0
// */
//public class RegisterTest {
//
//    private static Logger log = Logger.getLogger(RegisterTest.class);
//
//    public static void main(String argv[]) throws Exception {
//        Logger.getRootLogger().setLevel(Level.ALL);
//        ArtManager manager = ArtManagerFactory.createRmiArtManager();
//        manager.getRegistryManager().startArtRegistry();
//        manager.getContainerManager().startContainer();
//        manager.getExecutionEngineManager().startExecutionEngine();
//
//        //regManager.
//
//        ArtRegistryProxy regProxy = manager.getRegistryManager().getRegistry().createProxy();
//
//
//        ContainerProxy c[] = regProxy.getContainers();
//
//        for (ContainerProxy cp : c) {
//            log.debug(cp.getEntityName().getName());
//        }
//
//        ExecutionEngineProxy ex[] = regProxy.getExecutionEngines();
//
//        for (ExecutionEngineProxy p : ex) {
//            log.debug(p.getEntityName().getName());
//        }
//
//        //        log.debug(regProxy.getLoggers().size());
//
//    }
//}
