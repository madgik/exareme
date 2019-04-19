/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.simulatedAnnealing;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @author herald
 */
public class AcceptancePropability implements Serializable {

    private static final long serialVersionUID = 1L;

    public double prob(double cost, double newCost, double temperature) throws RemoteException {
        if (newCost < cost) {
            return 1.0;
        }
        return Math.exp((cost - newCost) / temperature);
    }
}
