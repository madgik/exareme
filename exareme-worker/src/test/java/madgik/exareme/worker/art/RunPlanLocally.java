/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art;

import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RunPlanLocally {

    private static ArtManager manager = null;
    private static Logger log = Logger.getLogger(RunPlanLocally.class);

    private RunPlanLocally() {
    }

    public static void main(String[] args) throws Exception {

        manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager().startArtRegistry();
        manager.getContainerManager().startContainer();
        manager.getExecutionEngineManager().startExecutionEngine();

        ContainerProxy container = ArtRegistryLocator.getArtRegistryProxy().getContainers()[0];

        StringBuilder planString = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("container")) {
                line = line.replaceAll("localhost", container.getEntityName().getName());
            }

            planString.append(line + "\n");
        }

        ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();

        ExecutionPlanParser parser = new ExecutionPlanParser();
        final ExecutionPlan plan = parser.parse(planString.toString());

        long start = System.currentTimeMillis();

        //     while(true) {
        log.debug("Running...");
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

        //    ReadAdaptorWrapper in = null;
        //    int count = 0;
        //    while (in.hasNext()) {
        //      ListRecordGroup rg = (ListRecordGroup) in.readNext();
        //      for (Record rec : rg) {
        //        for (String attrName : rec.iterateAttributeNames()) {
        //          log.debug("  " + attrName + " : " + rec.getAsString(attrName));
        //        }
        //        count++;
        //      }
        //      log.debug("--");
        //    }

        //  log.debug("Count = " + count);

        sessionPlan.close();
        session.close();

        log.debug("Finished!");

        long end = System.currentTimeMillis();
        //       }

        log.debug("Running Time = " + (end - start));

        manager.stopManager();

        System.exit(0);
    }
}
