/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatistics;
import madgik.exareme.common.app.engine.AdpDBStatus;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author herald
 */
public class AdpDBStatusImpl implements AdpDBStatus {
    AdpDBQueryID queryID = null;
    private int id = -1;

    public AdpDBStatusImpl(int id) {
        this.id = id;
    }

    public AdpDBStatusImpl(int id, AdpDBQueryID queryID) {
        this.id = id;
        this.queryID = queryID;
    }

    public AdpDBQueryID getQueryID() {
        return queryID;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean hasFinished() throws RemoteException {
        return AdpDBStatusManagerLocator.getStatusManager().hasFinished(id);
    }

    @Override
    public boolean hasError() throws RemoteException {
        return AdpDBStatusManagerLocator.getStatusManager().hasError(id);
    }

    @Override
    public void registerListener(AdpDBQueryListener listener) throws RemoteException {
        AdpDBStatusManagerLocator.getStatusManager().registerListener(listener, id);
    }

    @Override
    public void stopExecution() throws RemoteException {
        AdpDBStatusManagerLocator.getStatusManager().stopExecution(id);
    }

    @Override
    public AdpDBStatistics getStatistics() throws RemoteException {
        return AdpDBStatusManagerLocator.getStatusManager().getStatistics(id);
    }

    @Override
    public Exception getLastException() throws RemoteException {
        return AdpDBStatusManagerLocator.getStatusManager().getLastException(id);
    }

    @Override
    public List<Exception> getExceptions(int k) throws RemoteException {
        return AdpDBStatusManagerLocator.getStatusManager().getExceptions(k, id);
    }

    @Override
    public AdpDBStatus createSerializableStatus() throws RemoteException {
        return new AdpDBStatusSerializable(id, hasFinished(), hasError(), null,
                // TODO(herald): Make this right.
                getStatistics());
    }
}
