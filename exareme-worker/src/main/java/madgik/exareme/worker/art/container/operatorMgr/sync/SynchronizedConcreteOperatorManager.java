/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.operatorMgr.sync;


import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.OperatorImplementationEntity;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptorProxy;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptorProxy;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerInterface;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerStatus;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author herald
 */
public class SynchronizedConcreteOperatorManager implements ConcreteOperatorManagerInterface {

    private final ConcreteOperatorManagerInterface manager;

    public SynchronizedConcreteOperatorManager(ConcreteOperatorManagerInterface manager) {
        this.manager = manager;
    }

    @Override
    public ConcreteOperatorID instantiate(String operatorName, String category, OperatorType type,
        OperatorImplementationEntity operator, Parameters parameters,
        Map<String, LinkedList<Parameter>> outParameters, String queryString,
        PlanSessionReportID sessionReportID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            return manager
                .instantiate(operatorName, category, type, operator, parameters, outParameters,
                    queryString, sessionReportID, containerSessionID, sessionID);
        }
    }

    @Override public void start(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            manager.start(opID, containerSessionID, sessionID);
        }
    }

    @Override
    public void destroyInstance(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            manager.destroyInstance(opID, containerSessionID, sessionID);
        }
    }

    @Override public void destroyContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            manager.destroyContainerSession(containerSessionID, sessionID);
        }
    }

    @Override public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            manager.destroySessions(sessionID);
        }
    }

    @Override public void destroyAllSessions() throws RemoteException {
        synchronized (manager) {
            manager.destroyAllSessions();
        }
    }

    @Override public void stop(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            manager.stop(opID, containerSessionID, sessionID);
        }
    }

    @Override public ConcreteOperatorStatistics getOperatorStatistics(ConcreteOperatorID opID,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            return manager.getOperatorStatistics(opID, containerSessionID, sessionID);
        }
    }

    @Override public void addReadAdaptor(CombinedReadAdaptorProxy adaptor, String adaptorName,
        String portName, Parameters parameters, boolean remote, ConcreteOperatorID iD,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            manager.addReadAdaptor(adaptor, adaptorName, portName, parameters, remote, iD,
                containerSessionID, sessionID);
        }
    }

    @Override public void addWriteAdaptor(CombinedWriteAdaptorProxy adaptor, String adaptorName,
        String portName, Parameters parameters, boolean remote, ConcreteOperatorID iD,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        synchronized (manager) {
            manager.addWriteAdaptor(adaptor, adaptorName, portName, parameters, remote, iD,
                containerSessionID, sessionID);
        }
    }

    @Override public ConcreteOperatorManagerStatus getStatus() throws RemoteException {
        synchronized (manager) {
            return manager.getStatus();
        }
    }

}
