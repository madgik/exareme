/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.frequencyApproximation;

import madgik.exareme.utils.histogram.Bucket;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface FrequencyApproximation {

    double approximateFrequency(Bucket bucket) throws RemoteException;
}
