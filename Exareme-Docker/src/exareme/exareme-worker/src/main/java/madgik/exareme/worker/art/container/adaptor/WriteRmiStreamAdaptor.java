/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface WriteRmiStreamAdaptor extends Adaptor, RemoteObject<WriteRmiStreamAdaptorProxy> {

    void write(byte[] bytes) throws RemoteException;

    void write(byte[] bytes, int offset, int length) throws RemoteException;

    void close() throws RemoteException;
}
