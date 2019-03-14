/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.managementBean;

import madgik.exareme.common.art.entity.OperatorImplementationEntity;
import madgik.exareme.utils.managementBean.ManagementUtil;
import madgik.exareme.worker.art.container.monitor.OperatorMonitor;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * The implementation if the bean that manages an operator.
 *
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ConcreteOperatorManagement implements OperatorInstanceManagementMBean {

    private static int count = 0;
    private OperatorMonitor opMonitor = null;
    private boolean running = true;
    private ObjectName opManName = null;

    /**
     * The constructor.
     *
     * @param op        The operator to be managed.
     * @param opMonitor The operator monitor.
     */
    public ConcreteOperatorManagement(OperatorImplementationEntity op, OperatorMonitor opMonitor) {
        try {

            this.opMonitor = opMonitor;
            count++;

            ManagementUtil.registerMBean(this, "OperatorActivity");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Float getCPUUsage() {
        return opMonitor.getCpuUsage();
    }

    public Float getAverageCPUUsage() {
        return opMonitor.getAverageCpuUsage();
    }

    public boolean getRunning() {
        return running;
    }

    public void destroy() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            mbs.unregisterMBean(opManName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
