/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.partitionRule;

/**
 * @author herald
 */
public class PartitionRule {

    public PartitionClass partitionClass = null;
    public PartitionConstraint partitionConstraint = null;

    public PartitionRule(PartitionClass partitionClass, PartitionConstraint partitionConstraint) {
        this.partitionClass = partitionClass;
        this.partitionConstraint = partitionConstraint;
    }
}
