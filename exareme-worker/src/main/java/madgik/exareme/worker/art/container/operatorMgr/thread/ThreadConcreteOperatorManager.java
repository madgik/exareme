/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.operatorMgr.thread;

import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.OperatorImplementationEntity;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.concreteOperator.AbstractOperatorImpl;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptorProxy;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptorProxy;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterface;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;
import madgik.exareme.worker.art.container.event.closeSession.CloseSessionEvent;
import madgik.exareme.worker.art.container.event.closeSession.CloseSessionEventHandler;
import madgik.exareme.worker.art.container.event.closeSession.CloseSessionEventListener;
import madgik.exareme.worker.art.container.jobQueue.JobQueueInterface;
import madgik.exareme.worker.art.container.operatorGroupMgr.OperatorGroupManagerInterface;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerInterface;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerStatus;
import madgik.exareme.worker.art.container.statsMgr.StatisticsManagerInterface;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;
import org.apache.log4j.Logger;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Herald Kllapi
 * @since 1.0
 */
public class ThreadConcreteOperatorManager implements ConcreteOperatorManagerInterface {

    private static final Logger log = Logger.getLogger(ThreadConcreteOperatorManager.class);
    private ContainerID containerID = null;
    private ConcreteOperatorManagerStatus status = null;
    private StatisticsManagerInterface statistics = null;
    private DiskManagerInterface diskManagerInterface = null;
    private HashMap<PlanSessionID, TCOMPlanSession> sessionMap = null;
    private EventProcessor closeSessionProcessor = null;
    private EventProcessor operatorsProcessor = null;
    private JobQueueInterface jobQueueInterface;
    private DataTransferMgrInterface dataTransferManagerDTP = null;
    private OperatorGroupManagerInterface operatorGroupManager = null;


    public ThreadConcreteOperatorManager(ConcreteOperatorManagerStatus status,
                                         DiskManagerInterface diskManagerInterface, StatisticsManagerInterface statistics,
                                         ContainerID containerID, JobQueueInterface jobQueueInterface,
                                         DataTransferMgrInterface dataTransferManagerDTP,
                                         OperatorGroupManagerInterface operatorGroupManager) {
        this.status = status;
        this.diskManagerInterface = diskManagerInterface;
        this.statistics = statistics;
        this.containerID = containerID;
        this.sessionMap = new HashMap<>();
        this.closeSessionProcessor = new EventProcessor(1);
        this.closeSessionProcessor.start();
        this.operatorsProcessor = new EventProcessor(1);
        this.operatorsProcessor.start();
        this.jobQueueInterface = jobQueueInterface;
        this.dataTransferManagerDTP = dataTransferManagerDTP;
        this.operatorGroupManager = operatorGroupManager;

    }

    private TCOMContainerSession getContainerSession(ContainerSessionID containerSessionID,
                                                     PlanSessionID sessionID) throws RemoteException {
        TCOMPlanSession session = getCOMPlanSession(sessionID);
        if (session == null) {
            throw new NoSuchObjectException("Session not found: " + sessionID.getLongId());
        }

        TCOMContainerSession containerSession = session.getSession(containerSessionID);
        if (containerSession == null) {
            throw new NoSuchObjectException(
                    "Container session not found: " + containerSessionID.getLongId());
        }

        return containerSession;
    }

    @Override
    public ConcreteOperatorID instantiate(String name, String category, OperatorType type,
                                          OperatorImplementationEntity operator, Parameters parameters,
                                          Map<String, LinkedList<Parameter>> outParameters, String queryString,
                                          PlanSessionReportID sessionReportID, ContainerSessionID containerSessionID,
                                          PlanSessionID sessionID) throws RemoteException {
        try {
            TCOMContainerSession containerSession =
                    getContainerSession(containerSessionID, sessionID);

            ConcreteOperatorID id =
                    new ConcreteOperatorID(containerSession.getNextOperatorID(), name);

            AbstractOperatorImpl op =
                    (AbstractOperatorImpl) (containerSession.getOperatorClass(operator).newInstance());
            OperatorExecutionThread executionThread = null;

            executionThread = new OperatorExecutionThread(operator, op, jobQueueInterface, id);

            op.createSession(name, category, type, id, sessionReportID, containerSessionID,
                    sessionID, containerID, dataTransferManagerDTP, operatorGroupManager);

            op.getParameterManager().setParameters(parameters);
            op.getParameterManager().setOutputParameters(outParameters);
            op.getParameterManager().setQueryString(queryString);
            op.getDiskManager().setDiskManager(diskManagerInterface);
            op.getSessionManager()
                    .setSessionStatistics(statistics.getStatistics(containerSessionID, sessionID));

            op.initializeOperator();

            containerSession.addInstance(id, sessionReportID, operator, op, executionThread);
            status.getOperatorMeasurement().changeActiveValue(1);

            log.debug(
                    "Instantiated operator: " + operator.getClassName() + " with id " + id.uniqueID);
            return id;
        } catch (Exception e) {
            throw new ServerException("Cannot instantiate operator " + operator.getClassName(), e);
        }
    }

