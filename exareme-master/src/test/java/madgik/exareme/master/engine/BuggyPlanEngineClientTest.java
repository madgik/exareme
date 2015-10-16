package madgik.exareme.master.engine;

import madgik.exareme.master.app.cluster.ExaremeCluster;
import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author alex
 */
public class BuggyPlanEngineClientTest {
    private static final Logger log = Logger.getLogger(BuggyPlanEngineClientTest.class);

    @Before public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.ALL);
        log.info("------- SETUP --------");
        log.info("------- SETUP --------");
    }

    @Test public void testLocalDBManager() throws Exception {
        log.info("------- TEST --------");
        ExaremeCluster miniCluster = ExaremeClusterFactory.createMiniCluster(1098, 8088, 5);
        miniCluster.start();
        log.info("Mini cluster started.");
        try {

            String art = FileUtil.readFile(new File(BuggyPlanEngineClientTest.class.getClassLoader()
                .getResource("madgik/exareme/master/engine/buggyPlan2.json").getFile()));
            log.info("Art plan :" + art);

            ExecutionPlanParser planParser = new ExecutionPlanParser();
            ExecutionPlan executionPlan = planParser.parse(art);
            log.info("Parsed :" + executionPlan.toString());

            ExecutionEngineProxy engineProxy = ExecutionEngineLocator.getExecutionEngineProxy();
            ExecutionEngineSession engineSession = engineProxy.createSession();
            final ExecutionEngineSessionPlan sessionPlan = engineSession.startSession();
            sessionPlan.submitPlan(executionPlan);
            log.info("Submitted.");
            while (sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false
                && sessionPlan.getPlanSessionStatusManagerProxy().hasError() == false) {
                Thread.sleep(100);
            }
            log.info("Exited");
            if (sessionPlan.getPlanSessionStatusManagerProxy().hasError() == true) {
                log.error(sessionPlan.getPlanSessionStatusManagerProxy().getErrorList().get(0));
            }


            log.info("Submitting Again...");
            engineSession = engineProxy.createSession();
            final ExecutionEngineSessionPlan sessionPlan1 = engineSession.startSession();
            sessionPlan1.submitPlan(executionPlan);
            log.info("Submitted.");
            while (sessionPlan1.getPlanSessionStatusManagerProxy().hasFinished() == false
                && sessionPlan1.getPlanSessionStatusManagerProxy().hasError() == false) {
                Thread.sleep(100);
            }
            log.info("Exited");
            if (sessionPlan1.getPlanSessionStatusManagerProxy().hasError() == true) {
                log.error(sessionPlan1.getPlanSessionStatusManagerProxy().getErrorList().get(0));
            }


        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }

        Thread.sleep(10 * 1000);
        miniCluster.stop(true);
        log.info("Mini Cluster stopped.");
        log.info("------- TEST --------");
    }

    @After public void tearDown() throws Exception {
        log.info("------- CLEAN --------");
        log.info("------- CLEAN --------");
    }
}
