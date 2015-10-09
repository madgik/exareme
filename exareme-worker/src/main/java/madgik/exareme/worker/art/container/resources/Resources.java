/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.resources;

/**
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 */
public abstract class Resources implements Comparable<Resources> {

    double[] resourceArray = new double[ResourceName.values().length];

    public Resources(double memory) {
        this.resourceArray[ResourceName.memory.ordinal()] = memory;
    }

    public double[] getResourceArray() {
        return resourceArray;
    }

    public boolean hasResources() {
        for (int i = 0; i < ResourceName.values().length; ++i) {
            if (resourceArray[i] != 0) {
                return true;
            }
        }
        return false;
    }

    @Override public int compareTo(Resources other) {
        return (resourceArray[0] < other.getResourceArray()[0]) ?
            -1 :
            ((resourceArray[0] == other.getResourceArray()[0]) ? 0 : 1);

    }

    @Override public String toString() {
        return Double.toString(resourceArray[0]);
    }
}
