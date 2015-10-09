/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerProxy;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class RunQueryRemotely {

    private static Logger log = Logger.getLogger(RunQueryRemotely.class);

    public static void main(String[] args) throws Exception {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        //        System.getProperties().put("java.rmi.server.logCalls","true");

        ArtManager manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager()
            .connectToRegistry(new EntityName("ArtRegistry", args[0], Integer.parseInt(args[1])));
        manager.getExecutionEngineManager().connectToExecutionEngine();

        final ExecutionPlanParser parser = new ExecutionPlanParser();
        final ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();

        ArrayList<String> containers = new ArrayList<String>();
        for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy()
            .getContainers()) {
            log.debug(containerProxy.getEntityName().getName());
            containers.add(containerProxy.getEntityName().getName());
        }

    /* Read query */
        String planString = new String();

        BufferedReader input = new BufferedReader(new FileReader(new File(args[2])));

        String line = null;

        int containerCount = 0;
        while ((line = input.readLine()) != null) {
            if (line.startsWith("container")) {
                line = line.replace("$C", containers.get(containerCount));
                containerCount++;
            }

            planString += line + "\n";
        }

        log.debug(planString);

    /* Build execution plan */
        final ExecutionPlan plan = parser.parse(planString);

        new Thread() {

            @Override public void run() {
                try {

                    long start = System.currentTimeMillis();

          /* Create session */
                    ExecutionEngineSession session = engine.createSession();
                    ExecutionEngineSessionPlan sessionPlan = session.startSession();
                    sessionPlan.submitPlan(plan);
                    PlanSessionStatusManagerProxy managerProxy =
                        sessionPlan.getPlanSessionStatusManagerProxy();

                    if (managerProxy.hasError()) {
                        System.err.println("Finished - Errors");
                        sessionPlan.getPlanSessionStatusManagerProxy().
                            getErrorList().get(0).printStackTrace();
                        sessionPlan.close();
                        return;
                    }

                    while (sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false
                        || sessionPlan.getPlanSessionStatusManagerProxy().hasError()) {
                        Thread.sleep(100);
                    }

                    log.debug("Finished - OK");

                    sessionPlan.close();
                    session.close();

                    long end = System.currentTimeMillis();
                    log.debug("Running Time = " + (end - start));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.exit(0);
            }
        }.start();
    }
}
