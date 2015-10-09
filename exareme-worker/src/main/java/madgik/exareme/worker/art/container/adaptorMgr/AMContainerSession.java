/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptorMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptor;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptor;

import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author herald
 */
public class AMContainerSession implements Serializable {

    private static final long serialVersionUID = 1L;
    private HashMap<AdaptorID, CombinedReadAdaptor> readAdaptorMap =
        new HashMap<AdaptorID, CombinedReadAdaptor>();
    private HashMap<AdaptorID, CombinedWriteAdaptor> writeAdaptorMap =
        new HashMap<AdaptorID, CombinedWriteAdaptor>();
    private ContainerSessionID containerSessionID = null;
    private PlanSessionID sessionID = null;

    public AMContainerSession(ContainerSessionID containerSessionID, PlanSessionID sessionID) {
        this.containerSessionID = containerSessionID;
        this.sessionID = sessionID;
    }

    public void addReadAdaptor(AdaptorID adaptorID, CombinedReadAdaptor adaptor) {
        readAdaptorMap.put(adaptorID, adaptor);
    }

    public void addWriteAdaptor(AdaptorID adaptorID, CombinedWriteAdaptor adaptor) {
        writeAdaptorMap.put(adaptorID, adaptor);
    }

    public CombinedReadAdaptor getReadAdaptor(AdaptorID adaptorID) throws RemoteException {
        CombinedReadAdaptor adaptor = readAdaptorMap.get(adaptorID);
        if (adaptor == null) {
            throw new AccessException("Adaptor not found!");
        }

        return adaptor;
    }

    public CombinedWriteAdaptor getWriteAdaptor(AdaptorID adaptorID) throws RemoteException {
        CombinedWriteAdaptor adaptor = writeAdaptorMap.get(adaptorID);
        if (adaptor == null) {
            throw new AccessException("Adaptor not found!");
        }

        return adaptor;
    }

    public Pair<List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroySession() {

        Pair<List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> all =
            new Pair<List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>>(
                new LinkedList<CombinedReadAdaptor>(), new LinkedList<CombinedWriteAdaptor>());

        for (CombinedReadAdaptor read : readAdaptorMap.values()) {
            all.a.add(read);
        }

        for (CombinedWriteAdaptor write : writeAdaptorMap.values()) {
            all.b.add(write);
        }

        readAdaptorMap.clear();
        writeAdaptorMap.clear();

        return all;
    }
}
