/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer;

/**
 * @author herald
 */
public interface ContainerFilter {
    // Return true if the container can be used to assign operators to.
    boolean canUseContainer(int cNum);

    // Return the number of the filtered containers by this filter.
    int getNumberOfFilterefContainers();
}
