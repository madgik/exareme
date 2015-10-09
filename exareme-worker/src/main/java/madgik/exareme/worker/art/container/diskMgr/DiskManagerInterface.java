/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.diskMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.SessionBased;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface DiskManagerInterface extends SessionBased {

    DiskSession getGlobalSession(PlanSessionID planSessionID) throws RemoteException;

    DiskSession getContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID planSessionID) throws RemoteException;

    DiskSession getOperatorSession(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
        PlanSessionID planSessionID) throws RemoteException;
}
