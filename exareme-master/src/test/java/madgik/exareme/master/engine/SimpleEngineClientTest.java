package madgik.exareme.master.engine;

import madgik.exareme.master.app.cluster.ExaremeCluster;
import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionPlan.EditableExecutionPlanImpl;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;
import madgik.exareme.worker.art.executionPlan.parser.expression.Operator;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author alex
 */
public class SimpleEngineClientTest {
    private static final Logger log = Logger.getLogger(SimpleEngineClientTest.class);
    private String database = "/tmp/demo-control/";

    @Before public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.ALL);
        log.info("------- SETUP --------");


        log.info("------- SETUP --------");
    }

    @Test public void testLocalDBManager() throws Exception {
        log.info("------- TEST --------");
        ExaremeCluster miniCluster = ExaremeClusterFactory.createMiniCluster(1098, 8088, 0);
        miniCluster.start();
        log.info("Mini cluster started.");

        EditableExecutionPlanImpl plan = new EditableExecutionPlanImpl();
        Container[] containers = miniCluster.getContainers();
        for (Container container : containers) {
            plan.addContainer(container);
        }
        new File(database).mkdirs();

        LinkedList<Parameter> parameters = new LinkedList<>();
        parameters.add(new Parameter("time", "1"));
        parameters.add(new Parameter("memoryPercentage", "1"));
        parameters.add(new Parameter("conditionTable", "control"));
        parameters.add(new Parameter("database", database));
        Map<String, LinkedList<Parameter>> links = new HashMap<>();
        plan.addOperator(new Operator("sample1",
            "madgik.exareme.master.engine.executor.remote.operator.control.DoWhile", parameters,
            "distributed create table control as select \"False\";", String.format("c%1d", 0),
            links));
        log.info("----+ " + containers[0].name);
        try {

            ExecutionEngineProxy engineProxy = ExecutionEngineLocator.getExecutionEngineProxy();
            ExecutionEngineSession engineSession = engineProxy.createSession();
            final ExecutionEngineSessionPlan sessionPlan = engineSession.startSession();
            sessionPlan.submitPlan(plan);
            log.info("Submitted.");
            while (sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false
                && sessionPlan.getPlanSessionStatusManagerProxy().hasError() == false) {
                Thread.sleep(100);
            }
            log.info("Exited");
            if (sessionPlan.getPlanSessionStatusManagerProxy().hasError() == true) {
                log.error(sessionPlan.getPlanSessionStatusManagerProxy().getErrorList().get(0));
            }

        } catch (Exception e) {
            log.error(e);
        }
        miniCluster.stop(false);

        Thread.sleep(10 * 1000);

        miniCluster.stop(true);

        Thread.sleep(10 * 1000);

        log.info("Mini Cluster stopped.");
        log.info("------- TEST --------");
    }

    @After public void tearDown() throws Exception {
        log.info("------- CLEAN --------");
        Files.deleteIfExists(FileSystems.getDefault().getPath(database));
        log.info("------- CLEAN --------");
    }
}
