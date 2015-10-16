/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator;

import madgik.exareme.worker.art.concreteOperator.manager.AdaptorManager;
import madgik.exareme.worker.art.container.adaptor.ReadAdaptorWrapper;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public abstract class AbstractSiNo extends AbstractOperatorImpl {

    /**
     * Get the input stream.
     *
     * @return the input stream.
     */
    protected ReadAdaptorWrapper getInput() {
        return super.getAdaptorManager().getReadStreamAdaptor(0);
    }

    @Override protected AdaptorManager createAdaptorManager() {
        return new AdaptorManager(0, 1, this.getSessionManager());
    }
}
