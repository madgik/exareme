package madgik.exareme.master.client;

import java.rmi.RemoteException;

/**
 * @author alex
 */
public interface AdpDBClientQueryListener {

    void updated() throws RemoteException;

    void terminated() throws RemoteException;
}
