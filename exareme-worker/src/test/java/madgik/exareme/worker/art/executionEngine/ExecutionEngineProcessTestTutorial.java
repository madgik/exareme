/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine;

import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExecutionEngineProcessTestTutorial {

    private static ArtManager manager = null;
    private static Logger log = Logger.getLogger(ExecutionEngineProcessTestTutorial.class);

    public static void main(String[] args) throws Exception {

        log.setLevel(Level.ALL);

        //    System.setSecurityManager(new ContainerSecurityManager());

        manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager().startArtRegistry();
        manager.getContainerManager().startContainer();
        manager.getExecutionEngineManager().startExecutionEngine();

        ContainerProxy container = ArtRegistryLocator.getArtRegistryProxy().getContainers()[0];

        String planString = new String();

        //        planString +=
        //                "container c ('" + container.getEntityName().getName() + "',1099);\n" +
        //                "instantiate hello c('madgik.exareme.db.operatorLibrary.test.HelloWorld2')\n" +
        //                "{This is a query string for hello world operator!};\n" +
        //                "create hello_out c('10');\n" +
        //                "connect c(hello, hello_out);\n";

        planString += "container c ('" + container.getEntityName().getName() + "',1099);\n"
                + "instantiate hello c('madgik.exareme.db.operatorLibrary.test.HelloWorld2');\n"
                + "instantiate print c('madgik.exareme.db.operatorLibrary.test.Print');\n"
                + "create hello_out c('10');\n" + "connect c(hello, hello_out);\n"
                + "connect c(hello_out, print);\n";

        long start = System.currentTimeMillis();

        ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();

        ExecutionPlanParser parser = new ExecutionPlanParser();
        final ExecutionPlan plan = parser.parse(planString.toString());

        ExecutionEngineSession session = engine.createSession();
        final ExecutionEngineSessionPlan sessionPlan = session.startSession();
        sessionPlan.submitPlan(plan);

        if (sessionPlan.hasError()) {
            log.debug("Errors!");
            for (Exception e : sessionPlan.getPlanSessionStatusManagerProxy().getErrorList()) {
                e.printStackTrace();
            }
            System.exit(0);
        }

        Thread.sleep(1000);
        //    log.debug("Waiting to finish!");
        while (sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false || sessionPlan
                .getPlanSessionStatusManagerProxy().hasError()) {
            Thread.sleep(100);
        }
        //     log.debug("Finished!");

        //        new Thread() {
        //            @Override
        //            public void run() {
        //                try {
        //                    while(true) {
        //
        //                    log.debug(
        //                            sessionPlan.getPlanSessionStatusManagerProxy().
        //                            getStatistics().finishedOperators + " / " +
        //                            sessionPlan.getPlanSessionStatusManagerProxy().
        //                            getStatistics().totalOperators);
        //
        //                    Thread.sleep(100);
        //                    }
        //                } catch (Exception ex) {
        //                    ex.printStackTrace();
        //                }
        //            }
        //        }.start();


        long end = System.currentTimeMillis();

        log.debug("Running Time = " + (end - start));
        //        log.debug("Running Time = " + sessionPlan.getPlanSessionStatusManagerProxy().getStatistics().runningTime);

        // call a method
        //        log.debug(sessionPlan.getPlanSessionStatusManagerProxy().callMethod(
        //                plan.getOperator("map"), "hello", new Object[] { "it works!" }));
        //
        sessionPlan.close();

        manager.stopManager();
        log.debug("Finished!");

        //    System.exit(0);
    }
}
