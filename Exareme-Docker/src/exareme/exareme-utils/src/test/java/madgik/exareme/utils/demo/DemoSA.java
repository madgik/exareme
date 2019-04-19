/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.demo;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.simulatedAnnealing.SimulatedAnnealing;
import madgik.exareme.utils.simulatedAnnealing.State;
import madgik.exareme.utils.simulatedAnnealing.Temperature;
import madgik.exareme.utils.simulatedAnnealing.Transformation;

import java.rmi.RemoteException;
import java.util.Random;

class DemoSA extends SimulatedAnnealing {

    private static final long serialVersionUID = 1L;

    public DemoSA(int maxSteps, int stepsNotImprovedTermination, Temperature temperature) {
        super(maxSteps, stepsNotImprovedTermination, temperature);
    }

    @Override
    public State getInitial() throws RemoteException {
        return new DemoState();
    }

    @Override
    public Pair<Transformation, Transformation> getNeighbor(State state, Random rand)
            throws RemoteException {
        int from = rand.nextInt(100);
        int to = rand.nextInt(100);
        Transformation t1 = new DemoTransformation(from, to);
        Transformation t2 = new DemoTransformation(to, from);
        return new Pair<Transformation, Transformation>(t1, t2);
    }
}
