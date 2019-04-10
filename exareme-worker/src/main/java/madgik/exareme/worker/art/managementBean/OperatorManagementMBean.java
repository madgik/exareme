/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

/**
 * The bean that manages an operator.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface OperatorManagementMBean {

    /**
     * Get the times that the operator is invoked.
     *
     * @return the times that the operator is invoked.
     */
    Integer getInvocationCount();

    /**
     * Get the running instances of the operator.
     *
     * @return the running instances of the operator.
     */
    Integer getRunningInstances();
}
