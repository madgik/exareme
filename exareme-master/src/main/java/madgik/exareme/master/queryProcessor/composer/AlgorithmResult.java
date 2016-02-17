package madgik.exareme.master.queryProcessor.composer;

/**
 * @author alex
 */
public class AlgorithmResult {
    private String queryKey;



    private String status;

    public AlgorithmResult(String queryKey, String status) {
        this.queryKey = queryKey;
        this.status = status;
    }

    public AlgorithmResult(String queryKey) {
        this.queryKey = queryKey;
    }

    public AlgorithmResult() {
    }

    public String getQueryKey() {
        return queryKey;
    }

    public void setQueryKey(String queryKey) {
        this.queryKey = queryKey;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
