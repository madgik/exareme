/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.containerFilter;

import madgik.exareme.master.queryProcessor.optimizer.ContainerFilter;

/**
 * @author herald
 */
public class RangeContainerFilter implements ContainerFilter {
    private int min;
    private int max;

    public RangeContainerFilter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean canUseContainer(int cNum) {
        return min <= cNum && cNum <= max;

    }

    public int getNumberOfFilterefContainers() {
        return max - min + 1;
    }
}
