/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry.updateDeamon;

import madgik.exareme.worker.art.registry.Registerable;

import java.rmi.RemoteException;

/**
 * @author Dimitris Paparas<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RegistryUpdateDeamonFactory {

    public static RegistryUpdateDeamon createDeamon(Registerable r, long period)
            throws RemoteException {
        return new RegistryUpdateDeamon(r, period);
    }
}
