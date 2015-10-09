/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManager;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManagerProxy;
import madgik.exareme.worker.art.remote.RmiObjectProxy;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public class RmiClockTickManagerProxy extends RmiObjectProxy<ClockTickManager>
    implements ClockTickManagerProxy {

    public RmiClockTickManagerProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
    }

    @Override
    public void containerWarningClockTick(ContainerID id, long timeToTick_ms, long quantumCount)
        throws RemoteException {
        super.getRemoteObject().containerWarningClockTick(id, timeToTick_ms, quantumCount);
    }

    @Override public void containerClockTick(ContainerID id, long quantumCount)
        throws RemoteException {
        super.getRemoteObject().containerClockTick(id, quantumCount);
    }

    @Override public void globalWarningClockTick(long timeToTick_ms, long quantumCount)
        throws RemoteException {
        super.getRemoteObject().globalWarningClockTick(timeToTick_ms, quantumCount);
    }

    @Override public void globalClockTick(long quantumCount) throws RemoteException {
        super.getRemoteObject().globalClockTick(quantumCount);
    }
}
