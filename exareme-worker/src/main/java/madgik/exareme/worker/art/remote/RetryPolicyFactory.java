/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.units.Metrics;
import madgik.exareme.worker.art.remote.retryPolicy.ConstantTimeIntervalPolicy;
import madgik.exareme.worker.art.remote.retryPolicy.RandomTimeIntervalPolicy;
import madgik.exareme.worker.art.remote.retryPolicy.RetryNTimesPolicy;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RetryPolicyFactory {

    private static RetryPolicy defaultRetryPolicy = null;
    private static RetryPolicy socketRetryPolicy = null;

    static {
        int retryTimes = AdpProperties.getArtProps().getInt("art.rmi.retryTimes");
        int retryPeriod_ms = (int) (AdpProperties.getArtProps().getFloat("art.rmi.retryPeriod_sec")
                * (float) Metrics.MiliSec);

        defaultRetryPolicy = new RetryPolicy(new RetryNTimesPolicy(retryTimes),
                new ConstantTimeIntervalPolicy(retryPeriod_ms));

        socketRetryPolicy = new RetryPolicy(new RetryNTimesPolicy(retryTimes),
                new RandomTimeIntervalPolicy(retryPeriod_ms));
    }

    private RetryPolicyFactory() {
    }

    public static RetryPolicy defaultRetryPolicy() {
        return defaultRetryPolicy;
    }

    public static RetryPolicy socketRetryPolicy() {
        return socketRetryPolicy;
    }
}
