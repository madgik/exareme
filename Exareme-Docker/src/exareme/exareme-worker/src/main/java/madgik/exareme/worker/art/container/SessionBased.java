package madgik.exareme.worker.art.container;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface SessionBased extends Remote {

    void destroyContainerSession(ContainerSessionID containerSessionID, PlanSessionID sessionID)
            throws RemoteException;

    void destroySessions(PlanSessionID sessionID) throws RemoteException;

    void destroyAllSessions() throws RemoteException;
}
