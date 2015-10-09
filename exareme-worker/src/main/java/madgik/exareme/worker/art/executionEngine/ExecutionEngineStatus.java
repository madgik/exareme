/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine;

/**
 * @author Dimitris Paparas <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExecutionEngineStatus {
    private int activeExecutionPlans = 0;
    private int totalExecutedPlans = 0;
    private int successfullyExecutedPlans = 0;

    public ExecutionEngineStatus() {
        this.activeExecutionPlans = 0;
        this.totalExecutedPlans = 0;
        this.successfullyExecutedPlans = 0;
    }

    public void increaseActiveExecutionPlans() {
        ++activeExecutionPlans;
    }

    public void decreaseActiveExecutionPlans() {
        --activeExecutionPlans;
    }

    public int getActiveExecutionPlans() {
        return activeExecutionPlans;
    }

    public void increaseTotalExecutedPlans() {
        ++totalExecutedPlans;
    }

    public void decreaseTotalExecutedPlans() {
        --totalExecutedPlans;
    }

    public int getTotalExecutedPlans() {
        return totalExecutedPlans;
    }

    public void increaseSuccessfullyExecutedPlans() {
        ++successfullyExecutedPlans;
    }

    public void decreaseSuccessfullyExecutedPlans() {
        --successfullyExecutedPlans;
    }

    public int getSuccessfullyExecutedPlans() {
        return successfullyExecutedPlans;
    }
}
