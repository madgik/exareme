/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.statistics;

import madgik.exareme.worker.art.managementBean.ManagementUtil;
import madgik.exareme.worker.art.managementBean.StatisticsManagement;
import org.apache.log4j.Logger;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class Statistics {

    private static final Logger log = Logger.getLogger(Statistics.class);
    private static int operatorsInvocationNum = 0;
    private static int runningOperators = 0;
    private static int maxOperatorsRunning = 0;

    static {
        /* Find ADP Root directory */
        try {

            StatisticsManagement envManagment = new StatisticsManagement();
            ManagementUtil.registerMBean(envManagment, "Statistics");

        } catch (Exception e) {
            log.error("Cannot register statistics", e);
        }
    }

    private Statistics() {
    }

    /**
     * Get the total number of invoked operators.
     *
     * @return the total number of invoked operators.
     */
    public static int getOperatorInvocations() {
        return operatorsInvocationNum;
    }

    /**
     * Report that a new operator is running.
     */
    public synchronized static void newOperatorRunning() {
        operatorsInvocationNum++;
        runningOperators++;

        if (maxOperatorsRunning < runningOperators) {
            maxOperatorsRunning = runningOperators;
        }
    }

    /**
     * Report that an operator finished.
     */
    public synchronized static void operatorFinished() {
        runningOperators--;
    }

    /**
     * Get the current running operators count.
     *
     * @return the current running operators count.
     */
    public static int getRunningOperators() {
        return runningOperators;
    }

    /**
     * Get the maximum running operators count.
     *
     * @return the maximum running operators count.
     */
    public static int getMaxRuunningOperators() {
        return maxOperatorsRunning;
    }
}
