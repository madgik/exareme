/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.monitor;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.concreteOperator.AbstractOperatorImpl;
import madgik.exareme.worker.art.container.operatorMgr.thread.OperatorExecutionThread;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;

/**
 * An operator monitor is assigned to
 * an operator in order to monitor general
 * information (cpu utilization)
 *
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.2
 */
public class OperatorMonitor {

    public long in_size;
    public long in_count;
    public long out_size;
    public long out_count;
    public double in_rate;
    public double out_rate;
    public long cpu_time;
    public long run_time;
    public long memory_usage;
    private ThreadMXBean tmBean = null;
    private AbstractOperatorImpl mainOperator = null;
    private OperatorExecutionThread mainExecutionThread = null;
    private ArrayList<Thread> workerThreads = null;
    private StatusVariable running = null;
    private StatusVariable cpuUsage = null;
    private StatusVariable averageCpuUsage = null;
    private long startTime = 0;
    private long previousCPUTime = 0;
    private long previousReport = 0;
    private float prev_cpuUsage = 0;
    private float prev_averageCpuUsage = 0;
    private float reportPeriod;

    /**
     * Creates a monitor for the operator specified.
     *
     * @param mainOperatorThread The operator to be monitored.
     * @throws MonitorException
     */
    public OperatorMonitor(AbstractOperatorImpl mainOperator,
        OperatorExecutionThread mainExecutionThread) throws MonitorException {
        tmBean = ManagementFactory.getThreadMXBean();

        tmBean.setThreadCpuTimeEnabled(true);

        this.mainOperator = mainOperator;
        this.mainExecutionThread = mainExecutionThread;

        this.workerThreads = new ArrayList<Thread>();

        running = new StatusVariable("Running", Boolean.class);
        mainOperator.getVariableManager().register(running);

        cpuUsage = new StatusVariable("CpuUsage", Float.class);
        mainOperator.getVariableManager().register(cpuUsage);

        averageCpuUsage = new StatusVariable("AverageCpuUsage", Float.class);
        mainOperator.getVariableManager().register(averageCpuUsage);

        running.setStatus(Boolean.TRUE);

        startTime = System.nanoTime();
        previousReport = startTime;

        reportPeriod =
            AdpProperties.getArtProps().getFloat("art.container.maxStatusReportPeriod") * 1000000;
    }

    /**
     * Report the operator status.
     *
     * @throws MonitorException
     */
    public void reportStatus() throws MonitorException {
        long current = System.nanoTime();

        if (current - previousReport > reportPeriod) {

            long cpuTime = getCPUTime();

            if (cpuTime > 0) {

                prev_cpuUsage = (float) (cpuTime - previousCPUTime) / (current - previousReport);

                cpuUsage.setStatus(prev_cpuUsage);

                prev_averageCpuUsage = (float) cpuTime / (current - startTime);

                averageCpuUsage.setStatus(prev_averageCpuUsage);

                previousCPUTime = cpuTime;
                previousReport = current;
            }
        }
    }

    /**
     * An operator can have multiple thread running.
     * Using this method, all thread of an operator
     * are monitored.
     *
     * @param childThread An operator thread.
     */
    public void addWorkerThread(Thread childThread) {
        workerThreads.add(childThread);
    }

    /**
     * Closes the monitor.
     *
     * @throws MonitorException
     */
    public void close() throws MonitorException {
        running.setStatus(Boolean.FALSE);
    }

    private long getCPUTime() {
        long cpuTime = tmBean.getThreadCpuTime(mainExecutionThread.getId());

        for (Thread t : this.workerThreads) {
            if (t.isAlive()) {
                cpuTime += tmBean.getThreadCpuTime(t.getId());
            }
        }

        return cpuTime;
    }

    private long getUserTime() {
        long userTime = tmBean.getThreadUserTime(mainExecutionThread.getId());

        for (Thread t : this.workerThreads) {
            if (t.isAlive()) {
                userTime += tmBean.getThreadUserTime(t.getId());
            }
        }

        return userTime;
    }

    private long getWaitedTime() {
        long waitedTime = tmBean.getThreadInfo(mainExecutionThread.getId()).getWaitedTime();

        for (Thread t : this.workerThreads) {
            if (t.isAlive()) {
                waitedTime += tmBean.getThreadInfo(t.getId()).getWaitedTime();
            }
        }

        return waitedTime;
    }

    private long getBlockedTime() {
        long blockedTime = tmBean.getThreadInfo(mainExecutionThread.getId()).getBlockedTime();

        for (Thread t : this.workerThreads) {
            if (t.isAlive()) {
                blockedTime += tmBean.getThreadInfo(t.getId()).getBlockedTime();
            }
        }

        return blockedTime;
    }

    /**
     * Get the current average cpu usage.
     *
     * @return the current average cpu usage.
     */
    public float getAverageCpuUsage() {
        return prev_averageCpuUsage;
    }

    /**
     * Get the current cpu usage.
     *
     * @return the current cpu usage.
     */
    public float getCpuUsage() {
        return prev_cpuUsage;
    }
}
