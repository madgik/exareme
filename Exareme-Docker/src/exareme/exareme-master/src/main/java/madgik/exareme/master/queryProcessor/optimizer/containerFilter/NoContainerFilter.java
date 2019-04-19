/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.containerFilter;

import madgik.exareme.master.queryProcessor.optimizer.ContainerFilter;

/**
 * @author herald
 */
public class NoContainerFilter implements ContainerFilter {
    private static NoContainerFilter instance = new NoContainerFilter();

    public static NoContainerFilter getInstance() {
        return instance;
    }

    public boolean canUseContainer(int cNum) {
        return true;
    }

    public int getNumberOfFilterefContainers() {
        return 0;
    }
}
