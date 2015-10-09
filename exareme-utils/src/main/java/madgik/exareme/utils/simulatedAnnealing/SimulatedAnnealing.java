/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.simulatedAnnealing;

import madgik.exareme.utils.association.Pair;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Random;

/**
 * @author herald
 */
public abstract class SimulatedAnnealing implements Serializable {

    private static final long serialVersionUID = 1L;
    private int maxSteps = 0;
    private int stepsNotImprovedTermination = 0;
    private Temperature temperature = null;

    public SimulatedAnnealing(int maxSteps, int stepsNotImprovedTermination,
        Temperature temperature) {
        this.maxSteps = maxSteps;
        this.stepsNotImprovedTermination = stepsNotImprovedTermination;
        this.temperature = temperature;
    }

    /* return: initial state */
    public abstract State getInitial() throws RemoteException;

    /*
     * return: a pair with two transformations A and B with
     * the following properties:
     *  A: A(state) -> new state
     *  B: B(A(state)) -> state
     */
    public abstract Pair<Transformation, Transformation> getNeighbor(State state, Random rand)
        throws RemoteException;

    public State search() throws RemoteException {
        State bestState = getInitial();
        State state = bestState.clone();

        Random rand = new Random();

        long lastBestStep = 0;
        for (int k = 0; k < maxSteps; k++) {
            if (k - lastBestStep > stepsNotImprovedTermination) {
                break;
            }

            Pair<Transformation, Transformation> neighbor = getNeighbor(state, rand);

            double cost = state.getCost();
            state = neighbor.a.apply(state);

      /* Always keep a better state */
            if (bestState.getCost() > state.getCost()) {
                bestState = state.clone();

                lastBestStep = k;
            } else {
        /* Keep the solution with a probability */
                if (propability(cost, state.getCost(), temperature.getTemperature(k)) <= rand
                    .nextDouble()) {
          /* do not accept the solution */
                    state = neighbor.b.apply(state);
                }
            }
        }

        return bestState;
    }

    public double propability(double cost, double newCost, double temperature)
        throws RemoteException {
        if (newCost < cost) {
            return 1.0;
        }

        double prop = Math.pow(Math.E, -(1) / temperature);
        return prop;
    }
}
