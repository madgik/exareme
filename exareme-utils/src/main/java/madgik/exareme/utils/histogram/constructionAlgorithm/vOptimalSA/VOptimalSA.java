/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.constructionAlgorithm.vOptimalSA;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.simulatedAnnealing.SimulatedAnnealing;
import madgik.exareme.utils.simulatedAnnealing.State;
import madgik.exareme.utils.simulatedAnnealing.Temperature;
import madgik.exareme.utils.simulatedAnnealing.Transformation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

public class VOptimalSA extends SimulatedAnnealing {

    private static final long serialVersionUID = 1L;
    private ArrayList<Pair<?, Double>> data = null;
    private int bucketNum = 0;

    public VOptimalSA(int maxSteps, int stepsNotImprovedTermination, Temperature temperature,
        ArrayList<Pair<?, Double>> data, int bucketNum) {
        super(maxSteps, stepsNotImprovedTermination, temperature);

        this.data = data;
        this.bucketNum = bucketNum;
    }

    @Override public State getInitial() throws RemoteException {
        int[] thresholds = new int[bucketNum - 1];

    /* Initialize thresholds */
        int step = data.size() / bucketNum;
        for (int i = 0; i < thresholds.length; i++) {
            thresholds[i] = (i + 1) * step;
        }

        return new VOptimalState(data, thresholds);
    }

    @Override public Pair<Transformation, Transformation> getNeighbor(State state, Random rand)
        throws RemoteException {
        VOptimalState vos = (VOptimalState) state;

        int threshold = 0;
        int offset = 0;

        while (true) {
      /* Peek a random threshold */
            threshold = rand.nextInt(bucketNum - 1);
            int leftOffset = 0;
            int rightOffset = 0;

            if (threshold == 0) {
                leftOffset = vos.thresholds[threshold] - 1;
            } else {
                leftOffset = vos.thresholds[threshold] - vos.thresholds[threshold - 1] - 1;
            }

            if (threshold == bucketNum - 2) {
                rightOffset = data.size() - vos.thresholds[threshold] - 1;
            } else {
                rightOffset = vos.thresholds[threshold + 1] - vos.thresholds[threshold] - 1;
            }

            if (leftOffset > 0 || rightOffset > 0) {
                if (rand.nextBoolean()) {
                    if (leftOffset > 0) {
                        offset = -1;
                    } else {
                        offset = 1;
                    }
                } else {
                    if (rightOffset > 0) {
                        offset = 1;
                    } else {
                        offset = -1;
                    }
                }

                break;
            }
        }

        Transformation t1 = new VOptimalTransformation(threshold, offset);
        Transformation t2 = new VOptimalTransformation(threshold, -offset);

        return new Pair<Transformation, Transformation>(t1, t2);
    }
}
