/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.Dataflow;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.dataflow.RunningDataflow;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.PlanSessionStatistics;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.containerStatusMgr.PeriodicContainersStatusCheck;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.*;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.materialized.MaterializedBuffer;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.materialized.MaterializedObject;
import madgik.exareme.worker.art.executionEngine.recovery.RetryPolicyFactory;
import madgik.exareme.worker.art.executionEngine.recovery.retryPolicy.RetryPolicy;
import madgik.exareme.worker.art.executionEngine.resourceMgr.PlanSessionResourceManager;
import madgik.exareme.worker.art.executionEngine.session.PlanSession;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionProgressStats;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionStatus;
import madgik.exareme.worker.art.executionPlan.EditableExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanFactory;
import madgik.exareme.worker.art.executionPlan.SemanticError;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author herald
 */
public class PlanEventSchedulerState {

    private static final Logger log = Logger.getLogger(PlanEventSchedulerState.class);
    public final ArtRegistryProxy registryProxy;
    public final EventProcessor eventProcessor;
    public final PlanSessionResourceManager resourceManager;
    public final RetryPolicy retryPolicy;
    public final List<PlanTerminationListener> terminationListeners;
    private final SLA sla;
    private final RunningDataflow runningDataflow;
    public PlanEventScheduler eventScheduler = null;
    /* The following are statistics kept by clock counters */
    public long totalQuanta = 0;
    public long createDataflowTime;
    private PlanSession planSession = null;
    private PlanSessionID sessionID = null;
    private PlanSessionReportID reportID = null;
    private EditableExecutionPlan plan = null;
    private PlanSessionStatus sessionStatus = null;
    private OperatorGroupDependencySolver groupDependencySolver = null;
    // The containers can have multiple sessions
    private Map<String, ContainerProxy> containerProxyMap = null;
    // Operator id to object. This is used by the report manager
    private Map<ConcreteOperatorID, ActiveOperator> operatorIDToActiveObjectMap = null;
    private HashMap<String, HashMap<ContainerSessionID, ContainerSession>> containerToSessionIDMap =
            null;
    private HashMap<ContainerSessionID, LinkedList<ContainerSession>> sessionMap;
    /*
     * Name to active object map. Everything that can be
     * created are objects (operators, buffers, etc.)
     */
    private HashMap<String, ActiveObject> nameToActiveObjectMap = null;
    private HashMap<String, MaterializedObject> nameToMaterializedObjMap = null;
    private ArrayList<Exception> exceptions = new ArrayList<Exception>();
    private PlanSessionStatistics statistics = null;
    private long groupCount = 0;
    private long terminatedOperatorCount = 0;
    /* The following statistics are kept for the bag of tasks scheduler */
    private boolean terminated = false;
    private boolean hasError = false;
    private HashMap<ConcreteOperatorID, String> operatorExitMessage;

    // TODO(DS): this is the global state
    public PlanEventSchedulerState(PlanSessionID sessionID, PlanSessionReportID reportID, SLA sla,
                                   EventProcessor eventProcessor, DynamicPlanManager planManager,
                                   ArtRegistryProxy registryProxy, PlanSessionResourceManager resourceManager,
                                   PlanEventScheduler eventScheduler) {
        this.sessionID = sessionID;
        this.reportID = reportID;
        this.eventProcessor = eventProcessor;
        this.registryProxy = registryProxy;
        this.plan = ExecutionPlanFactory.createEditableExecutionPlan();
        this.sessionStatus = new PlanSessionStatus();
        this.planSession = new PlanSession(plan, sessionStatus, reportID);
        this.eventScheduler = eventScheduler;
        this.statistics = new PlanSessionStatistics(sessionID);
        this.resourceManager = resourceManager;
        this.retryPolicy = RetryPolicyFactory.getDefaultPolicy();
        this.containerProxyMap = Collections.synchronizedMap(new HashMap<String, ContainerProxy>());
        this.operatorIDToActiveObjectMap =
                Collections.synchronizedMap(new HashMap<ConcreteOperatorID, ActiveOperator>());
        this.terminationListeners =
                Collections.synchronizedList(new LinkedList<PlanTerminationListener>());
        this.nameToActiveObjectMap = new HashMap<String, ActiveObject>();
        this.nameToMaterializedObjMap = new HashMap<String, MaterializedObject>();
        this.containerToSessionIDMap =
                new HashMap<String, HashMap<ContainerSessionID, ContainerSession>>();
        this.sessionMap = new HashMap<ContainerSessionID, LinkedList<ContainerSession>>();
        this.groupDependencySolver = new OperatorGroupDependencySolver(this);
        // this.operatorExitMessage = new HashMap<ConcreteOperatorID, String>();
        this.sla = sla;
        this.runningDataflow = new RunningDataflow(new Dataflow(sessionID.getLongId(), sla));
    }

    public void addOperatorExitMessage(ConcreteOperatorID operatorID, int exidCode) {

        // operatorExitMessage.put(operatorID, Integer.toString(exidCode));
        //log.info("addOperatorExitMessage " + operatorID.uniqueID + " " + Integer.toString(exidCode) + " " + operatorExitMessage.size());

    }

    public OperatorGroupDependencySolver groupDependencySolver() {
        return groupDependencySolver;
    }

    public List<ContainerSession> getContSessions(ContainerSessionID contSID) {
        return sessionMap.get(contSID);
    }

