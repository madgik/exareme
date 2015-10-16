/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.reportMgr;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * @author herald
 */
public interface PlanSessionReportManagerInterface {
    void planStart(Date time, ContainerID containerID, PlanSessionReportID sessionID)
        throws RemoteException;

    void planInstantiationException(RemoteException exception, Date time, ContainerID containerID,
        PlanSessionReportID sessionID) throws RemoteException;

    void operatorSuccess(ConcreteOperatorID operatorID, int exidCode, Serializable exitMessage,
        Date time, ContainerID containerID, PlanSessionReportID sessionID) throws RemoteException;

    void operatorError(ConcreteOperatorID operatorID, RemoteException exception, Date time,
        ContainerID containerID, PlanSessionReportID sessionID) throws RemoteException;

    void quantumFinished(int quantumNum, ContainerID containerID, PlanSessionReportID sessionID)
        throws RemoteException;
}
