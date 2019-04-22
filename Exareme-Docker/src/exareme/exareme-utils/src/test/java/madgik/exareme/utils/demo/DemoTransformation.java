/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.demo;

import madgik.exareme.utils.simulatedAnnealing.State;
import madgik.exareme.utils.simulatedAnnealing.Transformation;

class DemoTransformation implements Transformation {

    private static final long serialVersionUID = 1L;
    int from;
    int to;

    public DemoTransformation(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public State apply(State state) {
        DemoState ds = (DemoState) state;
        ds.c += (from - to);
        return ds;
    }
}
