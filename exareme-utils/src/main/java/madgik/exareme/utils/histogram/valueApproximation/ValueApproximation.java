/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.valueApproximation;

import madgik.exareme.utils.histogram.Bucket;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface ValueApproximation {

    double approximateValue(Bucket bucket) throws RemoteException;
}
