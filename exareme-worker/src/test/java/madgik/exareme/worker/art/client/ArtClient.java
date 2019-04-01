/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.client;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.text.DecimalFormat;

/**
 * @author herald
 */
public class ArtClient {

    static ArtManager manager = null;
    private static Logger log = Logger.getLogger(ArtClient.class);

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.DEBUG);
        if (args.length < 2) {
            System.out.println("Usage: \n" + "\t./bin/Art.sh <master> <script>");
            System.exit(-1);
        }
        String artRegistry = args[0];
        String scriptFile = args[1];
        long start = System.currentTimeMillis();
        // Create the manager
        manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager()
                .connectToRegistry(new EntityName("ArtRegistry", artRegistry, 1098));
        manager.getExecutionEngineManager().connectToExecutionEngine();
        // Parse ART plan
        String art = FileUtil.readFile(new File(scriptFile));
        ExecutionPlanParser planParser = new ExecutionPlanParser();
        ExecutionPlan executionPlan = planParser.parse(art);
        log.debug("Ops   : " + executionPlan.getOperatorCount());
        //        log.debug("Buff  : " + executionPlan.getBufferCount());
        log.debug("Links : " + executionPlan.getOperatorLinkCount());
        // Execute plan
        ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();
        ExecutionEngineSession session = engine.createSession();
        final ExecutionEngineSessionPlan sessionPlan = session.startSession();
        sessionPlan.submitPlan(executionPlan);
        while (sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false || sessionPlan
                .getPlanSessionStatusManagerProxy().hasError()) {
            Thread.sleep(100);
        }
        sessionPlan.close();
        session.close();
        long end = System.currentTimeMillis();
        DecimalFormat format = new DecimalFormat("#.##");
        log.debug("Finished! in " + format.format((end - start) / 1000.0) + " seconds");
    }
}
