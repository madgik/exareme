package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.dag.PartitionCols;



public class PartitionedMemoValue implements MemoValue {
    private SinglePlan p;
    private double repCost;
    private boolean materialized;
    private PartitionCols dlvdPart;

    public PartitionedMemoValue(SinglePlan p, double repCost) {
        this.p = p;
        this.repCost = repCost;
        this.materialized = false;
    }

    public SinglePlan getPlan() {
        return p;
    }

    public double getRepCost() {
        return repCost;
    }

    public void setMaterialized(boolean b) {
        this.materialized = b;
    }

    public boolean isMaterialised() {
        return this.materialized;
    }

    public PartitionCols getDlvdPart() {
        return dlvdPart;
    }

    public void setDlvdPart(PartitionCols dlvdPart) {
        this.dlvdPart = dlvdPart;
    }



}
