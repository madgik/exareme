/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.arm.compute.session;

import madgik.exareme.worker.arm.compute.*;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class ArmComputeSession implements Serializable {

    private ArmComputeSessionID sessionID = null;
    private ArmCompute compute = null;
    private boolean isClosed = false;

    public ArmComputeSession(ArmComputeSessionID sessionID, ArmCompute compute) {
        this.sessionID = sessionID;
        this.compute = compute;
    }

    public ComputeSessionContainerManagerProxy getComputeSessionContainerManagerProxy()
        throws RemoteException {
        return compute.getComputeSessionContainerManagerProxy(sessionID);
    }

    public ComputeSessionReportManagerProxy getComputeSessionReportManagerProxy()
        throws RemoteException {
        return compute.getComputeSessionReportManagerProxy(sessionID);
    }

    public ComputeSessionStatisticsManagerProxy getComputeSessionStatisticsManagerProxy()
        throws RemoteException {
        return compute.getComputeSessionStatisticsManagerProxy(sessionID);
    }

    public ComputeSessionStatusManagerProxy getComputeSessionStatusManagerProxy()
        throws RemoteException {
        return compute.getComputeSessionStatusManagerProxy(sessionID);
    }

    public ArmComputeSessionID getSessionID() {
        return sessionID;
    }

    public void close() throws RemoteException {
        if (!isClosed) {
            compute.closeSession(sessionID);
            this.isClosed = true;
        } else {
            throw new ServerException("Close called multiple times");
        }
    }

    public boolean isClosed() throws RemoteException {
        return isClosed;
    }
}
