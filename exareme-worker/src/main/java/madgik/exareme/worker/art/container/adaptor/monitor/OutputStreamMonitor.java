/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.monitor;

import madgik.exareme.common.art.AdaptorStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;

/**
 * @author herald
 */
public class OutputStreamMonitor extends OutputStream {

    private AdaptorStatistics adaptorStats = null;
    private ConcreteOperatorStatistics opStats = null;
    private OutputStream output = null;

    public OutputStreamMonitor(OutputStream output, AdaptorStatistics adaptorStats,
        ConcreteOperatorStatistics opStats) {
        this.output = output;
        this.adaptorStats = adaptorStats;
        this.opStats = opStats;
    }

    @Override public void write(int b) throws IOException {
        long start = System.currentTimeMillis();
        long cpuStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        output.write((byte) b);
        adaptorStats.addBytes(1);

        long end = System.currentTimeMillis();
        long cpuEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        opStats.addSystemTime_ms(end - start, (cpuEnd - cpuStart) / 1000000);
    }

    @Override public void write(byte[] b) throws IOException {
        long start = System.currentTimeMillis();
        long cpuStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        output.write(b);
        adaptorStats.addBytes(b.length);

        long end = System.currentTimeMillis();
        long cpuEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        opStats.addSystemTime_ms(end - start, (cpuEnd - cpuStart) / 1000000);
    }

    @Override public void write(byte[] bytes, int off, int len) throws IOException {
        long start = System.currentTimeMillis();
        long cpuStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        output.write(bytes, off, len);
        adaptorStats.addBytes(len);

        long end = System.currentTimeMillis();
        long cpuEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        opStats.addSystemTime_ms(end - start, (cpuEnd - cpuStart) / 1000000);
    }

    @Override public void close() throws IOException {
        long start = System.currentTimeMillis();
        long cpuStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        output.close();
        long end = System.currentTimeMillis();

        long cpuEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        opStats.addSystemTime_ms(end - start, (cpuEnd - cpuStart) / 1000000);
    }
}
