/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptorMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.utils.association.Triple;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptor;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptor;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}
 * @di.uoa.gr<br> University of Athens / Department of Informatics and
 * Telecommunications.
 * @since 1.0
 */
public interface AdaptorManagerInterface {
    AdaptorID addReadAdaptor(CombinedReadAdaptor adaptor, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;

    AdaptorID addWriteAdaptor(CombinedWriteAdaptor adaptor, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;

    CombinedReadAdaptor getReadAdaptor(AdaptorID adaptorID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;

    CombinedWriteAdaptor getWriteAdaptor(AdaptorID adaptorID, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;

    Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroyContainerSession(
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException;

    Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroySessions(
        PlanSessionID sessionID) throws RemoteException;

    Triple<Long, List<CombinedReadAdaptor>, List<CombinedWriteAdaptor>> destroyAllSessions()
        throws RemoteException;
}
