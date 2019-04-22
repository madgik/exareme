/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RetryPolicy {

    private RetryTimesPolicy retryTimesPolicy = null;
    private RetryTimeIntervalPolicy retryTimeIntervalPolicy = null;

    public RetryPolicy(RetryTimesPolicy retryTimesPolicy,
                       RetryTimeIntervalPolicy retryTimeIntervalPolicy) {
        this.retryTimesPolicy = retryTimesPolicy;
        this.retryTimeIntervalPolicy = retryTimeIntervalPolicy;
    }

    public RetryTimesPolicy getRetryTimesPolicy() {
        return retryTimesPolicy;
    }

    public RetryTimeIntervalPolicy getRetryTimeInterval() {
        return retryTimeIntervalPolicy;
    }
}
