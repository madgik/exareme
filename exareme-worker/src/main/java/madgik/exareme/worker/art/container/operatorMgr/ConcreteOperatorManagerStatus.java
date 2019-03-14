/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.operatorMgr;

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
public class ConcreteOperatorManagerStatus {

    private Measurement operatorMeasurement = MeasurementFactory.createMeasurement("Operator");
    private Measurement sessionMeasurement = MeasurementFactory.createMeasurement("Session");

    public ConcreteOperatorManagerStatus() {
    }

    public Measurement getOperatorMeasurement() {
        return operatorMeasurement;
    }

    public Measurement getSessionMeasurement() {
        return sessionMeasurement;
    }
}
