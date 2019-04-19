/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.buffer;

/**
 * @author heraldkllapi
 */
public class CombinedBuffer {

    public SocketBuffer socket = null;
    public StreamBuffer stream = null;

    public CombinedBuffer(SocketBuffer socket, StreamBuffer stream) {
        this.socket = socket;
        this.stream = stream;
    }
}
