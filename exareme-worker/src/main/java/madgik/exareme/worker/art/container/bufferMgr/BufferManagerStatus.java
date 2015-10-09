/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.bufferMgr;

import madgik.exareme.utils.measurement.Measurement;
import madgik.exareme.utils.measurement.MeasurementFactory;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class BufferManagerStatus {

    private Measurement pipeCountMeasurement = MeasurementFactory.createMeasurement("Buffer Count");
    private Measurement pipeSizeMeasurement = MeasurementFactory.createMeasurement("Buffer Size");
    private Measurement sessionMeasurement = MeasurementFactory.createMeasurement("Session");

    public BufferManagerStatus() {
    }

    public Measurement getPipeCountMeasurement() {
        return pipeCountMeasurement;
    }

    public Measurement getPipeSizeMeasurement() {
        return pipeSizeMeasurement;
    }

    public Measurement getSessionMeasurement() {
        return sessionMeasurement;
    }
}
