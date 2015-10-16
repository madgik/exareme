/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.worker.art.remote.RemoteObject;

import java.rmi.RemoteException;

/**
 * This is the read stream adaptor interface.
 *
 * @author Herald Kllapi <br>
 *         herald@di.uoa.gr /
 *         University of Athens
 * @since 1.0
 */
public interface ReadRmiStreamAdaptor extends Adaptor, RemoteObject<ReadRmiStreamAdaptorProxy> {

    /**
     * The following is not possible with RMI because is creates copies of the
     * arguments. That's why we use the following that returns the buffer.
     * <p/>
     * public int read(byte[] bytes, int offset, int length)
     * throws RemoteException;
     *
     * @param length The maximum number of the bytes to read.
     * @return the array with the actual bytes. NULL indicates end of stream.
     * @throws RemoteException if something goes wrong.
     */
    byte[] read(int length) throws RemoteException;

    void close() throws RemoteException;
}
