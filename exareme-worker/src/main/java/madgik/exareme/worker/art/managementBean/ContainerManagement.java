/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import madgik.exareme.worker.art.container.Container;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi
 * @since 1.0
 */
public class ContainerManagement implements ContainerManagementMBean {
    private Container container = null;

    public ContainerManagement(Container container) throws RemoteException {
        this.container = container;
    }

    @Override public long getActiveConcreteOperators() throws RemoteException {
        return container.getStatus().operatorStatus.getOperatorMeasurement().getActiveValue();
    }

    @Override public long getActiveBuffers() throws RemoteException {
        return container.getStatus().bufferStatus.getPipeCountMeasurement().getActiveValue();
    }

    @Override public long getActiveAdaptors() throws RemoteException {
        return container.getStatus().adaptorStatus.getAdaptorMeasurement().getActiveValue();
    }

    @Override public long getActiveSessions() throws RemoteException {
        return 0;
    }

    @Override public long getPipePoolSize() throws RemoteException {
        return container.getStatus().bufferStatus.getPipeSizeMeasurement().getActiveValue();
    }

    @Override public long getPipePoolSessions() throws RemoteException {
        return container.getStatus().bufferStatus.getSessionMeasurement().getActiveValue();
    }
}
