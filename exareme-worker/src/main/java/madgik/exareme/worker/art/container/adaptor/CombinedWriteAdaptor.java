/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

/**
 * @author herald
 */
public class CombinedWriteAdaptor {

    public WriteRmiStreamAdaptor writeStreamAdaptor = null;
    public WriteSocketStreamAdaptor writeStreamAdaptor2 = null;

    public CombinedWriteAdaptor(WriteRmiStreamAdaptor writeStreamAdaptor,
        WriteSocketStreamAdaptor writeStreamAdaptor2) {
        this.writeStreamAdaptor = writeStreamAdaptor;
        this.writeStreamAdaptor2 = writeStreamAdaptor2;
    }
}
