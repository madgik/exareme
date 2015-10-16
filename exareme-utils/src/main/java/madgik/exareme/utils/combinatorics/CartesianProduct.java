/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.combinatorics;

import java.util.Arrays;

/**
 * @author herald
 */
public class CartesianProduct {
    private int current = 0;
    private int[] indexes = null;
    private int[] indexesLimits = null;
    private long total = 1;

    public CartesianProduct(int[] indexes) {
        this.indexes = indexes;
        indexesLimits = new int[indexes.length];

        Arrays.fill(indexes, 0);
        Arrays.fill(indexesLimits, 0);
    }

    public void setLimit(int index, int limit) {
        indexesLimits[index] = limit;
        total *= limit;
    }

    public void setLimitAll(int limit) {
        for (int i = 0; i < indexes.length; i++) {
            setLimit(i, limit);
        }
    }

    public boolean next() {
        indexes[indexes.length - 1]++;
        for (int i = indexes.length - 1; i > 0; i--) {
            if (indexes[i] < indexesLimits[i]) {
                break;
            }
            indexes[i] = 0;
            indexes[i - 1]++;
        }
        if (indexes[0] == indexesLimits[0]) {
            indexes[0] = 0;
            current++;
            return false;
        }
        return true;
    }

    public long getTotalCount() {
        return total;
    }
}
