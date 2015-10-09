/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import java.io.Serializable;

/**
 * @author Dimitris Paparas<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RegisterPolicy implements Serializable {

    ExpirationActionPolicy expirationActionPolicy;
    ExpirationPolicy expirationPolicy;

    public RegisterPolicy(ExpirationPolicy expirationPolicy,
        ExpirationActionPolicy expirationActionPolicy) {

        this.expirationActionPolicy = expirationActionPolicy;
        this.expirationPolicy = expirationPolicy;
    }


    public ExpirationPolicy getExpirationPolicy() {
        return expirationPolicy;
    }

    public ExpirationActionPolicy getExpirationActionPolicy() {
        return expirationActionPolicy;
    }
}
