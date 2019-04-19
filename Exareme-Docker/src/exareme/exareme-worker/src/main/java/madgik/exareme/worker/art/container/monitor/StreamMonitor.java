/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.monitor;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.concreteOperator.AbstractOperatorImpl;

/**
 * A stream monitor is used to monitor
 * output and input streams.
 *
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.2
 */
public class StreamMonitor {

    private StatusVariable firstRec = null;
    private StatusVariable rCount = null;
    private StatusVariable rps = null;
    private StatusVariable averageRps = null;
    private StatusVariable kbytes = null;
    private StatusVariable kbps = null;
    private StatusVariable averageKbps = null;
    private OperatorTask idle = null;
    private OperatorTask working = null;
    private long firstRecReport = 0;
    private long previousRecordReport = 0;
    private long previousKbytesReport = 0;
    private int previousRCount = 0;
    private long previousKBytes = 0;
    private float reportPeriod = 0;
    private Float prev_rps = null;
    private Float prev_averageRps = null;
    private Float prev_kbps = null;
    private Float prev_averageKbps = null;

    private StreamMonitor(StatusVariable firstRec, StatusVariable rCount, StatusVariable rps,
                          StatusVariable averageRps, StatusVariable kbytes, StatusVariable kbps,
                          StatusVariable averageKbps, OperatorTask idle, OperatorTask working) {
        this.firstRec = firstRec;
        this.rCount = rCount;
        this.rps = rps;
        this.averageRps = averageRps;
        this.kbytes = kbytes;
        this.kbps = kbps;
        this.averageKbps = averageKbps;
        this.idle = idle;
        this.working = working;

        reportPeriod = AdpProperties.getArtProps().getFloat("art.container.maxStatusReportPeriod");
    }

    /**
     * Creates an output stream monitor.
     *
     * @param op     The operator to be monitored.
     * @param suffix The suffix of the status variables.
     * @return The stream monitor.
     */
    public static StreamMonitor createOutputStreamMonitor(AbstractOperatorImpl op, String suffix) {
        return createStreamMonitor(op, "/out" + suffix);
    }

    /**
     * Creates an inpout stream monitor.
     *
     * @param op     The operator to be monitored.
     * @param suffix The suffix of the status variables.
     * @return The stream monitor.
     */
    public static StreamMonitor createInputStreamMonitor(AbstractOperatorImpl op, String suffix) {
        return createStreamMonitor(op, "/in" + suffix);
    }

    private static StreamMonitor createStreamMonitor(AbstractOperatorImpl op, String suffix) {
        try {
            StatusVariable firstRec = new StatusVariable("FirstRecord" + suffix, String.class);
            op.getVariableManager().register(firstRec);

            StatusVariable rCount = new StatusVariable("RecordCount" + suffix, Integer.class);
            op.getVariableManager().register(rCount);

            StatusVariable rps = new StatusVariable("RecordPerSecond" + suffix, Float.class);
            op.getVariableManager().register(rps);

            StatusVariable averageRps =
                    new StatusVariable("AverageRecordPerSecond" + suffix, Float.class);
            op.getVariableManager().register(averageRps);

            StatusVariable kbytes = new StatusVariable("KBytes" + suffix, Long.class);
            op.getVariableManager().register(kbytes);

            StatusVariable kbps = new StatusVariable("KBytesPerSecond" + suffix, Float.class);
            op.getVariableManager().register(kbps);

            StatusVariable averageKbps =
                    new StatusVariable("AverageKBytesPerSecond" + suffix, Float.class);
            op.getVariableManager().register(averageKbps);

            return new StreamMonitor(firstRec, rCount, rps, averageRps, kbytes, kbps, averageKbps,
                    op.getTaskManager().getTask("Idle"), op.getTaskManager().getTask("Working"));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Report that the operator has send or recieved it's first record.
     *
     * @throws Exception
     */
    public void firstRecord() throws Exception {
        firstRec.setStatus("First Record");

        long currentTime = System.currentTimeMillis();

        firstRecReport = currentTime;
        previousRecordReport = currentTime;
        previousKbytesReport = currentTime;
        previousRCount = 1;

        idle.setEnd();
        working.setStart();
    }

    /**
     * Report current the record count.
     *
     * @param recordsCount The current record count.
     * @throws MonitorException
     */
    public void recordCount(int recordsCount) throws MonitorException {
        long currentTime = System.currentTimeMillis();

        if (currentTime - previousRecordReport > reportPeriod) {
            rCount.setStatus(recordsCount);

            prev_rps = new Float(
                    ((float) (recordsCount - previousRCount) / (currentTime - previousRecordReport))
                            * 1000);

            rps.setStatus(prev_rps);

            previousRCount = recordsCount;
            previousRecordReport = currentTime;

            prev_averageRps =
                    new Float(((float) recordsCount / (currentTime - firstRecReport)) * 1000);

            averageRps.setStatus(prev_averageRps);
        }
    }

    /**
     * Close the monitor.
     *
     * @param recordsCount The final record count.
     * @param kbytes       The final KBytes count.
     * @throws MonitorException
     */
    public void close(int recordsCount, long kbytes) throws MonitorException {
        long currentTime = System.currentTimeMillis();

        rCount.setStatus(recordsCount);

        prev_averageRps = new Float(((float) recordsCount / (currentTime - firstRecReport)) * 1000);

        averageRps.setStatus(prev_averageRps);

        this.kbytes.setStatus(new Long(kbytes));

        prev_averageKbps = new Float(((float) kbytes / (currentTime - firstRecReport)) * 1000);

        averageKbps.setStatus(prev_averageKbps);
    }

    /**
     * Report the current KBytes.
     *
     * @param kbytes
     * @throws MonitorException
     */
    public void kbytes(long kbytes) throws MonitorException {
        long currentTime = System.currentTimeMillis();

        if (currentTime - previousKbytesReport > reportPeriod) {
            this.kbytes.setStatus(new Long(kbytes));

            prev_kbps = new Float(
                    ((float) (kbytes - previousKBytes) / (currentTime - previousKbytesReport)) * 1000);

            kbps.setStatus(prev_kbps);

            previousKBytes = kbytes;
            previousKbytesReport = currentTime;

            prev_averageKbps = new Float(((float) kbytes / (currentTime - firstRecReport)) * 1000);

            averageKbps.setStatus(prev_averageKbps);
        }
    }

    /**
     * Get the last report time.
     *
     * @return the last report time.
     */
    public long getPreviousReport() {
        return Math.max(this.previousRecordReport, this.previousKbytesReport);
    }

    /**
     * Get the current average kbytes per second.
     *
     * @return the current average kbytes per second.
     */
    public Float getAverageKbps() {
        return prev_averageKbps;
    }

    /**
     * Get the current average record per second.
     *
     * @return the current average record per second.
     */
    public Float getAverageRps() {
        return prev_averageRps;
    }

    /**
     * Get the current kbytes per second.
     *
     * @return the current kbytes per second.
     */
    public Float getKbps() {
        return prev_kbps;
    }

    /**
     * Get the current record per second.
     *
     * @return the current record per second.
     */
    public Float getRps() {
        return prev_rps;
    }
}
