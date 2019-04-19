/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.simulatedAnnealing;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public interface Transformation extends Serializable {

    State apply(State state) throws RemoteException;
}
