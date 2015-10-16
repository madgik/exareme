/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator;

import madgik.exareme.worker.art.concreteOperator.manager.AdaptorManager;
import madgik.exareme.worker.art.container.adaptor.WriteAdaptorWrapper;

/**
 * The generic map operator is an extension of the
 * generic unary. The result stream contains the
 * stream URIs of the results.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public abstract class AbstractSiMo extends AbstractSiSo {

    /**
     * Get the number of output streams.
     *
     * @return The number of output streams.
     * @throws Exception
     */
    public int getOutputCount() throws Exception {
        return super.getAdaptorManager().getOutputCount();
    }

    /**
     * Get an output stream.
     *
     * @param out The output stream num [0, OutputCount-1]
     * @return The stream specified.
     * @throws Exception
     */
    public WriteAdaptorWrapper getOutput(int out) throws Exception {
        return super.getAdaptorManager().getWriteStreamAdaptor(out);
    }

    public WriteAdaptorWrapper getOutput(String out) throws Exception {
        return super.getAdaptorManager().getWriteStreamAdaptor(out);
    }

    /**
     * Closes an output stream.
     *
     * @param out The output stream num [0, OutputCount-1]
     * @throws java.lang.Exception
     */
    public void closeOutput(int out) throws Exception {
        super.getAdaptorManager().closeOutput(out);
    }

    @Override protected AdaptorManager createAdaptorManager() {
        return new AdaptorManager(-1, 1, this.getSessionManager());
    }
}
