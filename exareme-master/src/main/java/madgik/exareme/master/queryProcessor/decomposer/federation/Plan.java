/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.query.Column;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author dimitris
 */
public class Plan {

    private final PlanPath path;
    private double cost;
    private List<Column> repartitions;
    private Iterator<Integer> it;
    private List<MemoKey> inputPlans;
    private int repCounter;

    public Plan(PlanPath path, double cost, List<Column> reps) {
        this.path = path;
        this.cost = cost;
        this.repartitions = reps;
        it = this.path.getPlanIterator();
        repCounter = 0;
        this.inputPlans = new ArrayList<MemoKey>();
    }

    public Plan(PlanPath path, double cost) {
        this.path = path;
        this.cost = cost;
        this.repartitions = new ArrayList<Column>();
        repCounter = 0;
        this.inputPlans = new ArrayList<MemoKey>();
    }

    public double getCost() {
        return this.cost;
    }


    public void setCost(double planCost) {
        this.cost = planCost;
    }

    public PlanPath getPath() {
        return this.path;
    }

    public void addRepartition(Column c) {
        this.repartitions.add(c);
        this.path.addOption(-1);
    }



    // boolean isPtnedOn(Column c) {
    //    return this.partitionRecord.getClassForColumn(this.repartitions.get(this.repartitions.size()-1)).contains(c);
    // }

    public Column getLastRepartition() {
        return this.repartitions.get(this.repartitions.size() - 1);
    }

    public void append(Plan otherPlan) {
        Iterator<Integer> otherChoices = otherPlan.getPath().getPlanIterator();
        while (otherChoices.hasNext()) {
            this.path.addOption(otherChoices.next());
        }
        for (Column rep : otherPlan.repartitions) {
            this.repartitions.add(rep);
        }
    }

    public Iterator<Integer> getPathIterator() {
        return this.path.getPlanIterator();
    }

    public void addInputPlan(Node e, Column c) {
        this.inputPlans.add(new MemoKey(e, c));
    }

    public MemoKey getInputPlan(int i) {
        return this.inputPlans.get(i);
    }

    public Column getNextRepartition() {
        return this.repartitions.get(repCounter++);
    }

    public boolean repartitionsIsEmpty() {
        return this.repartitions.isEmpty();
    }

    public boolean containsRepartitionOnColumn(Column c) {
        if (this.repartitions.isEmpty()) {
            return false;
        } else if (this.repartitions.contains(c)) {
            return true;
        } else {
            return false;
        }
    }

    void increaseCost(double c) {
        this.cost += c;
    }

}
