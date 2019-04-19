/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.diskMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author herald
 */
public class DMSession {

    private static final Logger log = Logger.getLogger(DMSession.class);
    private HashMap<ContainerSessionID, DMContainerSession> containerSessionMap =
            new HashMap<ContainerSessionID, DMContainerSession>();
    private DiskManagerInterface diskManager = null;
    private PlanSessionID sessionID = null;
    private File rootFile = null;
    private DiskSession globalSession = null;

    public DMSession(PlanSessionID sessionID, DiskManagerInterface diskManager)
            throws RemoteException {
        this.sessionID = sessionID;
        this.diskManager = diskManager;

        String operatorRootFileName = AdpProperties.getArtProps()
                .getString("art.container.diskRoot", "art.container.operatorRoot");

        log.debug("Creating root session");
        rootFile = new File(operatorRootFileName + "/S_" + sessionID.getLongId() + "/");
        rootFile.mkdirs();

        File globalSessionFile = new File(rootFile, "global");
        globalSessionFile.mkdirs();
        globalSession = new DiskSessionSynchronized(new DiskSessionSimple(globalSessionFile));
    }

    public DiskSession getGlobalSession() {
        return globalSession;
    }

    public DiskSession getContainerSession(ContainerSessionID containerSessionID)
            throws RemoteException {
        DMContainerSession cSession = getOrCreateContainerSession(containerSessionID);
        return cSession.getGlobalSession();
    }

    public DiskSession getOperatorSession(ConcreteOperatorID opID,
                                          ContainerSessionID containerSessionID) throws RemoteException {
        DMContainerSession cSession = getOrCreateContainerSession(containerSessionID);
        return cSession.getOperatorSession(opID);
    }

    private DMContainerSession getOrCreateContainerSession(ContainerSessionID containerSessionID)
            throws RemoteException {
        DMContainerSession cSession = containerSessionMap.get(containerSessionID);
        if (cSession == null) {
            cSession = new DMContainerSession(rootFile, containerSessionID, sessionID);
            containerSessionMap.put(containerSessionID, cSession);
        }
        return cSession;
    }

    public void destroyContainerSession(ContainerSessionID cSessionID) throws RemoteException {
        DMContainerSession containerSession = containerSessionMap.remove(cSessionID);
        if (containerSession != null) {
            containerSession.destroySession();
        }
    }

    public void destroyAllSessions() throws RemoteException {
        List<ContainerSessionID> ids =
                new LinkedList<ContainerSessionID>(containerSessionMap.keySet());
        for (ContainerSessionID containerSessionID : ids) {
            destroyContainerSession(containerSessionID);
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
