package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.query.Column;

import java.util.HashMap;
import java.util.Map;

public class CentralizedMemo {

    private final Map<MemoKey, CentralizedMemoValue> memo =
        new HashMap<MemoKey, CentralizedMemoValue>();

    public CentralizedMemo() {
    }

    public CentralizedMemoValue getMemoValue(MemoKey k) {
        return memo.get(k);
    }

    public boolean containsMemoKey(MemoKey ec) {
        return memo.containsKey(ec);
    }


    public void setPlanUsed(MemoKey e) {
        CentralizedMemoValue v = getMemoValue(e);
        v.setUsed(true);
        SinglePlan p = v.getPlan();
        for (int i = 0; i < p.noOfInputPlans(); i++) {
            MemoKey sp = p.getInputPlan(i);
            setPlanUsed(sp);

        }

    }

    public void put(Node e, SinglePlan resultPlan, Column c, int i) {
        MemoKey k = new MemoKey(e, c);
        CentralizedMemoValue v = new CentralizedMemoValue(resultPlan);
        memo.put(k, v);

    }



}
