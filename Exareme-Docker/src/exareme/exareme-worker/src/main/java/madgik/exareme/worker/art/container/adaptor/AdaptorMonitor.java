/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public interface AdaptorMonitor {

    double getBytes();

    double getRecords();

    boolean getBytesPerSecond();

    boolean getRecordsPerSecond();

    boolean getAverageBytesPerSecond();

    boolean getAverageRecordsPerSecond();

    long getLifeTime();
}
