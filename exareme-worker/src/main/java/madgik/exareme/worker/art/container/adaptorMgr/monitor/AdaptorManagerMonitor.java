/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptorMgr.monitor;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.association.Triple;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptor;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptor;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorManagerInterface;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author herald
 */
public class AdaptorManagerMonitor implements AdaptorManagerInterface {

    @Override
    public AdaptorID addReadAdaptor(CombinedReadAdaptor adaptor,
                                    ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdaptorID addWriteAdaptor(CombinedWriteAdaptor adaptor,
                                     ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CombinedReadAdaptor getReadAdaptor(AdaptorID adaptorID,
                                              ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CombinedWriteAdaptor getWriteAdaptor(AdaptorID adaptorID,
                                                ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroyContainerSession(
            ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroySessions(
            PlanSessionID sessionID) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroyAllSessions()
            throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
