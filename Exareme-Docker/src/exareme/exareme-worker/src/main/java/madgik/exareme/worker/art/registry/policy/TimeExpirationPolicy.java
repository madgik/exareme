/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry.policy;

import madgik.exareme.worker.art.registry.ExpirationPolicy;

/**
 * @author Dimitris Paparas<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class TimeExpirationPolicy implements ExpirationPolicy {

    private long timeOfInitialization;
    private long timeout;
    private boolean initialized;

    public TimeExpirationPolicy(long miliseconds) {
        timeOfInitialization = 0;
        timeout = miliseconds;
        initialized = false;
    }

    public void init() {
        initialized = true;
        timeOfInitialization = System.currentTimeMillis();
    }

    public boolean hasExpired() {
        if (!initialized) {
            throw new IllegalStateException("Object not initialized.");
        }

        return (timeOfInitialization + timeout) <= System.currentTimeMillis();
    }
}
