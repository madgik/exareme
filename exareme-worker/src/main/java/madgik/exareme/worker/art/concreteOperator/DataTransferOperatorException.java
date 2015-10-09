package madgik.exareme.worker.art.concreteOperator;

import java.util.List;

/**
 * Created by vagos on 21/5/2015.
 */
public class DataTransferOperatorException extends Exception {
    private List<String> failedOut;
    private long opendesc;

    public DataTransferOperatorException(List<String> failedOut, long opendesc) {
        this.failedOut = failedOut;
        this.opendesc = opendesc;
    }

    public List<String> getFailedOut() {
        return failedOut;
    }

    public long getOpenFileDescCount() {
        return opendesc;
    }
}
