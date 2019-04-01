/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator;

/**
 * @author Herald Kllapi
 * @since 1.0
 */
public class AssignmentResult {

    public int cNum;
    public double operatorDuration;
    public AssigmentStats before = null;
    public AssigmentStats after = null;

    public AssignmentResult() {
        this.before = new AssigmentStats();
        this.after = new AssigmentStats();
    }

    @Override
    public String toString() {
        return after.moneyQuanta + " - " + after.time_SEC;
    }


    public class AssigmentStats {
        public int time_SEC;
        public int moneyQuanta;
        public double moneyNoFragmentation;
        public int containersUsed;
    }
}
