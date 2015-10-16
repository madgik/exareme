/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.session;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class PlanSessionProgressStats implements Serializable {
    private final int totalProc;
    private final int procCompleted;
    private final int totalData;
    private final int dataCompleted;
    private final int errors;

    public PlanSessionProgressStats(int totalProc, int procCompleted, int totalData,
        int dataCompleted, int errors) {
        this.totalProc = totalProc;
        this.procCompleted = procCompleted;
        this.totalData = totalData;
        this.dataCompleted = dataCompleted;
        this.errors = errors;
    }

    public int getTotalProc() {
        return totalProc;
    }

    public int processingOperatorsCompleted() {
        return procCompleted;
    }

    public int getTotalData() {
        return totalData;
    }

    public int getDataTransferCompleted() {
        return dataCompleted;
    }

    public int getErrors() {
        return errors;
    }
}
