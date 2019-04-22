/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptorMgr;

import madgik.exareme.utils.measurement.Measurement;
import madgik.exareme.utils.measurement.MeasurementFactory;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class AdaptorManagerStatus {

    private Measurement adaptorMeasurement = MeasurementFactory.createMeasurement("Adaptor");
    private Measurement sessionMeasurement = MeasurementFactory.createMeasurement("Session");

    public AdaptorManagerStatus() {
    }

    public Measurement getAdaptorMeasurement() {
        return adaptorMeasurement;
    }

    public Measurement getSessionMeasurement() {
        return sessionMeasurement;
    }
}
