/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

/**
 * The bean that manages an Operator instance.
 *
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface OperatorInstanceManagementMBean {

    /**
     * Get the CPU usage.
     *
     * @return the CPU usage.
     */
    Float getCPUUsage();

    /**
     * Get the average CPU usage.
     *
     * @return the average CPU usage.
     */
    Float getAverageCPUUsage();

    /**
     * @return true if the operator instance is running else false.
     */
    boolean getRunning();

    /**
     * Destroyes the instance management bean.
     */
    void destroy();
}
