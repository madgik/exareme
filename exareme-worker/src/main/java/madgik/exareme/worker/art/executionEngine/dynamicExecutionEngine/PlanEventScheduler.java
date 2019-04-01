/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.common.optimizer.OperatorCategory;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.utils.serialization.SerializationUtil;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.ExecEngineConstants;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.addContainer.AddContainerEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.addContainer.AddContainerEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.addContainer.AddContainerEventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.containerQuantumClockTick.ContainerQuantumClockTickEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.containerQuantumClockTick.ContainerQuantumClockTickEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.containerQuantumClockTick.ContainerQuantumClockTickEventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.globalQuantumClockTick.GlobalQuantumClockTickEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.globalQuantumClockTick.GlobalQuantumClockTickEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.globalQuantumClockTick.GlobalQuantumClockTickEventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.closeContainerSession.CloseContainerSessionEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.closeContainerSession.CloseContainerSessionEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.closeContainerSession.CloseContainerSessionEventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs.ContainerJobsEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containersError.ContainersErrorEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containersError.ContainersErrorEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containersError.ContainersErrorEventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperator.CreateOperatorEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperatorConnect.CreateOperatorConnectEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.destroy.DestroyEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.destroy.DestroyEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.destroy.DestroyEventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.exception.OperatorExceptionEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.exception.OperatorExceptionEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.exception.OperatorExceptionEventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent.IndependentEvents;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent.IndependentEventsHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent.IndependentEventsListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.planTermination.PlanTerminationEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.planTermination.PlanTerminationEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.planTermination.PlanTerminationEventListener;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.start.StartEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.stop.StopEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated.OperatorGroupTerminatedEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated.OperatorTerminatedEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated.OperatorTerminatedEventListener;
import madgik.exareme.worker.art.executionEngine.resourceMgr.PlanSessionResourceManager;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionPlan.EditableExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.SemanticError;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorLinkEntity;
import madgik.exareme.worker.art.executionPlan.entity.PragmaEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.*;
import madgik.exareme.worker.art.parameter.Parameters;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author herald
 */
public class PlanEventScheduler {

    private static final Logger log = Logger.getLogger(PlanEventScheduler.class);
    public int terminatedActiveEvents = 0;
    public Lock lock = null;
    public HashMap<EntityName, ContainerSession> entityNameToSession;
    private DynamicPlanManager planManager = null;
    private PlanEventSchedulerState state = null;
    private IndependentEventsHandler independentEventsHandler = null;
    private IndependentEventsListener independentEventsListener = null;

    public PlanEventScheduler(PlanSessionID sessionID, PlanSessionReportID reportID,
                              EventProcessor eventProcessor, DynamicPlanManager planManager,
                              PlanSessionResourceManager resourceManager, ArtRegistryProxy registryProxy) {
        this.lock = new ReentrantLock();
        //    this.eventsLock = new Semaphore(1);
        this.planManager = planManager;
        this.state =
                new PlanEventSchedulerState(sessionID, reportID, null, eventProcessor, planManager,
                        registryProxy, resourceManager, this);
        createHandlers();
        createListeners();
    }

    public PlanEventScheduler(EventProcessor eventProcessor, DynamicPlanManager planManager,
                              PlanSessionResourceManager resourceManager, ArtRegistryProxy registryProxy) {
        this.planManager = planManager;
        createHandlersAndListeners();
    }

    public static void engineInternalException(ExecEngineEvent event, Exception e) {
        log.error("Internal error. Destroying plan ...", e);
        try {
            event.state.addException(e);
            event.state.setError(true);
            event.state.eventScheduler.destroyPlanWithError();
        } catch (RemoteException ex) {
            log.error("Cannot destroy plan", ex);
        }
    }

    private void createHandlersAndListeners() {
        independentEventsHandler = new IndependentEventsHandler();
        independentEventsListener = new IndependentEventsListener();
    }

    private void createHandlers() {
        independentEventsHandler = new IndependentEventsHandler();
    }

