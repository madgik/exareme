/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.clockTickManager;

import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public interface ClockTickManagerProxy extends ObjectProxy<ClockTickManager> {

    void containerWarningClockTick(ContainerID id, long timeToTick_ms, long quantumCount)
        throws RemoteException;

    void containerClockTick(ContainerID id, long quantumCount) throws RemoteException;

    void globalWarningClockTick(long timeToTick_ms, long quantumCount) throws RemoteException;

    void globalClockTick(long quantumCount) throws RemoteException;
}
