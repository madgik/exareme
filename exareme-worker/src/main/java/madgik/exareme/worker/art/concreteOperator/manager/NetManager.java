/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator.manager;

import madgik.exareme.worker.art.container.netMgr.NetManagerInterface;
import madgik.exareme.worker.art.container.netMgr.NetSession;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class NetManager {
    private SessionManager sessionManager = null;
    private NetManagerInterface netManager = null;
    private NetSession globalNetSession = null;
    private NetSession containerNetSession = null;
    private NetSession operatorNetSession = null;

    public NetManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setNetManager(NetManagerInterface manager) throws RemoteException {
        this.netManager = manager;

        this.globalNetSession = netManager.getGlobalSession(sessionManager.getSessionID());

        this.containerNetSession = netManager
            .getContainerSession(sessionManager.getContainerSessionID(),
                sessionManager.getSessionID());

        this.operatorNetSession = netManager
            .getOperatorSession(sessionManager.getOpID(), sessionManager.getContainerSessionID(),
                sessionManager.getSessionID());
    }

    public NetSession getGlobalSession() {
        return globalNetSession;
    }

    public NetSession getContainerSession() {
        return containerNetSession;
    }

    public NetSession getOperatorSession() {
        return operatorNetSession;
    }
}
