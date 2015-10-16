/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.recovery;

import madgik.exareme.worker.art.executionEngine.recovery.retryPolicy.RetryPolicy;
import madgik.exareme.worker.art.executionEngine.recovery.retryPolicy.SimpleRetryPolicy;

/**
 * @author herald
 */
public class RetryPolicyFactory {

    private static RetryPolicy defaultRetryPolicy = new SimpleRetryPolicy(3);

    private RetryPolicyFactory() {
    }

    public static RetryPolicy getDefaultPolicy() {
        return defaultRetryPolicy;
    }
}
