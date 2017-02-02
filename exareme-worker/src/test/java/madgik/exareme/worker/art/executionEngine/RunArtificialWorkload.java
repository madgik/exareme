///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.worker.art.executionEngine;
//
//import madgik.exareme.utils.file.FileUtil;
//import madgik.exareme.worker.art.container.ContainerProxy;
//import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
//import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
//import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
//import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
//import madgik.exareme.worker.art.manager.ArtManager;
//import madgik.exareme.worker.art.manager.ArtManagerFactory;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//
//import java.io.File;
// TODO jctests
///**
// * @author herald
// */
//public class RunArtificialWorkload {
//    static ArtManager manager = null;
//
//    public static void main(String[] args) throws Exception {
//
//        Logger.getRootLogger().setLevel(Level.OFF);
//
//        manager = ArtManagerFactory.createRmiArtManager();
//        manager.getRegistryManager().startArtRegistry();
//        manager.getContainerManager().startContainer();
//        manager.getExecutionEngineManager().startExecutionEngine();
//
//    /* Read art script and  */
//        String art = FileUtil.readFile(new File(RunArtificialWorkload.class.getClassLoader()
//            .getResource("madgik/exareme/worker/art/HelloWorldDemo.json").getFile()));
//        ContainerProxy[] cont =
//            manager.getRegistryManager().getRegistry().createProxy().getContainers();
//        art = art.replace("localhost", cont[0].getEntityName().getName());
//        System.out.println(art);
//
//    /* Parse ART plan */
//        ExecutionPlanParser planParser = new ExecutionPlanParser();
//        ExecutionPlan executionPlan = planParser.parse(art);
//
//    /* Execute plan */
//        ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();
//        ExecutionEngineSession session = engine.createSession();
//        final ExecutionEngineSessionPlan sessionPlan = session.startSession();
//        sessionPlan.submitPlan(executionPlan);
//
//    /* Wait for termination */
//        while (sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false) {
//            Thread.sleep(100);
//        }
//        if (sessionPlan.getPlanSessionStatusManagerProxy().hasError()) {
//            sessionPlan.getPlanSessionStatusManagerProxy().getErrorList().get(0).printStackTrace();
//        }
//        System.exit(0);
//    }
//}
