package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.util.Collection;
import java.util.List;

import madgik.exareme.master.queryProcessor.decomposer.dag.PartitionCols;



public class PartitionedMemoValue implements MemoValue {
    private SinglePlan p;
    private double repCost;
    private boolean materialized;
    private PartitionCols dlvdPart;
    private boolean used;
    private boolean multiUsed;
    private List<MemoKey> toMat;

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

	@Override
	public void setUsed(boolean b) {
		if(used && b){
			this.multiUsed=true;
		}
		this.used=b;
	}

	@Override
	public boolean isUsed() {
		return used;
	}

	@Override
	public boolean isFederated() {
		return true;
	}

	public Collection<? extends MemoKey> getToMat() {
		return toMat;
	}

	public void setToMat(List<MemoKey> toMaterialize) {
		toMat=toMaterialize;
	}

	public boolean isMultiUsed() {
		return multiUsed;
	}
	
	

}
