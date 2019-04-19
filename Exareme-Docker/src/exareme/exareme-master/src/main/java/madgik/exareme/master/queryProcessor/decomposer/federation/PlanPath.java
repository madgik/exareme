/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author dimitris
 */
public class PlanPath {

    private List<Integer> choices;
    /*repartition:-1
     * bcast:-2
     */

    @Override
    public String toString() {
        return "PlanPath{" + "choices=" + choices + '}';
    }

    public PlanPath() {
        this.choices = new ArrayList<Integer>();
    }

    public void addOption(int o) {
        this.choices.add(o);
    }

    public Iterator<Integer> getPlanIterator() {
        return this.choices.iterator();
    }

    void removeOption(int i) {
        this.choices.remove(i);
    }

    void removeLastOption() {
        this.choices.remove(choices.size() - 1);
    }

    void addOption(int o, int pos) {
        this.choices.add(pos, o);
    }


}
