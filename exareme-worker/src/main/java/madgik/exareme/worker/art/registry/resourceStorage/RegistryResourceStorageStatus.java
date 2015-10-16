/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry.resourceStorage;

/**
 * @author Dimitris Paparas<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RegistryResourceStorageStatus {

    private int storedObjects;

    public RegistryResourceStorageStatus() {
        storedObjects = 0;
    }

    public void increaseStoredObjects() {
        storedObjects++;
    }

    public void decreaseStoredObjects() {
        storedObjects--;
    }

    public int getStoredObjects() {
        return storedObjects;
    }
}
