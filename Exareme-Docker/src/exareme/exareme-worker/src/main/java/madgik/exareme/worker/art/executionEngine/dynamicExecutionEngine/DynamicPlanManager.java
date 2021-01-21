/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.executionEngine.ExecutionEngine;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent.IndependentEvents;
import madgik.exareme.worker.art.executionEngine.resourceMgr.PlanSessionResourceManager;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManagerInterface;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author herald
 */
public class DynamicPlanManager implements PlanSessionManagerInterface {
    private static Logger log = Logger.getLogger(DynamicPlanManager.class);
    private final HashMap<PlanSessionID, PlanSessionReportID> elasticTreeSessions = new HashMap<>();
    private EventProcessor eventProcessor = null;
    private long sessionCount = 0;
    ReentrantLock sessionCountLock = new ReentrantLock();
    private long containerSessionCount = 0;
    /* ROOT sessions */
    private Map<PlanSessionID, PlanEventScheduler> schedulerMap = null;
    /* Parent -> Child* sessions */
    private Map<PlanSessionID, LinkedList<ContainerSessionID>> containerSessionMap = null;
    private DynamicReportManager reportManager = null;
    private PlanEventSchedulerElasticTree elasticTreeScheduler = null;
    private DynamicStatusManager statusManager = null;
    private PlanSessionResourceManager resourceManager = null;
    private DynamicClockTickManager clockTickManager = null;
    private DynamicStatisticsManager statisticsManager = null;
    private ArtRegistryProxy registryProxy = null;
    private ExecutionEngine executionEngine = null;
    private long forceSessionStopAfter_sec =
            AdpProperties.getArtProps().getLong("art.executionEngine.forceSessionStopAfter_sec");

    public DynamicPlanManager(ArtRegistryProxy registryProxy, DynamicReportManager reportManager,
                              DynamicStatusManager statusManager, PlanSessionResourceManager resourceManager,
                              DynamicClockTickManager clockTickManager, DynamicStatisticsManager statisticsManager) {
        this.eventProcessor = new EventProcessor(1);
        this.eventProcessor.start();
        this.schedulerMap = new HashMap<PlanSessionID, PlanEventScheduler>(16);
        this.containerSessionMap = new HashMap<PlanSessionID, LinkedList<ContainerSessionID>>();
        this.reportManager = reportManager;
        this.statusManager = statusManager;
        this.resourceManager = resourceManager;
        this.clockTickManager = clockTickManager;
        this.statisticsManager = statisticsManager;
        this.registryProxy = registryProxy;
    }

    public void setExecutionEngine(ExecutionEngine executionEngine) {
        this.executionEngine = executionEngine;
    }

    @Override
    public void createGlobalScheduler() throws RemoteException {
        sessionCountLock.lock();
        PlanSessionID sessionID = new PlanSessionID(sessionCount);
        PlanSessionReportID reportID = new PlanSessionReportID(sessionCount);
        sessionCount++;
        sessionCountLock.unlock();

        reportID.reportManagerProxy = executionEngine.getPlanSessionReportManagerProxy(reportID);
        PlanEventScheduler eventScheduler =
                new PlanEventScheduler(sessionID, reportID, eventProcessor, this, resourceManager,
                        registryProxy);

        schedulerMap.put(sessionID, eventScheduler);
        clockTickManager.setGlobalScheduler(eventScheduler);
    }

    @Override
    public PlanSessionID createNewSession() throws RemoteException {
        sessionCountLock.lock();
        PlanSessionID sessionID = new PlanSessionID(sessionCount);
        PlanSessionReportID reportID = new PlanSessionReportID(sessionCount);
        sessionCount++;
        sessionCountLock.unlock();

        reportID.reportManagerProxy = executionEngine.getPlanSessionReportManagerProxy(reportID);
        PlanEventScheduler eventScheduler =
                new PlanEventScheduler(sessionID, reportID, eventProcessor, this, resourceManager,
                        registryProxy);

        schedulerMap.put(sessionID, eventScheduler);
        reportManager.registerScheduler(sessionID, reportID, eventScheduler);
        statusManager.registerScheduler(sessionID, reportID, eventScheduler);
        statisticsManager.registerScheduler(sessionID, reportID, eventScheduler);
        clockTickManager.registerScheduler(sessionID, reportID, eventScheduler);
        return sessionID;
    }

