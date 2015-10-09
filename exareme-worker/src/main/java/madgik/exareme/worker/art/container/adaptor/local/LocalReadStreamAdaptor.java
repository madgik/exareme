/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.local;

import madgik.exareme.worker.art.container.adaptor.AdaptorMonitor;
import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.ReadRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.container.adaptor.StreamFactory;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.container.buffer.StreamBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class LocalReadStreamAdaptor implements ReadRmiStreamAdaptor {

    private StreamBuffer buffer = null;
    private InputStream input = null;

    public LocalReadStreamAdaptor(StreamBuffer buffer) throws IOException {
        this.buffer = buffer;
        this.input = StreamFactory.createInputStream(this.buffer);
    }

    public InputStream getInput() throws RemoteException {
        return input;
    }

    public byte read() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte[] read(int length) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int read(byte[] bytes, int offset, int length) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AdaptorMonitor getMonitor() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BufferID getBufferProxy() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getRegEntryName() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ReadRmiStreamAdaptorProxy createProxy() throws RemoteException {
        return new LocalReadStreamAdaptorProxy(this);
    }
}
