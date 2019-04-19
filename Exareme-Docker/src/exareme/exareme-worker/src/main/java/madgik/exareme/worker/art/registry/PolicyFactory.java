/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import madgik.exareme.worker.art.registry.policy.DeleteOnExpirationActionPolicy;
import madgik.exareme.worker.art.registry.policy.DoNothingOnExpirationActionPolicy;
import madgik.exareme.worker.art.registry.policy.NoExpirationPolicy;
import madgik.exareme.worker.art.registry.policy.TimeExpirationPolicy;

/**
 * @author Dimitris Paparas<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class PolicyFactory {
    public static RegisterPolicy generateNoExpirationPolicy() {
        return new RegisterPolicy(new NoExpirationPolicy(),
                new DoNothingOnExpirationActionPolicy());
    }

    public static RegisterPolicy generateTimeExpirationDeletePolicy(long timeout) {
        return new RegisterPolicy(new TimeExpirationPolicy(timeout),
                new DeleteOnExpirationActionPolicy());
    }

}
