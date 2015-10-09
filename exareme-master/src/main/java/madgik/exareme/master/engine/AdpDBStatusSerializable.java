/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatistics;
import madgik.exareme.common.app.engine.AdpDBStatus;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @author herald
 */
public class AdpDBStatusSerializable implements AdpDBStatus, Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final boolean hasFinished;
    private final boolean hasError;
    private final List<Exception> exceptions;
    private final AdpDBStatistics stats;

    public AdpDBStatusSerializable(int id, boolean hasFinished, boolean hasError,
        List<Exception> exceptions, AdpDBStatistics stats) {
        this.id = id;
        this.hasFinished = hasFinished;
        this.hasError = hasError;
        this.exceptions = exceptions;
        this.stats = stats;
    }

    public int getId() {
        return id;
    }

    @Override public AdpDBQueryID getQueryID() {
        return null;
    }

    public boolean hasFinished() throws RemoteException {
        return hasFinished;
    }

    public boolean hasError() throws RemoteException {
        return hasError;
    }

    public void registerListener(AdpDBQueryListener listener) throws RemoteException {
        throw new UnsupportedOperationException("Not supported by this implementation.");
    }

    public void stopExecution() throws RemoteException {
        throw new UnsupportedOperationException("Not supported by this implementation.");
    }

    public Exception getLastException() throws RemoteException {
        if (exceptions.isEmpty()) {
            return null;
        }
        return exceptions.get(exceptions.size() - 1);
    }

    public List<Exception> getExceptions(int k) throws RemoteException {
        // TODO(herald): Make this right.
        return exceptions;
    }

    public AdpDBStatistics getStatistics() throws RemoteException {
        return stats;
    }

    public AdpDBStatus createSerializableStatus() throws RemoteException {
        return this;
    }
}
