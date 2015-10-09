/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.bufferMgr;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.container.SessionBased;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.container.buffer.BufferQoS;
import madgik.exareme.worker.art.container.buffer.CombinedBuffer;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface BufferManagerInterface extends SessionBased {

    BufferID createBuffer(String bufferName, BufferQoS quality,
        ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException;

    void destroyBuffer(BufferID id, ContainerSessionID containerSessionID, PlanSessionID sessionID)
        throws RemoteException;

    CombinedBuffer getLocalBuffer(BufferID id, ContainerSessionID containerSessionID,
        PlanSessionID sessionID) throws RemoteException;
}
