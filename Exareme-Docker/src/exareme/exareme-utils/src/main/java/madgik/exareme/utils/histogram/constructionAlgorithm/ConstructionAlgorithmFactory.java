/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.constructionAlgorithm;

import madgik.exareme.utils.histogram.partitionRule.PartitionConstraint;
import madgik.exareme.utils.histogram.partitionRule.PartitionRule;

/**
 * @author herald
 */
public class ConstructionAlgorithmFactory {

    private ConstructionAlgorithmFactory() {
    }

    public static ConstructionAlgorithm getAlgorithm(PartitionRule partitionRule) {
        if (partitionRule.partitionConstraint == PartitionConstraint.maxdiff) {
            return new MaxDiffConstructionAlgorithm();
        }

        if (partitionRule.partitionConstraint == PartitionConstraint.v_optimal) {
            return new VOptimalConstructionAlgorithm();
        }

        if (partitionRule.partitionConstraint == PartitionConstraint.equi_width) {
            return new EquiWidthConstructionAlgorithm();
        }

        throw new UnsupportedOperationException("Not supported yet!");
    }
}
