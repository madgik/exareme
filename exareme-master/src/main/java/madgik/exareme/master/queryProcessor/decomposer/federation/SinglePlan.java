/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.query.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dimitris
 */
public class SinglePlan {

    private int choice;
    private double cost;
    private PlanRepartitionInfo rep;
    private List<MemoKey> inputPlans;
    //private int repCounter;

    public SinglePlan(double cost, PlanRepartitionInfo reps) {
        this.cost = cost;
        this.rep = reps;
        //repCounter=0;
        this.inputPlans = new ArrayList<MemoKey>();
    }

    public SinglePlan(double cost) {
        this.cost = cost;
        this.rep = new PlanRepartitionInfo();
        //repCounter=0;
        this.inputPlans = new ArrayList<MemoKey>();
    }

    public void setChoice(int c) {
        this.choice = c;
    }

    public int getChoice() {
        return choice;
    }

    public double getCost() {
        return this.cost;
    }


    public void setCost(double planCost) {
        this.cost = planCost;
    }

    public int noOfInputPlans() {
        return this.inputPlans.size();
    }


    public void addRepartitionBeforeOp(Column c) {
        this.rep.setRepBeforeOp(c);
    }

    public void addRepartitionAfterOp(int i, Column c) {
        this.rep.setRepAfterOp(i, c);
    }


    public void addInputPlan(Node e, Column c) {
        this.inputPlans.add(new MemoKey(e, c));
    }

    public MemoKey getInputPlan(int i) {
        return this.inputPlans.get(i);
    }

    public Column getRepartitionBeforeOp() {
        return this.rep.getRepBeforeOp();
    }

    public Column getRepartitionAfterOp(int i) {
        return this.rep.getRepAfterOp(i);
    }

    void increaseCost(double c) {
        this.cost += c;
    }



}