    public ContainerProxy getContainerProxy(String name) {
        return containerProxyMap.get(name);
    }

    public HashMap<EntityName, ContainerSession> getEntityNameToSession() {
        return eventScheduler.entityNameToSession;
    }

    public void setEntityNameToSession(HashMap<EntityName, ContainerSession> entityNameToSession) {
        eventScheduler.entityNameToSession = entityNameToSession;
    }

    public void addContainerSession(String name, ContainerSessionID contSID,
                                    ContainerSession session) {
        HashMap<ContainerSessionID, ContainerSession> sessions = containerToSessionIDMap.get(name);
        if (sessions == null) {
            sessions = new HashMap<ContainerSessionID, ContainerSession>();
            containerToSessionIDMap.put(name, sessions);
            statistics.incrContainersUsed();
        }
        sessions.put(contSID, session);

        LinkedList<ContainerSession> sessionList = sessionMap.get(contSID);
        if (sessionList == null) {
            sessionList = new LinkedList<ContainerSession>();
            sessionMap.put(contSID, sessionList);
        }
        sessionList.add(session);
    }


    public PlanSessionID getSessionID() {
        return sessionID;
    }

    public RunningDataflow getRunningDataflow() {
        return runningDataflow;
    }

    public ContainerSession getContainerSession(String name, ContainerSessionID contSID) {
        HashMap<ContainerSessionID, ContainerSession> sessions = containerToSessionIDMap.get(name);
        if (sessions == null) {
            return null;
        }
        return sessions.get(contSID);
    }

    public void addContainerProxy(String name, ContainerProxy proxy) {
        containerProxyMap.put(name, proxy);
    }

    public List<ContainerProxy> getContainerProxies() {
        return new ArrayList<ContainerProxy>(containerProxyMap.values());
    }

    public ActiveOperator getActiveOperator(ConcreteOperatorID opId) {
        return operatorIDToActiveObjectMap.get(opId);
    }

    public void addActiveOperator(ConcreteOperatorID opId, ActiveOperator op) {
        operatorIDToActiveObjectMap.put(opId, op);
    }

    public EditableExecutionPlan getPlan() {
        return plan;
    }

    public void setPlan(EditableExecutionPlan plan) {
        this.plan = plan;
    }

    public MaterializedBuffer getMaterializedBuffer(String name) {
        return (MaterializedBuffer) nameToMaterializedObjMap.get(name);
    }

    public void addMaterializedBuffer(String name, MaterializedBuffer matBuff) {
        nameToMaterializedObjMap.put(name, matBuff);
    }

    public PlanSessionStatistics getStatistics() {
        return statistics;
    }

    public PlanSessionProgressStats getProgress() {
        return new PlanSessionProgressStats((int) statistics.totalProcessingOperators(),
                (int) statistics.processingOperatorsCompleted(),
                (int) statistics.getTotalDataTransfer(), (int) statistics.getDataTransferCompleted(),
                (int) statistics.operatorsError);
    }

    public void addException(Exception e) {
        exceptions.add(e);
    }

    public void createSessionPlan() {
        planSession = new PlanSession(plan, sessionStatus, reportID);
    }

    public PlanSession getPlanSession() {
        return planSession;
    }

    public long incrGroupCount() {
        return groupCount++;
    }

    public long incrTerminatedOperatorCount() {
        return terminatedOperatorCount++;
    }

    public long getTerminatedOperatorCount() {
        return terminatedOperatorCount;
    }

    public void registerTerminationListener(PlanTerminationListener listener) {
        terminationListeners.add(listener);
    }

    public PlanSessionID getPlanSessionID() {
        return sessionID;
    }

    public PlanSessionReportID getPlanSessionReportID() {
        return reportID;
    }

    public ActiveObject getActiveObject(String name) {
        return nameToActiveObjectMap.get(name);
    }

    public ActiveOperator getActiveOperator(String name) {
        return (ActiveOperator) nameToActiveObjectMap.get(name);
    }

    public Collection<ActiveOperator> getActiveOperators() {
        return operatorIDToActiveObjectMap.values();
    }

    public ActiveBuffer getActiveBuffer(String name) {
        return (ActiveBuffer) nameToActiveObjectMap.get(name);
    }

    public ActiveBufferPool getActiveBufferPool(String name) {
        return (ActiveBufferPool) nameToActiveObjectMap.get(name);
    }

    public ActiveContainer getActiveContainer(String name) {
        return (ActiveContainer) nameToActiveObjectMap.get(name);
    }

    public ActiveState getActiveState(String name) {
        return (ActiveState) nameToActiveObjectMap.get(name);
    }

    public void addActiveObject(String name, ActiveObject object) {
        nameToActiveObjectMap.put(name, object);
    }

    public Collection<ActiveObject> getAllActiveObjects() {
        return nameToActiveObjectMap.values();
    }

    public void setError(boolean error) {
        this.hasError = error;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    public boolean hasError() {
        return hasError;
    }

    public void addAllContainerCheck() {
        PeriodicContainersStatusCheck pcsc = new PeriodicContainersStatusCheck(eventScheduler);
        for (String containerName : plan.iterateContainers()) {
            try {
                if (!containerName.contains("any")) {
                    pcsc.addContainerToCheck(plan.getContainer(containerName));
                }
            } catch (SemanticError semanticError) {
                semanticError.printStackTrace();
            }
        }
    }
}
