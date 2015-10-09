package madgik.exareme.master.queryProcessor.decomposer.federation;


public class CentralizedMemoValue implements MemoValue {
    private boolean used;
    private SinglePlan p;
    private boolean materialized;
    private boolean federated;

    public CentralizedMemoValue(SinglePlan p) {
        this.p = p;
        materialized = false;
        used = false;
        federated = false;
    }

    public SinglePlan getPlan() {
        return p;
    }

    public void setMaterialized(boolean b) {
        this.materialized = b;
    }

    public boolean isMaterialised() {
        return this.materialized;
    }

    public void setUsed(boolean b) {
        used = b;
    }

    public boolean isUsed() {
        return used;
    }

    public void setFederated(boolean f) {
        this.federated = f;
    }

    public boolean isFederated() {
        return this.federated;
    }

}