    private void createListeners() {
        independentEventsListener = new IndependentEventsListener();
    }

    public PlanEventSchedulerState getState(PlanSessionID sessionID) {
        return state;
    }

    public PlanEventSchedulerState getState() {
        return state;
    }

    public DynamicPlanManager getPlanManager() {
        return planManager;
    }

    public HashMap<EntityName, ContainerSession> getEntityNameToSession() {
        return entityNameToSession;
    }

    public void setEntityNameToSession(HashMap<EntityName, ContainerSession> entityNameToSession) {
        this.entityNameToSession = entityNameToSession;
    }

    private LinkedList<Parameter> createMediatorParams(String type, OperatorEntity op) {
        LinkedList<Parameter> params = new LinkedList<>();
        params.add(new Parameter(OperatorEntity.MEMORY_PARAM,
                String.valueOf(ExecEngineConstants.DATA_TRANSFER_MEM)));
        params.add(new Parameter(OperatorEntity.BEHAVIOR_PARAM,
                String.valueOf(OperatorBehavior.pipeline)));
        params.add(
                new Parameter(OperatorEntity.TYPE_PARAM, String.valueOf(OperatorType.dataTransfer)));
        params.add(new Parameter(OperatorEntity.CATEGORY_PARAM,
                op.category + "_" + type + "_" + OperatorCategory.dt));
        return params;
    }

    private void addMediators(OperatorEntity from, OperatorEntity to, EditableExecutionPlan plan,
                              String fromMediatorOperator, String toMediatorOperator) throws SemanticError {
        log.trace("Add icm after " + from.operatorName);
        log.trace("Create the 'from' mediator operator ... ");
        LinkedList<Parameter> fromMediatorParams = createMediatorParams("F", from);
        LinkedList<Parameter> toMediatorParams = createMediatorParams("T", to);

        OperatorEntity fromMediator = plan.addOperator(
                new Operator(to.operatorName + "_" + from.operatorName + ".ICM_FROM",
                        fromMediatorOperator, fromMediatorParams, "", from.containerName,
                        from.linksparams));

        OperatorEntity toMediator = plan.addOperator(
                new Operator(from.operatorName + "_" + to.operatorName + ".ICM_TO", toMediatorOperator,
                        toMediatorParams, "", to.containerName, from.linksparams));

        log.trace("Remove existing link between from and to ... ");
        OperatorLinkEntity operatorLink =
                plan.removeOperatorLink(from.operatorName, to.operatorName);

        log.trace("Connect 'from' with the with 'icm from' ...");
        plan.addOperatorLink(
                new OperatorLink(from.operatorName, fromMediator.operatorName, from.containerName,
                        operatorLink.paramList));

        log.trace("Connect 'icm from' with 'icm to' ... ");
        plan.addOperatorLink(new OperatorLink(fromMediator.operatorName, toMediator.operatorName,
                toMediator.containerName, operatorLink.paramList));

        log.trace("Connect 'icm from' with to ... ");
        plan.addOperatorLink(
                new OperatorLink(toMediator.operatorName, to.operatorName, to.containerName,
                        operatorLink.paramList));

    }

