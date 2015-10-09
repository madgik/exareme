/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor;

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
public class ArtificialWorkloadDemo {

    private static ArtManager manager = null;
    private static Logger log = Logger.getLogger(ArtificialWorkloadDemo.class);

    public static void main(String[] args) throws Exception {

        manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager().startArtRegistry();
        manager.getContainerManager().startContainer();
        manager.getExecutionEngineManager().startExecutionEngine();

        ContainerProxy container = ArtRegistryLocator.getArtRegistryProxy().getContainers()[0];

        String planString = new String();

        BufferedReader input = new BufferedReader(new FileReader(new File("./planART/art.ep")));

        String line = null;

        while ((line = input.readLine()) != null) {
            if (line.startsWith("container")) {
                line = line.replace("%C", container.getEntityName().getName());
            }

            planString += line + "\n";
        }

        //        planString +=
        //                "container c ('" + container.getEntityName().getName() + "',1099);\n" +
        //                "instantiate producer c('madgik.exareme.db.operatorLibrary.artificialWorkload.AWnimoSF', CPU='10000', maxCPU='0.5', datain='1', dataout='10');\n" +
        //                "instantiate consumer c('madgik.exareme.db.operatorLibrary.artificialWorkload.AWmimoSF', CPU='10000', maxCPU='0.5', datain='10', dataout='1');\n" +
        //
        //                "create p_out c('10');\n" +
        //                "create c_out c('10');\n" +
        //
        //                "connect c(producer, p_out);" +
        //                "connect c(p_out, consumer);" +
        //                "connect c(consumer, c_out);";

        log.debug(planString);

        //        if(true) {
        //            System.exit(0);
        //        }

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

        long end = System.currentTimeMillis();
        log.debug("Running Time = " + (end - start));

        sessionPlan.close();

        manager.stopManager();
        log.debug("Finished!");

        System.exit(0);
    }
}
