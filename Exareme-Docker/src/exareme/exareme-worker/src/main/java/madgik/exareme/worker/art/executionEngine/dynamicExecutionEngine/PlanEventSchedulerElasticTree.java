/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;


import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeConstants;
import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.GlobalTime;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.SystemConstants;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.exception.OperatorExceptionEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.exception.OperatorExceptionEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.exception.OperatorExceptionEventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent.IndependentEvents;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated.OperatorElasticTreeTerminatedEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated.OperatorElasticTreeTerminatedEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated.OperatorTerminatedEventListener;
import madgik.exareme.worker.art.executionEngine.resourceMgr.PlanSessionResourceManager;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionPlan.EditableExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class PlanEventSchedulerElasticTree extends PlanEventScheduler {
    private static final Logger log = Logger.getLogger(PlanEventSchedulerElasticTree.class);
    private final List<PlanSessionID> queuedDataflows;
    private EventProcessor eventProc = null;
    private DynamicPlanManager planManager = null;
    private PlanSessionResourceManager resourceMngr = null;
    private ArtRegistryProxy registryProxy = null;
    private PlanEventSchedulerStateElasticTree elasticState;

    public PlanEventSchedulerElasticTree(EventProcessor eventProcessor,
                                         DynamicPlanManager planManager, PlanSessionResourceManager resourceManager,
                                         ArtRegistryProxy registryProxy) {
        super(eventProcessor, planManager, resourceManager, registryProxy);


        this.eventProc = eventProcessor;
        this.planManager = planManager;
        this.resourceMngr = resourceManager;
        this.registryProxy = registryProxy;
        this.queuedDataflows = Collections.synchronizedList(new ArrayList<PlanSessionID>());
    }

    private void createElasticTreeIfNotExists() {
        // Maybe do something different below! For now is fine! :-)
        if (elasticState == null) {
            ContainerProxy[] allContainers = null;
            try {
                allContainers = ArtRegistryLocator.getArtRegistryProxy().getContainers();
            } catch (RemoteException e) {
                log.error("Cannot get containers", e);
            }
            this.elasticState = new PlanEventSchedulerStateElasticTree(allContainers);
        }
    }

    @Override
    public PlanEventSchedulerState getState(PlanSessionID sessionID) {
        try {
            lock.lock();
            createElasticTreeIfNotExists();
            return elasticState.getState(sessionID);
        } finally {
            lock.unlock();
        }
    }

    public ElasticTreeStatistics getStatistics() {
        try {
            lock.lock();
            createElasticTreeIfNotExists();
            ElasticTreeStatistics stats = elasticState.getStatistics();
            stats.queuedQueries = queuedDataflows.size();
            return stats;
        } finally {
            lock.unlock();
        }
    }

    public void execute(ExecutionPlan plan, SLA sla, PlanSessionID planSessionID,
                        PlanSessionReportID reportID) throws RemoteException {
        log.info("EXECUTE");
        try {
            lock.lock();
            createElasticTreeIfNotExists();
            log.debug("EXECTUTING: " + plan.toString());
            PlanEventSchedulerState state =
                    new PlanEventSchedulerState(planSessionID, reportID, sla, eventProc, planManager,
                            registryProxy, resourceMngr, this);
            elasticState.addState(planSessionID, state);

            EditableExecutionPlan newPlan = preprocessPlan(plan);
            state.setPlan(newPlan);
            state.getStatistics().setTotalProcessingOperators(plan.getOperatorCount());
            state.getStatistics().setTotalDataTransfer(plan.getOperatorLinkCount());
            state.createSessionPlan();
            state.getStatistics().setStartTime(System.currentTimeMillis());
            elasticState.dataflowQueued(planSessionID);

            if (elasticState.getRunningDataflows()
                    < SystemConstants.SETTINGS.MAX_CONCURENT_QUERIES) {
                continueExecutionEvent(planSessionID);
            } else {
                if (SystemConstants.SETTINGS.FAIL_QUEUED_QUERIES) {
                    destroyPlanWithError(planSessionID);
                } else {
                    // Queue the dataflow
                    queuedDataflows.add(planSessionID);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean continueExecutionEvent(PlanSessionID sessionID) {
        //    log.info("CONTINUE");
        lock.lock();
        try {
            PlanEventSchedulerState state = elasticState.getState(sessionID);
            if (state == null) {
                return false;
            }
            IndependentEvents jobs = new IndependentEvents(state);
            OperatorElasticTreeTerminatedEvent event =
                    new OperatorElasticTreeTerminatedEvent(null, -1, null, this, state, elasticState);
            jobs.addEvent(event, OperatorElasticTreeTerminatedEventHandler.instance,
                    OperatorTerminatedEventListener.instance);
            queueIndependentEvents(jobs, true);
            elasticState.dataflowStarted(sessionID);
            return true;
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void terminated(ConcreteOperatorID operatorID, int exidCode, Serializable exitMessage,
                           Date time, PlanSessionID sessionID, boolean terminateGroup) throws RemoteException {
        //    log.info("TERMINATED");
        lock.lock();
        try {
            PlanEventSchedulerState state = elasticState.getState(sessionID);
            IndependentEvents jobs = new IndependentEvents(state);
            OperatorElasticTreeTerminatedEvent event =
                    new OperatorElasticTreeTerminatedEvent(operatorID, exidCode, exitMessage, this,
                            state, elasticState);

            jobs.addEvent(event, OperatorElasticTreeTerminatedEventHandler.instance,
                    OperatorTerminatedEventListener.instance);

            queueIndependentEvents(jobs, true);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void exception(ConcreteOperatorID operatorID, RemoteException exception, Date time,
                          PlanSessionID sessionID) throws RemoteException {
        //    log.info("EXCEPTION");
        lock.lock();
        try {
            PlanEventSchedulerState state = elasticState.getState(sessionID);
            IndependentEvents jobs = new IndependentEvents(state);
            OperatorExceptionEvent event =
                    new OperatorExceptionEvent(operatorID, exception, time, state);
            jobs.addEvent(event, OperatorExceptionEventHandler.instance,
                    OperatorExceptionEventListener.instance);
            queueIndependentEvents(jobs, false);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void destroyPlanWithError(PlanSessionID sessionID) throws RemoteException {
        lock.lock();
        try {
            PlanEventSchedulerState state = elasticState.getState(sessionID);
            state.setError(true);
            state.getPlanSession().getPlanSessionStatus().planException();
            // Terminate
            IndependentEvents termJobs = new IndependentEvents(state);
            state.eventScheduler.planTerminated(termJobs);
            queueIndependentEvents(termJobs, false);
            // close
            IndependentEvents closeJobs = new IndependentEvents(state);
            state.eventScheduler.closeSession(closeJobs);
            queueIndependentEvents(closeJobs, false);
        } finally {
            lock.unlock();
        }
    }

    public void destroySession(PlanSessionID sessionID) {
        try {
            lock.lock();
            PlanEventSchedulerState state = elasticState.getState(sessionID);
            elasticState.dataflowFinished(sessionID, state.hasError());
            elasticState.removeState(sessionID);

            // Start executing queued dataflows
            if (queuedDataflows.isEmpty() == false) {
                PlanSessionID queuedSession = queuedDataflows.remove(0);
                continueExecutionEvent(queuedSession);
            }

            // Enable allocator after a certain period of time to be able to get a good sample
            if (GlobalTime.getCurrentSec() < TreeConstants.SETTINGS.ENABLE_SUPPLIER_AFTER_TIME) {
                return;
            }

            try {
                elasticState.reorganizeResources();
            } catch (RemoteException e) {
                log.error("Cannot reorganize resources!", e);
            }
        } finally {
            lock.unlock();
        }
    }
}
