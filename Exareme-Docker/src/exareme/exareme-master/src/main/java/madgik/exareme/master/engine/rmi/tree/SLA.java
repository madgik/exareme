package madgik.exareme.master.engine.rmi.tree;

import madgik.exareme.utils.association.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panos on 7/5/14.
 */
public class SLA {

    // cost -> upper time bound
    private List<Pair<Double, Double>> costs = new ArrayList<Pair<Double, Double>>();

    public SLA() {
        this.costs.add(new Pair<Double, Double>(0.0, 0.0));
    }

    public void append(double cost, double time) {
        this.costs.add(new Pair<Double, Double>(cost, time));
    }

    public double getCostAtTime(double time) {
        for (Pair<Double, Double> cost : this.costs) {
            if (time <= cost.getB()) {
                return cost.getA();
            }
        }
        return 0.0f;
    }
}
