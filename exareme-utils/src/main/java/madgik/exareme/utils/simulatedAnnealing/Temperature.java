/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.simulatedAnnealing;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface Temperature extends Serializable {

    double getTemperature(int step) throws RemoteException;
}
