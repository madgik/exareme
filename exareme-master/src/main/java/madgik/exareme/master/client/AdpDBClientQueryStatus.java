package madgik.exareme.master.client;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;

import java.rmi.RemoteException;

/**
 * @author alex
 */
public interface AdpDBClientQueryStatus {

    AdpDBQueryID getQueryID();

    boolean hasFinished() throws RemoteException;

    String getStatus() throws RemoteException;

    String getStatusIfChanged() throws RemoteException;

    boolean hasError() throws RemoteException;

    String getError() throws RemoteException;

    String getExecutionTime() throws RemoteException;

    void close() throws RemoteException;

    void registerListener(AdpDBQueryListener listener) throws RemoteException;
}
