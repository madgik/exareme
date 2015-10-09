/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerProxy;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
import madgik.exareme.worker.art.manager.ArtManager;
import madgik.exareme.worker.art.manager.ArtManagerFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Semaphore;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class ExecutionEngineDemo {

    static long start = System.currentTimeMillis();
    static long end = 0;
    static int finishThreadCount = 0;
    static int totalThreads = 1;
    private static volatile int planCount = 0;
    private static Logger log = Logger.getLogger(ExecutionEngineDemo.class);

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.ALL);
        //        if (System.getSecurityManager() == null) {
        //            System.setSecurityManager(new SecurityManager());
        //        }

        //        System.getProperties().put("java.rmi.server.logCalls","true");

        //     log.debug("Testing...");

        ArtManager manager = ArtManagerFactory.createRmiArtManager();
        manager.getRegistryManager()
            .connectToRegistry(new EntityName("ArtRegistry", "83.212.98.175", 1098));

        System.exit(0);

        //    log.debug("Connected to registry!");

        //        manager.getContainerManager().startContainer();
        manager.getExecutionEngineManager().connectToExecutionEngine();
        //    manager.getLoggerManager().connectToLogger();

        //    log.debug("Connected to execution engine!");

        //        for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
        //            log.debug(containerProxy.getEntityName().getName());
        //        }

        if (args.length == 2) {
            return;
        }

        final ExecutionPlanParser parser = new ExecutionPlanParser();

        final ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();

        FileInputStream inputStream = new FileInputStream(new File(args[2]));

        final ExecutionPlan plan = parser.parse(inputStream);

        inputStream.close();


        final Semaphore sem = new Semaphore(10);

        for (int i = 0; i < totalThreads; i++) {
            new Thread() {

                @Override public void run() {
                    try {

                        sem.acquire();

                        ExecutionEngineSession session = engine.createSession();

                        ExecutionEngineSessionPlan sessionPlan = session.startSession();
                        sessionPlan.submitPlan(plan);

                        PlanSessionStatusManagerProxy managerProxy =
                            sessionPlan.getPlanSessionStatusManagerProxy();

                        while (sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false
                            || sessionPlan.getPlanSessionStatusManagerProxy().hasError()) {
                            Thread.sleep(100);
                        }

                        if (managerProxy.hasError()) {
                            //      log.debug("Finished - Errors");
                            sessionPlan.getPlanSessionStatusManagerProxy().getErrorList().get(0)
                                .printStackTrace();
                            sessionPlan.close();

                            sem.release();
                            return;
                        }

                        sessionPlan.close();
                        session.close();

                        ++finishThreadCount;

                        sem.release();

                        if (finishThreadCount == totalThreads) {
                            end = System.currentTimeMillis();

                            //      log.debug("!!!END = " + ((float)(end - start)/1000));
                        }

                        //                        for (ConcreteOperatorTask task : managerProxy.getOperatorState(
                        //                                managerProxy.getActiveExecutionPlan().mapNameToID("hello")).taskMap.values()) {
                        //                            log.debug(task.getName() + " : " + task.getDuration());
                        //                        }
                    } catch (Exception e) {
                        sem.release();

                        e.printStackTrace();
                        //                        System.exit(0);
                    }
                }
            }.start();
        }

        //        LoggerProxy log = LoggerLocator.getLoggerProxy(ExecutionEngineDemo.class);
        //
        //        MessageReader mr = log.getOnlineMessageReader(null);
        //
        //        while(mr.hasNext()) {
        //            MessageEvent me = mr.getNext();
        //            log.debug(me.date);
        //        }
    }
}
