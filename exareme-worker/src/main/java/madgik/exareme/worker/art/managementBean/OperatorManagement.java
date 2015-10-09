/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import madgik.exareme.common.art.entity.OperatorImplementationEntity;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * The implementation of the bean that manages an operator.
 *
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class OperatorManagement implements OperatorManagementMBean {

    private int invocationCount = 0;
    private int runningInstances = 0;

    /**
     * @param operator
     */
    public OperatorManagement(OperatorImplementationEntity operator) {
        try {

            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

            ObjectName opManName = null;

            try {
                opManName = new ObjectName("MPE:type=Operators, name=" + operator.getClassName());
                mbs.registerMBean(this, opManName);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the times that the operator is invoked.
     *
     * @return the times that the operator is invoked.
     */
    public Integer getInvocationCount() {
        return invocationCount;
    }

    /**
     * Get the running instances of the operator.
     *
     * @return the running instances of the operator.
     */
    public Integer getRunningInstances() {
        return runningInstances;
    }

    /**
     * Report that a new instace of the operator is running.
     */
    public synchronized void newInstanceRunning() {
        invocationCount++;
        runningInstances++;
    }

    /**
     * Report that an instace of the operator has finished.
     */
    public synchronized void instanceFinished() {
        runningInstances--;
    }
}
