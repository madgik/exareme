/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.buffer;

import madgik.exareme.common.art.entity.EntityName;

import java.net.Socket;
import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public interface SocketBuffer {

    EntityName getNetEntityName() throws RemoteException;

    Socket openServerConnection() throws RemoteException;

    void close() throws RemoteException;
}
