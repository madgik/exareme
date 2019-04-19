/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.query.Column;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dimitris
 */
public class PlanRepartitionInfo {

    private Column repBeforeOp;
    private Map<Integer, Column> repsAferOp;

    public PlanRepartitionInfo() {
        this.repBeforeOp = null;
        this.repsAferOp = new HashMap<Integer, Column>();
    }

    public Column getRepBeforeOp() {
        return repBeforeOp;
    }

    public void setRepBeforeOp(Column repBeforeOp) {
        this.repBeforeOp = repBeforeOp;
    }

    public Column getRepAfterOp(int i) {
        if (repsAferOp.containsKey(i)) {
            return repsAferOp.get(i);
        } else {
            return null;
        }
    }

    void setRepAfterOp(int i, Column c) {
        this.repsAferOp.put(i, c);
    }

}
