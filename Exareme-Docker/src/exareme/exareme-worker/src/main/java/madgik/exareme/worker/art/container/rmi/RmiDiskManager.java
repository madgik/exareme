/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.rmi;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.Container;
import madgik.exareme.worker.art.container.diskMgr.DiskManager;
import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class RmiDiskManager implements DiskManager {
    private static Logger log = Logger.getLogger(RmiDiskManager.class);
    private Container container = null;
    private DiskManagerInterface diskManagerInterface = null;
    private EntityName regEntityName = null;

    public RmiDiskManager(Container container, DiskManagerInterface diskManagerInterface,
                          EntityName regEntityName) throws RemoteException {
        this.container = container;
        this.diskManagerInterface = diskManagerInterface;
        this.regEntityName = regEntityName;
    }

    @Override
    public void destroyContainerSession(ContainerSessionID containerSessionID,
                                        PlanSessionID sessionID) throws RemoteException {
        log.debug("destroyContainerSession");
        diskManagerInterface.destroyContainerSession(containerSessionID, sessionID);
    }

    @Override
    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        log.debug("destroySessions");
        diskManagerInterface.destroySessions(sessionID);
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
        log.debug("destroyAllSessions");
        diskManagerInterface.destroyAllSessions();
    }
}
