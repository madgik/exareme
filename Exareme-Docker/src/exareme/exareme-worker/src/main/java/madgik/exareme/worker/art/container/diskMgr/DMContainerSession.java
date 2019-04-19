/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.diskMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.HashMap;

/**
 * @author herald
 */
public class DMContainerSession {

    private static final Logger log = Logger.getLogger(DMContainerSession.class);
    private HashMap<ConcreteOperatorID, DiskSession> sessionIDMap =
            new HashMap<ConcreteOperatorID, DiskSession>();
    private File rootFile = null;
    private DiskSession globalSession = null;
    private ContainerSessionID containerSessionID = null;
    private PlanSessionID sessionID = null;

    public DMContainerSession(File root, ContainerSessionID containerSessionID,
                              PlanSessionID sessionID) {
        this.containerSessionID = containerSessionID;
        this.sessionID = sessionID;

        log.debug("Creating root session");

        String sessionRootName = "CS_" + containerSessionID.getLongId() + "/";
        rootFile = new File(root, sessionRootName);
        rootFile.mkdirs();

        File globalSessionFile = new File(rootFile, "global");
        globalSessionFile.mkdirs();

        globalSession = new DiskSessionSynchronized(new DiskSessionSimple(globalSessionFile));
    }

    public DiskSession getGlobalSession() throws RemoteException {
        return globalSession;
    }

    public DiskSession getOperatorSession(ConcreteOperatorID opID) throws RemoteException {
        DiskSession session = sessionIDMap.get(opID);
        if (session == null) {
            File sessionFile = new File(rootFile, "OP_" + opID.uniqueID + "." + opID.operatorName);
            sessionFile.mkdirs();
            session = new DiskSessionSynchronized(new DiskSessionSimple(sessionFile));
            sessionIDMap.put(opID, session);
        }
        return session;
    }

    public void destroySession() throws RemoteException {
        for (DiskSession ds : sessionIDMap.values()) {
            ds.clean();
        }
        globalSession.clean();
        try {
            FileUtils.deleteDirectory(rootFile);
            rootFile.delete();
        } catch (Exception e) {
            throw new ServerException("Cannot delete root directory", e);
        }
    }
}
