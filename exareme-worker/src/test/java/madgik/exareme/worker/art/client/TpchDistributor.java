/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.client;

import madgik.exareme.common.art.entity.EntityName;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author heraldkllapi
 */
public class TpchDistributor {
    static ArtManager manager = null;
    private static Logger log = Logger.getLogger(ArtClient.class);

    public static String createScript() throws RemoteException {
        String[] tables =
                {"customer.tbl", "lineitem.tbl", "nation.tbl", "orders.tbl", "partsupp.tbl", "part.tbl",
                        "region.tbl", "supplier.tbl",};

        ContainerProxy[] proxies = ArtRegistryLocator.getArtRegistryProxy().getContainers();
        StringBuilder script = new StringBuilder();

        // Sort the proxies based on name
        Arrays.sort(proxies, new Comparator<ContainerProxy>() {
            @Override
            public int compare(ContainerProxy o1, ContainerProxy o2) {
                return o1.getEntityName().compareTo(o2.getEntityName());
            }
        });

        for (int c = 0; c < proxies.length; ++c) {
            script.append(
                    "container c" + c + "('" + proxies[c].getEntityName().getName() + "', 1099);\n");
        }
        StringBuilder netcatScript = new StringBuilder();
        for (int part = 0; part < 32; ++part) {
            script.append("operator op" + part + " c" + part +
                    "('madgik.exareme.core.operatorLibrary.tpch.TPCHFilePartReader', " + "part='" + part
                    + "', " + "memoryPercentage='60');\n");
            for (int t = 0; t < tables.length; ++t) {
                netcatScript.append("cat " + tables[t] + ".out." + part +
                        " | nc " +
                        proxies[part].getEntityName().getIP() +
                        " 40" + t + "" + part + " & \n");
            }
        }
        System.out.println(netcatScript);
        return script.toString();
    }

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.DEBUG);
        if (args.length < 1) {
            System.out.println("Usage: \n" + "\t./bin/Art.sh <master>");
            System.exit(-1);
        }

        String artRegistry = args[0];
        long start = System.currentTimeMillis();
        // Create the manager
        manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager()
                .connectToRegistry(new EntityName("ArtRegistry", artRegistry, 1098));
        manager.getExecutionEngineManager().connectToExecutionEngine();

        // Create and Parse ART plan
        String art = createScript();
        System.out.println("Running: \n" + art);
        ExecutionPlanParser planParser = new ExecutionPlanParser();
        ExecutionPlan executionPlan = planParser.parse(art);
        log.debug("Ops   : " + executionPlan.getOperatorCount());
        // log.debug("Buff  : " + executionPlan.getBufferCount());
        // log.debug("Links : " + executionPlan.getBufferLinkCount());
        // Execute plan
        ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();
        ExecutionEngineSession session = engine.createSession();
        ExecutionEngineSessionPlan sessionPlan = session.startSession();

        CloseSession closeSessionThread = new CloseSession(session, sessionPlan);
        Runtime.getRuntime().addShutdownHook(closeSessionThread);

        sessionPlan.submitPlan(executionPlan);
        while (sessionPlan.getPlanSessionStatusManagerProxy().hasError() == false
                && sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false) {
            log.info("Completed: " + sessionPlan.getPlanSessionStatisticsManagerProxy().
                    getStatistics().operatorCompleted + " ops");
            Thread.sleep(10000);
        }
        log.info("Completed: " + sessionPlan.getPlanSessionStatisticsManagerProxy().
                getStatistics().operatorCompleted + " ops");
        for (Exception e : sessionPlan.getPlanSessionStatusManagerProxy().getErrorList()) {
            e.printStackTrace();
        }
        sessionPlan.close();
        session.close();
        long end = System.currentTimeMillis();
        DecimalFormat format = new DecimalFormat("#.##");
        log.debug("Finished! in " + format.format((end - start) / 1000.0) + " seconds");
    }
}


class CloseSession extends Thread {
    private static Logger log = Logger.getLogger(CloseSession.class);
    private ExecutionEngineSession session = null;
    private ExecutionEngineSessionPlan sessionPlan = null;
    private boolean stop = true;

    CloseSession(ExecutionEngineSession session, ExecutionEngineSessionPlan sessionPlan) {
        this.session = session;
        this.sessionPlan = sessionPlan;
    }

    public void setStopSession(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {
        if (stop == false) {
            return;
        }
        try {
            log.debug("Clossing session ...");
            sessionPlan.close();
            session.close();
        } catch (Exception e) {
            log.error("Cannot close session", e);
        }
    }
}
