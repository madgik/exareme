/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.dag.PartitionCols;
import madgik.exareme.master.queryProcessor.decomposer.query.Column;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dimitris
 */
public class Memo {
    private final Map<MemoKey, MemoValue> memo = new HashMap<MemoKey, MemoValue>();

    public Memo() {
    }

    public MemoValue getMemoValue(MemoKey k) {
        return memo.get(k);
    }

    public boolean containsMemoKey(MemoKey ec) {
        return memo.containsKey(ec);
    }

    public void put(Node e, SinglePlan resultPlan, Column c, double repCost, PartitionCols l, List<MemoKey> toMaterialize) {
        MemoKey k = new MemoKey(e, c);
        PartitionedMemoValue v = new PartitionedMemoValue(resultPlan, repCost);
        v.setDlvdPart(l);
        v.setToMat(toMaterialize);
        memo.put(k, v);

    }

    public void put(Node e, SinglePlan resultPlan, Column c, double repCost, boolean b,
        PartitionCols l) {
        MemoKey k = new MemoKey(e, c);
        PartitionedMemoValue v = new PartitionedMemoValue(resultPlan, repCost);
        v.setMaterialized(b);
        v.setDlvdPart(l);
        memo.put(k, v);
    }

    public void setPlanUsed(MemoKey e) {
        MemoValue v =  getMemoValue(e);
        v.setUsed(true);
        if(v.isMaterialised()){
        	return;
        }
        SinglePlan p = v.getPlan();
        for (int i = 0; i < p.noOfInputPlans(); i++) {
            MemoKey sp = p.getInputPlan(i);
            
            if(sp.getNode().getDescendantBaseTables().size()==1 && !this.getMemoValue(sp).isFederated()){
            	continue;            	
            }
            setPlanUsed(sp);

        }

    }

    public void put(Node e, SinglePlan resultPlan, boolean used, boolean federated) {
        MemoKey k = new MemoKey(e, null);
        CentralizedMemoValue v = new CentralizedMemoValue(resultPlan);
        v.setFederated(federated);
        memo.put(k, v);

    }

    public void put(Node e, SinglePlan resultPlan, boolean materialized, boolean used,
        boolean federated) {
        MemoKey k = new MemoKey(e, null);
        CentralizedMemoValue v = new CentralizedMemoValue(resultPlan);
        v.setFederated(federated);
        v.setMaterialized(materialized);
        memo.put(k, v);

    }



}
