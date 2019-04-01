/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.demo;

import madgik.exareme.utils.simulatedAnnealing.State;

import java.util.Random;

class DemoState implements State {

    private static final long serialVersionUID = 1L;
    double c;

    public DemoState() {
        c = new Random().nextDouble();
    }

    public double getCost() {
        return c;
    }

    @Override
    public State clone() {
        DemoState s = new DemoState();
        s.c = this.c;
        return s;
    }
}
