/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

/**
 * @author herald
 */
public class CombinedReadAdaptor {

    public ReadRmiStreamAdaptor readStreamAdaptor = null;
    public ReadSocketStreamAdaptor readSocketStreamAdaptor = null;

    public CombinedReadAdaptor(ReadRmiStreamAdaptor readStreamAdaptor,
                               ReadSocketStreamAdaptor readStreamAdaptor2) {
        this.readStreamAdaptor = readStreamAdaptor;
        this.readSocketStreamAdaptor = readStreamAdaptor2;
    }
}
