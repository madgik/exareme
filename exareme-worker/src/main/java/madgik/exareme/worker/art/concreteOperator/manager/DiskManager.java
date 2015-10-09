/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator.manager;

import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;
import madgik.exareme.worker.art.container.diskMgr.DiskSession;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class DiskManager {

    private static Logger log = Logger.getLogger(DiskManager.class);
    private SessionManager sessionManager = null;
    private DiskManagerInterface diskManager = null;
    private DiskSession globalDMSession = null;
    private DiskSession containerDMSession = null;
    private DiskSession operatorDMSession = null;

    public DiskManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setDiskManager(DiskManagerInterface manager) throws RemoteException {
        this.diskManager = manager;

        // get sessions
        globalDMSession = diskManager.getGlobalSession(sessionManager.getSessionID());

        containerDMSession = diskManager.getContainerSession(sessionManager.getContainerSessionID(),
            sessionManager.getSessionID());

        operatorDMSession = diskManager
            .getOperatorSession(sessionManager.getOpID(), sessionManager.getContainerSessionID(),
                sessionManager.getSessionID());
    }

    public DiskSession getGlobalSession() {
        return globalDMSession;
    }

    public DiskSession getContainerSession() {
        return containerDMSession;
    }

    public DiskSession getOperatorSession() {
        return operatorDMSession;
    }
}
