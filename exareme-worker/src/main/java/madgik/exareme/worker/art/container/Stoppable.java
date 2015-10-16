/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public interface Stoppable {

    void stopManager() throws RemoteException;
}
