/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.experiments;

import madgik.exareme.utils.association.Pair;

import java.util.ArrayList;

/**
 * @author heraldkllapi
 */
public class FunctionSmoothing {
    private final double window;
    private final boolean rangeDivide;
    private ArrayList<Pair<Double, Double>> values = new ArrayList<>();

    public FunctionSmoothing(double window) {
        this(window, false);
    }

    public FunctionSmoothing(double window, boolean rangeDivide) {
        this.window = window;
        this.rangeDivide = rangeDivide;
    }

    public void add(double x, double y) {
        values.add(new Pair<>(x, y));
    }

    public double getValue() {
        double sum = 0.0;
        double count = 0.0;
        Pair<Double, Double> now = values.get(values.size() - 1);
        for (int i = values.size() - 1; i >= 0; i--) {
            Pair<Double, Double> p = values.get(i);
            if (p.a < now.a - window) {
                break;
            }
            sum += p.b;
            count += 1.0;
        }
        return sum / ((rangeDivide) ? window : count);
    }
}