    @Override
    public void start(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
                      PlanSessionID sessionID) throws RemoteException {
        log.debug("Starting operator : " + opID.uniqueID);
        TCOMContainerSession containerSession = getContainerSession(containerSessionID, sessionID);
        containerSession.startInstance(opID);
        //JC container
    }

    @Override
    public void stop(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
                     PlanSessionID sessionID) throws RemoteException {
        log.debug("Stopping operator : " + opID.uniqueID);
        TCOMContainerSession containerSession = getContainerSession(containerSessionID, sessionID);
        containerSession.stopInstance(opID);
    }

    @Override
    public ConcreteOperatorManagerStatus getStatus() throws RemoteException {
        return status;
    }

    @Override
    public void addReadAdaptor(CombinedReadAdaptorProxy adaptor, String adaptorName,
                               String portName, Parameters parameters, boolean remote, ConcreteOperatorID opID,
                               ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        TCOMContainerSession containerSession = getContainerSession(containerSessionID, sessionID);
        containerSession.addReadAdaptor(opID, adaptor, adaptorName, portName, parameters, remote);
    }

    @Override
    public void addWriteAdaptor(CombinedWriteAdaptorProxy adaptor, String adaptorName,
                                String portName, Parameters parameters, boolean remote, ConcreteOperatorID opID,
                                ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        TCOMContainerSession containerSession = getContainerSession(containerSessionID, sessionID);
        containerSession.addWriteAdaptor(opID, adaptor, adaptorName, portName, parameters, remote);
    }

    @Override
    public void destroyInstance(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
                                PlanSessionID sessionID) throws RemoteException {
        log.debug("Destroying operator : " + opID.uniqueID);
        TCOMContainerSession containerSession = getContainerSession(containerSessionID, sessionID);
        containerSession.destroyInstance(opID);
    }

    @Override
    public void destroyContainerSession(ContainerSessionID cSID, PlanSessionID sessionID)
            throws RemoteException {
        TCOMPlanSession session = getCOMPlanSession(sessionID);
        log.debug("Destroy Container Session, Plan ID: " + sessionID.getLongId() + " cSID: " + cSID
                .getLongId());
        CloseSessionEvent event = new CloseSessionEvent(sessionID, cSID, session, status);
        closeSessionProcessor
                .queue(event, CloseSessionEventHandler.instance, CloseSessionEventListener.instance);
    }

    @Override
    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        TCOMPlanSession session = sessionMap.remove(sessionID);
        CloseSessionEvent event = new CloseSessionEvent(sessionID, session, status);
        closeSessionProcessor
                .queue(event, CloseSessionEventHandler.instance, CloseSessionEventListener.instance);
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
        log.debug("Destroying all sessions ...");
        ArrayList<PlanSessionID> sessions = new ArrayList<>(sessionMap.keySet());
        for (PlanSessionID sID : sessions) {
            destroySessions(sID);
        }
    }

    @Override
    public ConcreteOperatorStatistics getOperatorStatistics(ConcreteOperatorID opID,
                                                            ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        return null;
    }

    private TCOMPlanSession getCOMPlanSession(PlanSessionID sessionID) {
        TCOMPlanSession session = sessionMap.get(sessionID);
        if (session == null) {
            session = new TCOMPlanSession(sessionID, jobQueueInterface);
            sessionMap.put(sessionID, session);
        }
        return session;
    }

}
