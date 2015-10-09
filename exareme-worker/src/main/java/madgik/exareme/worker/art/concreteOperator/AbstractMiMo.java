/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator;

import madgik.exareme.worker.art.concreteOperator.manager.AdaptorManager;
import madgik.exareme.worker.art.container.adaptor.ReadAdaptorWrapper;
import madgik.exareme.worker.art.container.adaptor.WriteAdaptorWrapper;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public abstract class AbstractMiMo extends AbstractOperatorImpl {

    /**
     * Get the number of input streams.
     *
     * @return The number of input streams.
     */
    public int getInputCount() {
        return super.getAdaptorManager().getInputCount();
    }

    public ReadAdaptorWrapper getInput(int in) {
        return super.getAdaptorManager().getReadStreamAdaptor(in);
    }

    @Override protected AdaptorManager createAdaptorManager() {
        return new AdaptorManager(-1, -1, this.getSessionManager());
    }

    /**
     * Get the number of output streams.
     *
     * @return The number of output streams.
     */
    public int getOutputCount() throws Exception {
        return super.getAdaptorManager().getOutputCount();
    }

    /**
     * Get an output stream.
     *
     * @param out The output stream num [0, OutputCount-1]
     * @return The stream specified.
     */
    public WriteAdaptorWrapper getOutput(int out) throws Exception {
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
}
