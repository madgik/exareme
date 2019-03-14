/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.reportMgr;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}
 * @di.uoa.gr<br> University of Athens / Department of Informatics and
 * Telecommunications.
 * @since 1.0
 */
public interface PlanSessionReportManagerProxy extends ObjectProxy<PlanSessionReportManager> {
    void planStart(Date time, ContainerID containerID) throws RemoteException;

    void planInstantiationException(RemoteException exception, Date time, ContainerID containerID)
            throws RemoteException;

    void operatorSuccess(ConcreteOperatorID opID, int exidCode, Serializable exitMessage, Date time,
                         ContainerID containerID, boolean terminateGroup) throws RemoteException;

    void operatorError(ConcreteOperatorID opID, RemoteException exception, Date time,
                       ContainerID containerID) throws RemoteException;
}
