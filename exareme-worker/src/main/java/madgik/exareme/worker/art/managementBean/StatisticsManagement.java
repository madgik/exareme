/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import madgik.exareme.worker.art.statistics.Statistics;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.2
 */
public class StatisticsManagement implements StatisticsManagementMBean {

    public Integer getOperatorsInvocationsNumber() {
        return Statistics.getOperatorInvocations();
    }

    public Integer getRunningOperators() {
        return Statistics.getRunningOperators();
    }

    public Integer getMaxConcurrentOperators() {
        return Statistics.getMaxRuunningOperators();
    }
}
