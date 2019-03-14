/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

/**
 * This is the StatisticsManagementMBean interface.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface StatisticsManagementMBean {

    /**
     * Get the total number of invoked operators.
     *
     * @return the total number of invoked operators.
     */
    Integer getOperatorsInvocationsNumber();

    /**
     * Get the current running operators count.
     *
     * @return the current running operators count.
     */
    Integer getRunningOperators();

    /**
     * Get the maximum running operators count.
     *
     * @return the maximum running operators count.
     */
    Integer getMaxConcurrentOperators();
}
