package madgik.exareme.worker.art;

/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
//package madgik.exareme.db.demo;
//
//import madgik.exareme.common.entity.EntityName;
//import madgik.exareme.db.manager.AdpManager;
//import madgik.exareme.db.manager.AdpManagerFactory;
//import madgik.exareme.master.queryProcessor.graph.AbstractQueryGraph;
//
///**
// * @author Herald Kllapi <br>
// *      University of Athens /
// *      Department of Informatics and Telecommunications.
// * @since 1.0
// */
//public class AbstractPlanExecution {
//
//    private static AdpManager manager = null;
//
//    public static void main(String[] args) throws Exception {
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
//
//        //     System.getProperties().put("java.rmi.server.logCalls","true");
//
//        manager = AdpManagerFactory.createRmiAdpManager();
//        manager.getArtManager().getRegistryManager().connectToRegistry(
//                new EntityName("ArtRegistry", "88.197.20.247", 1098));
//        manager.getResourceMediatorManager().connectToResourceMediator();
//        manager.getArtManager().getExecutionEngineManager().startExecutionEngine();
//
//        AbstractQueryGraph abstractGraph = new AbstractQueryGraph();
//
//    //    QueryScheduler optimizer = new PrologSimpleQueryOptimizer();
//
//     //   FinalExecutionPlan executionPlan = optimizer.abstractGraph);
//
//     //   QueryExecutor queryExecutor = new RmiQueryExecutor();
//
//    //    queryExecutor.execute(executionPlan);
//    }
//}