    @Override
    public ContainerSessionID createContainerSession(PlanSessionID planSessionID) {
        ContainerSessionID containerSessionID = new ContainerSessionID(containerSessionCount);
        containerSessionCount++;
        LinkedList<ContainerSessionID> containerSessionIDs = containerSessionMap.get(planSessionID);
        if (containerSessionIDs == null) {
            containerSessionIDs = new LinkedList<ContainerSessionID>();
            containerSessionMap.put(planSessionID, containerSessionIDs);
        }
        containerSessionIDs.add(containerSessionID);
        return containerSessionID;
    }

    @Override
    public void destroySession(PlanSessionID sessionID) throws RemoteException {
        try {
            PlanEventScheduler eventScheduler = schedulerMap.get(sessionID);
            IndependentEvents jobs = new IndependentEvents(eventScheduler.getState());
            eventScheduler.closeSession(jobs);
            eventScheduler.queueIndependentEvents(jobs);
            Semaphore sem = new Semaphore(0);
            if (!eventScheduler.getState().isTerminated()) {
                eventScheduler.getState()
                        .registerTerminationListener(new SemaphoreTerminationListener(sem));
                log.debug(
                        "Waiting '" + forceSessionStopAfter_sec + "' seconds for session to stop ...");
                boolean stopped = sem.tryAcquire(forceSessionStopAfter_sec, TimeUnit.SECONDS);
                if (!stopped) {
                    log.error("Force stop! SessionID: " + sessionID.getLongId() + "\n" + Arrays.toString(Thread.currentThread().getStackTrace()).concat("\n"));
                }
            }

            log.debug("Destroying session with ID: " + sessionID.getLongId());
            PlanSessionReportID reportID = eventScheduler.getState().getPlanSessionReportID();
            schedulerMap.remove(sessionID);
            containerSessionMap.remove(sessionID);
            reportManager.removeScheduler(sessionID, reportID);
            statusManager.removeScheduler(sessionID, reportID);
            statisticsManager.removeScheduler(sessionID, reportID);
            clockTickManager.removeScheduler(sessionID, reportID);
        } catch (InterruptedException e) {
            throw new ServerException("Cannot destroy session ...", e);
        }
    }

    @Override
    public void execute(ExecutionPlan plan, PlanSessionID sessionID)
            throws RemoteException {
        PlanEventScheduler eventScheduler = this.schedulerMap.get(sessionID);
        log.debug("Exec: " + sessionID.getLongId());
        eventScheduler.execute(plan);
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
        log.debug("Destroy all sessions ...");
        LinkedList<PlanSessionID> session = new LinkedList<PlanSessionID>(schedulerMap.keySet());
        for (PlanSessionID id : session) {
            destroySession(id);
        }
    }

    @Override
    public void stopManager() throws RemoteException {
        stopManager(false);
    }

    @Override
    public void stopManager(boolean force) throws RemoteException {
        this.eventProcessor.stop(force);
    }

    @Override
    public PlanSessionID createNewSessionElasticTree() throws RemoteException {
        PlanSessionID sessionID = new PlanSessionID(sessionCount);
        PlanSessionReportID reportID = new PlanSessionReportID(sessionCount);
        reportID.reportManagerProxy = executionEngine.getPlanSessionReportManagerProxy(reportID);
        sessionCount++;

        elasticTreeSessions.put(sessionID, reportID);
        schedulerMap.put(sessionID, elasticTreeScheduler);
        reportManager.registerScheduler(sessionID, reportID, elasticTreeScheduler);
        statusManager.registerScheduler(sessionID, reportID, elasticTreeScheduler);
        statisticsManager.registerScheduler(sessionID, reportID, elasticTreeScheduler);
        clockTickManager.registerScheduler(sessionID, reportID, elasticTreeScheduler);
        return sessionID;
    }

    @Override
    public void executeElasticTree(ExecutionPlan plan, SLA sla, PlanSessionID sessionID)
            throws RemoteException {
        log.debug("Exec Elastic Tree: " + sessionID.getLongId());
        elasticTreeScheduler.execute(plan, sla, sessionID, elasticTreeSessions.get(sessionID));
    }

}
