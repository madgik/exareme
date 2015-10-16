/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.netMgr;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.container.buffer.SocketBuffer;

import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author heraldkllapi
 */
public interface NetSession {

    InputStream openInputStream(EntityName netAddress) throws RemoteException;

    InputStream openInputStream(SocketBuffer socket) throws RemoteException;

    OutputStream openOutputStream(EntityName netAddress) throws RemoteException;

    OutputStream openOutputStream(SocketBuffer socket) throws RemoteException;
}
