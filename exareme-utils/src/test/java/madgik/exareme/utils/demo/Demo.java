/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.sa.demo;

import madgik.exareme.utils.simulatedAnnealing.LogarithmicTemperature;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class Demo {

    private static Logger log = Logger.getLogger(Demo.class);

    public static void main(String[] args) throws RemoteException {
        DemoSA sa = new DemoSA(1000, 1000, new LogarithmicTemperature(1.0));
        DemoState best = (DemoState) sa.search();
        log.debug(best.getCost());
    }
}
