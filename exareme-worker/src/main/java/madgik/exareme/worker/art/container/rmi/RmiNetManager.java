/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.rmi;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.Container;
import madgik.exareme.worker.art.container.netMgr.NetManager;
import madgik.exareme.worker.art.container.netMgr.NetManagerInterface;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class RmiNetManager implements NetManager {

    private static Logger log = Logger.getLogger(RmiNetManager.class);
    private Container container = null;
    private NetManagerInterface netMngrIface = null;
    private EntityName regEntityName = null;

    public RmiNetManager(Container container, NetManagerInterface netMngrIface,
        EntityName regEntityName) throws RemoteException {
        this.container = container;
        this.netMngrIface = netMngrIface;
        this.regEntityName = regEntityName;
    }

    @Override public void destroyContainerSession(ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException {
        log.debug("destroyContainerSession");
        //    netMngrIface.destroyContainerSession(containerSessionID, sessionID);
    }

    @Override public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        log.debug("destroySessions");
        //    netMngrIface.destroySessions(sessionID);
    }

    @Override public void destroyAllSessions() throws RemoteException {
        log.debug("destroyAllSessions");
        //    netMngrIface.destroyAllSessions();
    }
}
