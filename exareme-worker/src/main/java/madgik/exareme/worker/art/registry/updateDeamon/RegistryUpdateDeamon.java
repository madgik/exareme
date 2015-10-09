/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry.updateDeamon;

import madgik.exareme.worker.art.registry.ArtRegistry;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import madgik.exareme.worker.art.registry.Registerable;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author Dimitris Paparas<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RegistryUpdateDeamon extends Thread {
    private static final Logger log = Logger.getLogger(RegistryUpdateDeamon.class);
    private Registerable entry;
    private boolean stop;
    private ArtRegistry artRegistry;
    private long period;

    public RegistryUpdateDeamon(Registerable entry, long period) throws RemoteException {
        this.entry = entry;
        this.artRegistry = ArtRegistryLocator.getArtRegistryProxy().getRemoteObject();
        this.period = period;
        this.setName("UpdateDeamon(" + entry.getEntityName().getName() + ")");
    }

    public void startDeamon() {
        stop = false;
        this.setDaemon(true);
        this.start();
    }

    public void stopDeamon() {
        stop = true;
        this.interrupt();
    }

    @Override public void run() {
        while (!stop) {
            try {
                Thread.sleep(period);
                artRegistry.registerEntry(entry);
            } catch (InterruptedException e) {

            } catch (Exception ex) {
                log.error("Cannot register: " + entry.getEntityName().getName(), ex);
            }
        }
    }
}