    private void addMediator(OperatorEntity from, List<OperatorEntity> toOps,
                             EditableExecutionPlan plan, String datatransfer) throws SemanticError {

        LinkedList<Parameter> DTParams = createMediatorParams("F", from);
        Map<String, LinkedList<Parameter>> outParams = from.linksparams;
        LinkedList<Parameter> outParameters = null;
        for (OperatorEntity to : toOps) {
            OperatorLinkEntity outLink = plan.getOperatorLink(from.operatorName, to.operatorName);
            //      log.debug("**-- " + to.container.getDataTransferPort()+"");
            outParameters = outLink.paramList;
            outParameters.add(new Parameter(OperatorEntity.TO_CONTAINER_IP_PARAM,
                    to.container.getIP().split("_")[0]));
            outParameters.add(new Parameter(OperatorEntity.FROM_CONTAINER_IP_PARAM,
                    from.container.getIP().split("_")[0]));
            outParameters.add(new Parameter(OperatorEntity.TO_CONTAINER_PORT_PARAM,
                    to.container.getDataTransferPort() + ""));
            outParameters.add(new Parameter(OperatorEntity.FROM_CONTAINER_PORT_PARAM,
                    from.container.getDataTransferPort() + ""));
            outParams.put(to.operatorName, outParameters);
        }

        OperatorEntity dataTrasferOp = plan.addOperator(
                new Operator(from.operatorName + ".DT", datatransfer, DTParams, "", from.containerName,
                        outParams));

        OperatorLinkEntity operatorLink = null;
        for (OperatorEntity to : toOps) {
            log.trace("Remove existing link between from and to ... ");
            operatorLink = plan.removeOperatorLink(from.operatorName, to.operatorName);

            log.trace("Connect 'dt' with 'to' ... ");
            plan.addOperatorLink(
                    new OperatorLink(dataTrasferOp.operatorName, to.operatorName, to.containerName,
                            operatorLink.paramList));
        }
        log.trace("Connect 'from' with the with 'dt' ...");
        plan.addOperatorLink(
                new OperatorLink(from.operatorName, dataTrasferOp.operatorName, from.containerName,
                        operatorLink.paramList));
    }

    public EditableExecutionPlan preprocessPlan(ExecutionPlan plan) throws RemoteException {
        log.debug("Preprocessing plan ...");

        addLinkParameters(plan);

        PragmaEntity mediatorFrom =
                plan.getPragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_MEDIATOR_FROM);

