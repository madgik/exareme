/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.local;

import madgik.exareme.worker.art.container.adaptor.AdaptorMonitor;
import madgik.exareme.worker.art.container.adaptor.StreamFactory;
import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptor;
import madgik.exareme.worker.art.container.adaptor.WriteRmiStreamAdaptorProxy;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.container.buffer.StreamBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class LocalWriteStreamAdaptor implements WriteRmiStreamAdaptor {

    private StreamBuffer buffer = null;
    private OutputStream output = null;

    public LocalWriteStreamAdaptor(StreamBuffer buffer) throws IOException {
        this.buffer = buffer;
        this.output = StreamFactory.createOutputStream(this.buffer);
    }

    public OutputStream getOutput() throws RemoteException {
        return output;
    }

    public void write(byte b) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(byte[] bytes) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(byte[] bytes, int offset, int length) throws RemoteException {
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

    public WriteRmiStreamAdaptorProxy createProxy() throws RemoteException {
        return new LocalWriteStreamAdaptorProxy(this);
    }
}
