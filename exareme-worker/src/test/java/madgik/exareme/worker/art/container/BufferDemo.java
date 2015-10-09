/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
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
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class BufferDemo {

    private static ArtManager manager = null;
    private static Logger log = Logger.getLogger(BufferDemo.class);

    public static void main(String[] args) throws Exception {

        Logger.getRootLogger().setLevel(Level.INFO);

        manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager().startArtRegistry();
        manager.getContainerManager().startContainer();
        manager.getExecutionEngineManager().startExecutionEngine();

        ContainerProxy container = ArtRegistryLocator.getArtRegistryProxy().getContainers()[0];

        String planString = new String();

        planString += "container c ('" + container.getEntityName().getName() + "',1099);\n"
            + "instantiate hello1 c('madgik.exareme.db.operatorLibrary.test.HelloWorld');\n"
            + "instantiate hello2 c('madgik.exareme.db.operatorLibrary.test.HelloWorld');\n"
            + "instantiate merge1 c('madgik.exareme.db.operatorLibrary.test.Merge');\n"
            + "instantiate file c('madgik.exareme.db.operatorLibrary.test.WriteToTextFile', file='/tmp/result.txt');\n"
            + "create hello1_out c('10');\n" + "create hello2_out c('10');\n"
            + "create merge1_out c('10');\n" + "connect c(hello1, hello1_out);\n"
            + "connect c(hello2, hello2_out);\n" + "connect c(merge1, merge1_out);\n"
            + "connect c(hello1_out, merge1);\n" + "connect c(hello2_out, merge1);\n"
            + "connect c(merge1_out, file);\n";

        //	planString +=
        //		"container c ('" + container.getEntityName().getName() + "',1099);\n"
        //		+ "instantiate pr c('madgik.exareme.db.operatorLibrary.stream.StreamProducer', behavior='pipeline');\n"
        //		+ "instantiate co c('madgik.exareme.db.operatorLibrary.stream.StreamConsumer', behavior='pipeline');\n"
        //		+ "create pr_out c('10');\n"
        //		+ "connect c(pr, pr_out);\n"
        //		+ "connect c(pr_out, co);\n";

        ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();

        ExecutionPlanParser parser = new ExecutionPlanParser();
        final ExecutionPlan plan = parser.parse(planString.toString());

        ExecutionEngineSession session = engine.createSession();

        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            final ExecutionEngineSessionPlan sessionPlan = session.startSession();
            sessionPlan.submitPlan(plan);
            while (sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false
                || sessionPlan.getPlanSessionStatusManagerProxy().hasError()) {
                Thread.sleep(100);
            }
            log.debug(
                sessionPlan.getPlanSessionStatisticsManagerProxy().getStatistics().toString());
            sessionPlan.close();

            long end = System.currentTimeMillis();

            log.debug("Running Time = " + (end - start));
        }

        manager.stopManager();
        log.debug("Finished!");

        System.exit(0);
    }
}
