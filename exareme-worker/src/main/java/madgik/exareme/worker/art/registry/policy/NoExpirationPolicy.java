/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry.policy;

import madgik.exareme.worker.art.registry.ExpirationPolicy;

/**
 * @author Dimitris Paparas<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class NoExpirationPolicy implements ExpirationPolicy {

    public void init() {
    }

    public boolean hasExpired() {
        return false;
    }
}
