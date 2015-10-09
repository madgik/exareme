/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.SessionBased;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public interface NetManagerInterface extends SessionBased {

    NetSession getGlobalSession(PlanSessionID planSessionID) throws RemoteException;

    NetSession getContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID planSessionID) throws RemoteException;

    NetSession getOperatorSession(ConcreteOperatorID opID, ContainerSessionID containerSessionID,
        PlanSessionID planSessionID) throws RemoteException;
}
