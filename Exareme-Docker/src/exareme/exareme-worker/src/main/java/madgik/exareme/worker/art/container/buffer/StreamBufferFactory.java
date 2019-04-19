/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.buffer;

import madgik.exareme.worker.art.container.buffer.fixedSizeStreamBuffer.SimpleCyclicStreamByteBuffer;
import madgik.exareme.worker.art.container.buffer.sync.SynchronizedBuffer;
import madgik.exareme.worker.art.container.buffer.tcp.TcpSocketBuffer;

import java.io.IOException;

/**
 * @author herald
 */
public class StreamBufferFactory {

    private StreamBufferFactory() {
    }

    public static StreamBuffer createStreamBuffer(int size) throws IOException {
        return new SynchronizedBuffer(new SimpleCyclicStreamByteBuffer(size));
    }

    public static SocketBuffer createSocketBuffer(int size) throws IOException {
        return new TcpSocketBuffer();
    }
}
