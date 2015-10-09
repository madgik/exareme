/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator;

import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional.TransactionalBitSet;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional.TransactionalInteger;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional.TransactionalObject;

/**
 * This is an active container used for schedule estimation.
 *
 * @author heraldkllapi
 */
public class ActiveContainer implements TransactionalObject {
    private final int quantumSize_SEC;
    public TransactionalInteger lastOpEnd_SEC;
    public TransactionalBitSet timeUsed_SEC;
    public TransactionalBitSet timeUsedNoFrag_SEC;

    public ActiveContainer(RunTimeParameters params) {
        quantumSize_SEC = (int) params.quantum__SEC;
        lastOpEnd_SEC = new TransactionalInteger();
        timeUsed_SEC = new TransactionalBitSet();
        timeUsedNoFrag_SEC = new TransactionalBitSet();
    }

    public void setUse(int from_SEC, int to_SEC) {
        timeUsedNoFrag_SEC.set(from_SEC, to_SEC);

        int startQuantum = (int) (Math.floor((double) from_SEC / quantumSize_SEC));
        int endQuantum = (int) (Math.ceil((double) to_SEC / quantumSize_SEC));
        timeUsed_SEC.set(startQuantum, endQuantum);
    }

    @Override public void commit() {
        lastOpEnd_SEC.commit();
        timeUsed_SEC.commit();
        timeUsedNoFrag_SEC.commit();
    }

    @Override public void rollback() {
        lastOpEnd_SEC.rollback();
        timeUsed_SEC.rollback();
        timeUsedNoFrag_SEC.rollback();
    }
}
