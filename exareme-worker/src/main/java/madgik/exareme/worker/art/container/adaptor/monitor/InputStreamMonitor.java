/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor.monitor;

import madgik.exareme.common.art.AdaptorStatistics;
import madgik.exareme.common.art.ConcreteOperatorStatistics;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;

/**
 * @author herald
 */
public class InputStreamMonitor extends InputStream {

    private AdaptorStatistics adaptorStats = null;
    private ConcreteOperatorStatistics opStats = null;
    private InputStream intput = null;

    public InputStreamMonitor(InputStream intput, AdaptorStatistics adaptorStats,
                              ConcreteOperatorStatistics opStats) {
        this.intput = intput;
        this.adaptorStats = adaptorStats;
        this.opStats = opStats;
    }

    @Override
    public int read() throws IOException {
        long start = System.currentTimeMillis();
        long cpuStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        int r = intput.read();
        adaptorStats.addBytes(1);

        long end = System.currentTimeMillis();
        long cpuEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        opStats.addSystemTime_ms(end - start, (cpuEnd - cpuStart) / 1000000);

        return r;
    }

    @Override
    public int read(byte[] b) throws IOException {
        long start = System.currentTimeMillis();
        long cpuStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        int l = intput.read(b);
        adaptorStats.addBytes(l);

        long end = System.currentTimeMillis();
        long cpuEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        opStats.addSystemTime_ms(end - start, (cpuEnd - cpuStart) / 1000000);

        return l;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        long start = System.currentTimeMillis();
        long cpuStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        int l = intput.read(bytes, off, len);
        adaptorStats.addBytes(l);

        long end = System.currentTimeMillis();
        long cpuEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        opStats.addSystemTime_ms(end - start, (cpuEnd - cpuStart) / 1000000);

        return l;
    }

    @Override
    public void close() throws IOException {
        long start = System.currentTimeMillis();
        long cpuStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        intput.close();

        long end = System.currentTimeMillis();
        long cpuEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        opStats.addSystemTime_ms(end - start, (cpuEnd - cpuStart) / 1000000);
    }
}