        PragmaEntity mediatorTo =
                plan.getPragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_MEDIATOR_TO);

        PragmaEntity dataTransfer =
                plan.getPragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_DATA_TRANSFER);

        if (mediatorFrom == null && mediatorTo == null) {
            log.debug("No preprocessing needed!");
            return (EditableExecutionPlan) plan;
        }
        EditableExecutionPlan newPlan = (EditableExecutionPlan) SerializationUtil.deepCopy(plan);
        log.debug("Adding inter container mediators ...");
        //int icmCount = 0;
    /*for (OperatorEntity to : plan.iterateOperators()) {
     for (OperatorEntity from : plan.getFromLinks(to)) {
     if (to.container.equals(from.container) == true) {
     continue;
     } else {
     addMediators(from, to, newPlan, mediatorFrom.pragmaValue, mediatorTo.pragmaValue);
     //addMediator(from, to, newPlan, dataTransfer);
     }
     }
     }*/
        int dataTransferOperatorsCount = 0;
        for (OperatorEntity from : plan.iterateOperators()) {
            List<OperatorEntity> remoteOps = new ArrayList<>();
            for (OperatorEntity to : plan.getToLinks(from)) {
                // remoteOps.add(to); //TODO(JV): SOS delete me
                if (to.container.equals(from.container) == true) {
                    continue;
                } else {
                    //            log.debug("**--" + to.container.getDataTransferPort());
                    remoteOps.add(to);//OR ME //JV91
                    //addMediators(from, to, newPlan, mediatorFrom.pragmaValue, mediatorTo.pragmaValue);
                    //TODO(JV): SOS uncomment me ^^
                }
            }
            if (!remoteOps.isEmpty()) {
                addMediator(from, remoteOps, newPlan, dataTransfer.pragmaValue);
                dataTransferOperatorsCount++;
            }
            remoteOps = null;
        }
        log.debug("Added " + dataTransferOperatorsCount + " data transfer operators.");
        newPlan.setDataTransferOperatorsCount(dataTransferOperatorsCount);
        return newPlan;
    }

    private void validatePlan(ExecutionPlan plan) throws RemoteException {
        log.debug("Validating plan ...");
        // TODO(herald): Each operator writes to only one buffer
    }

    public void execute(ExecutionPlan plan) throws RemoteException {
        lock.lock();
        try {
            log.debug("Plan submitted for execution ...");
            EditableExecutionPlan newPlan = preprocessPlan(plan);
            validatePlan(newPlan);
            log.debug("PlanAfterPreprocessing: " + newPlan.toString());
            //      state.resourceManager.reset(); TODO(jv) comment or not?
            state.setPlan(newPlan);
            state.getStatistics().setTotalProcessingOperators(plan.getOperatorCount());
            state.getStatistics().setTotalDataTransfer(newPlan.getDataTransferOperatorsCount());
            state.createSessionPlan();
            state.getStatistics().setStartTime(System.currentTimeMillis());
            state.addAllContainerCheck();
            continueExecutionEvent(state.getSessionID());
        } finally {
            lock.unlock();
        }
    }

    public boolean continueExecutionEvent(PlanSessionID planSessionID) {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            OperatorTerminatedEvent event =
                    new OperatorTerminatedEvent(null, -1, null, this, state, false);
            jobs.addEvent(event, OperatorGroupTerminatedEventHandler.instance,
                    OperatorTerminatedEventListener.instance);
            queueIndependentEvents(jobs, true);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void closeSession(IndependentEvents jobs) {
        lock.lock();
        try {
            PlanTerminationEvent event = new PlanTerminationEvent(state);
            jobs.addEvent(event, PlanTerminationEventHandler.instance,
                    PlanTerminationEventListener.instance);
        } finally {
            lock.unlock();
        }
    }

    public void planTerminated(IndependentEvents jobs) {
        lock.lock();
        try {
            PlanTerminationEvent event = new PlanTerminationEvent(state);
            jobs.addEvent(event, PlanTerminationEventHandler.instance,
                    PlanTerminationEventListener.instance);
        } finally {
            lock.unlock();
        }
    }

    public void closeContainerSession(ContainerSessionID contSID, IndependentEvents jobs) {
        lock.lock();
        try {
            CloseContainerSessionEvent event = new CloseContainerSessionEvent(contSID, state);
            jobs.addEvent(event, CloseContainerSessionEventHandler.instance,
                    CloseContainerSessionEventListener.instance);
        } finally {
            lock.unlock();
        }
    }

    public void addContainer(String containerName, EntityName container, IndependentEvents jobs)
            throws RemoteException {
        lock.lock();
        try {
            AddContainerEvent event = new AddContainerEvent(containerName, container, state);
            jobs.addEvent(event, AddContainerEventHandler.instance,
                    AddContainerEventListener.instance);
        } finally {
            lock.unlock();
        }
    }

    public void createOperator(OperatorEntity operator, ContainerJobsEvent jobs)
            throws RemoteException {
        lock.lock();
        try {
            CreateOperatorEvent event = new CreateOperatorEvent(operator, state);
            jobs.operators.add(event);
        } finally {
            lock.unlock();
        }
    }

    public void createLink(OperatorLinkEntity connect, ContainerJobsEvent jobs)
            throws RemoteException {
        lock.lock();
        try {
            CreateOperatorConnectEvent event = new CreateOperatorConnectEvent(connect, state);
            jobs.connects.add(event);
        } finally {
            lock.unlock();
        }
    }

    public void start(Start start, ContainerJobsEvent jobs) throws RemoteException {
        lock.lock();
        try {
            StartEvent event = new StartEvent(start, state);
            jobs.starts.add(event);
        } finally {
            lock.unlock();
        }
    }

    public void stop(Stop stop, ContainerJobsEvent jobs) throws RemoteException {
        lock.lock();
        try {
            StopEvent event = new StopEvent(stop, state);
            jobs.stops.add(event);
        } finally {
            lock.unlock();
        }
    }

    public void destroy(Destroy destroy, ContainerJobsEvent jobs) throws RemoteException {
        lock.lock();
        try {
            DestroyEvent event = new DestroyEvent(destroy, state);
            jobs.destroys.add(event);
        } finally {
            lock.unlock();
        }
    }

    public void addContainerJobs(ContainerJobsEvent event, IndependentEvents jobs) {
        lock.lock();
        try {
            jobs.addEvent(event, DestroyEventHandler.instance, DestroyEventListener.instance);
        } finally {
            lock.unlock();
        }
    }

    public void destroyPlanWithError(PlanSessionID sessionID) throws RemoteException {
        log.debug("Destroy Plan with error");
        lock.lock();
        try {
            state.setError(true);
            state.getPlanSession().getPlanSessionStatus().planException();
            // Terminate
            IndependentEvents termJobs = new IndependentEvents(state);
            state.eventScheduler.planTerminated(termJobs);
            queueIndependentEvents(termJobs);
            // close
            IndependentEvents closeJobs = new IndependentEvents(state);
            state.eventScheduler.closeSession(closeJobs);
            queueIndependentEvents(closeJobs);
        } finally {
            lock.unlock();
        }
    }

    public void destroyPlanWithError() throws RemoteException {
        lock.lock();

        log.debug("Destroy plan with error...");
        for (OperatorEntity coe : state.groupDependencySolver().getOperatorsWithActiveResources()) {
            state.resourceManager.getAvailableResources(coe.container).releaseMemory(coe);
        }
        state.groupDependencySolver().getOperatorsWithActiveResources().clear();
        try {
            state.setError(true);
            state.getPlanSession().getPlanSessionStatus().planException();
            // Terminate
            IndependentEvents termJobs = new IndependentEvents(state);
            state.eventScheduler.planTerminated(termJobs);
            queueIndependentEvents(termJobs);
            // close
            IndependentEvents closeJobs = new IndependentEvents(state);
            state.eventScheduler.closeSession(closeJobs);
            queueIndependentEvents(closeJobs);
        } finally {
            state.resourceManager.printUsage("After destroying Plan");
            lock.unlock();
        }

    }

    public void queueIndependentEvents(IndependentEvents event) {
        queueIndependentEvents(event, false);
    }

    public void queueIndependentEvents(IndependentEvents event, boolean terminatedEvent) {
        lock.lock();
        try {
            if (terminatedEvent) {
                terminatedActiveEvents++;
            }
            state.eventProcessor.queue(event, independentEventsHandler, independentEventsListener);
        } finally {
            lock.unlock();
        }
    }

    // START Container Events
    public void exception(ConcreteOperatorID operatorID, RemoteException exception, Date time,
                          PlanSessionID sessionID) throws RemoteException {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            OperatorExceptionEvent event =
                    new OperatorExceptionEvent(operatorID, exception, time, state);
            jobs.addEvent(event, OperatorExceptionEventHandler.instance,
                    OperatorExceptionEventListener.instance);
            queueIndependentEvents(jobs);
            terminatedActiveEvents = 0;
        } finally {
            lock.unlock();
        }
    }

    // START Container Events
    public void exception(ConcreteOperatorID operatorID, RemoteException exception, Date time)
            throws RemoteException {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            OperatorExceptionEvent event =
                    new OperatorExceptionEvent(operatorID, exception, time, state);
            jobs.addEvent(event, OperatorExceptionEventHandler.instance,
                    OperatorExceptionEventListener.instance);
            queueIndependentEvents(jobs);
            terminatedActiveEvents = 0;
        } finally {
            lock.unlock();
        }
    }

    public void terminated(ConcreteOperatorID operatorID, int exidCode, Serializable exitMessage,
                           Date time, PlanSessionID sessionID, boolean terminateGroup) throws RemoteException {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            OperatorTerminatedEvent event =
                    new OperatorTerminatedEvent(operatorID, exidCode, exitMessage, this, state,
                            terminateGroup);

            jobs.addEvent(event, OperatorGroupTerminatedEventHandler.instance,
                    OperatorTerminatedEventListener.instance);

            queueIndependentEvents(jobs, true);
            // this.reportOperator(operatorID, exidCode, exitMessage, time);
        } finally {
            lock.unlock();
        }
    }

    public void terminated(ConcreteOperatorID operatorID, int exidCode, Serializable exitMessage,
                           Date time, boolean terminateGroup) throws RemoteException {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            OperatorTerminatedEvent event =
                    new OperatorTerminatedEvent(operatorID, exidCode, exitMessage, this, state,
                            terminateGroup);

            jobs.addEvent(event, OperatorGroupTerminatedEventHandler.instance,
                    OperatorTerminatedEventListener.instance);

            queueIndependentEvents(jobs, true);
            // this.reportOperator(operatorID, exidCode, exitMessage, time);
        } finally {
            lock.unlock();
        }
    }

    public void reportOperator(ConcreteOperatorID operatorID, int exidCode,
                               Serializable exitMessage, Date time) throws RemoteException {
        lock.lock();
        try {
            // IndependentEvents jobs = new IndependentEvents();

            //ReportEvent Repevent = new ReportEvent(operatorID,
            //    exidCode,
            //     exitMessage,
            //     this);
            // jobs.addEvent(Repevent, ReportEventHandler.instance, ReportEventListener.instance);
            // queueIndependentEvents(jobs, false);
        } finally {
            lock.unlock();
        }
    }

    public void containerWarningClockTick(ContainerID id, long timeToTick_ms, long quantumCount)
            throws RemoteException {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            ContainerQuantumClockTickEvent event =
                    new ContainerQuantumClockTickEvent(id, timeToTick_ms, quantumCount, state);
            jobs.addEvent(event, ContainerQuantumClockTickEventHandler.instance,
                    ContainerQuantumClockTickEventListener.instance);
            queueIndependentEvents(jobs);
        } finally {
            lock.unlock();
        }
    }

    public void containerClockTick(ContainerID id, long quantumCount) throws RemoteException {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            ContainerQuantumClockTickEvent event =
                    new ContainerQuantumClockTickEvent(id, quantumCount, state);
            jobs.addEvent(event, ContainerQuantumClockTickEventHandler.instance,
                    ContainerQuantumClockTickEventListener.instance);
            queueIndependentEvents(jobs);
        } finally {
            lock.unlock();
        }
    }

    // END Container Events
    //
    //
    public void globalWarningClockTick(long timeToTick_ms, long quantumCount)
            throws RemoteException {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            GlobalQuantumClockTickEvent event =
                    new GlobalQuantumClockTickEvent(timeToTick_ms, quantumCount, state);
            jobs.addEvent(event, GlobalQuantumClockTickEventHandler.instance,
                    GlobalQuantumClockTickEventListener.instance);
            queueIndependentEvents(jobs);
        } finally {
            lock.unlock();
        }
    }

    public void globalClockTick(long quantumCount) throws RemoteException {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            GlobalQuantumClockTickEvent event =
                    new GlobalQuantumClockTickEvent(quantumCount, state);
            jobs.addEvent(event, GlobalQuantumClockTickEventHandler.instance,
                    GlobalQuantumClockTickEventListener.instance);
            queueIndependentEvents(jobs);
        } finally {
            lock.unlock();
        }
    }

    private void addLinkParameters(ExecutionPlan plan) {
        for (OperatorEntity op : plan.iterateOperators()) {
            HashMap<String, Parameters> params = new HashMap<>();
            for (OperatorLinkEntity link : plan.iterateOperatorLinks()) {
                Parameters linkParameters = new Parameters();
                for (Parameter p : link.paramList) {
                    linkParameters.addParameter(
                            new madgik.exareme.worker.art.parameter.Parameter(p.name, p.value));

                }
                params.put(link.toOperator.operatorName, linkParameters);
            }
            op.setLinkParams(params);
        }
    }

    public void containersError(Set<EntityName> faultyContainers) {
        lock.lock();
        try {
            IndependentEvents jobs = new IndependentEvents(state);
            ContainersErrorEvent event = new ContainersErrorEvent(state, faultyContainers);
            jobs.addEvent(event, ContainersErrorEventHandler.instance,
                    ContainersErrorEventListener.instance);
            queueIndependentEvents(jobs);
        } finally {
            lock.unlock();
        }
    }
}
