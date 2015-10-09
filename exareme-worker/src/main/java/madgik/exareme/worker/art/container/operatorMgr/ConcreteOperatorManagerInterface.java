/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.operatorMgr;

import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.OperatorImplementationEntity;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.SessionBased;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptorProxy;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptorProxy;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ConcreteOperatorManagerInterface extends SessionBased {

    ConcreteOperatorID instantiate(String name, String category, OperatorType type,
        OperatorImplementationEntity operator, Parameters parameters,
        Map<String, LinkedList<Parameter>> outParameters, String queryString,
        PlanSessionReportID sessionReportID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;


    void start(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;

    void destroyInstance(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;

    void stop(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;

    ConcreteOperatorStatistics getOperatorStatistics(ConcreteOperatorID opID,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException;

    void addReadAdaptor(CombinedReadAdaptorProxy adaptor, String adaptorName, String portName,
        Parameters parameters, boolean remote, ConcreteOperatorID iD,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException;

    void addWriteAdaptor(CombinedWriteAdaptorProxy adaptor, String adaptorName, String portName,
        Parameters parameters, boolean remote, ConcreteOperatorID iD,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException;

    ConcreteOperatorManagerStatus getStatus() throws RemoteException;

}
