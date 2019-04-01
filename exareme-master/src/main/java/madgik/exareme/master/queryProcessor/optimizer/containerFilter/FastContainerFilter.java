/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.containerFilter;

import madgik.exareme.master.queryProcessor.optimizer.ContainerFilter;

import java.util.Arrays;

/**
 * @author herald
 */
public class FastContainerFilter implements ContainerFilter {

    private boolean[] containers = null;
    private int filtered = 0;

    public FastContainerFilter(int numOfContainers) {
        containers = new boolean[numOfContainers];
        Arrays.fill(containers, false);
    }

    public void useContainer(int container) {
        containers[container] = true;
        ++filtered;
    }

    @Override
    public boolean canUseContainer(int cNum) {
        return containers[cNum];
    }

    @Override
    public int getNumberOfFilterefContainers() {
        return filtered;
    }
}
