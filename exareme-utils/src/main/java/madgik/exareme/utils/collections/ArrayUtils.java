/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.collections;

/**
 * @author herald
 */
public class ArrayUtils {

    private ArrayUtils() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static int getMaxIndex(double[] values) {
        if (values.length == 0) {
            return -1;
        }

        int maxIndex = 0;
        double maxValue = values[0];

        for (int i = 1; i < values.length; i++) {
            if (values[i] > maxValue) {
                maxIndex = i;
                maxValue = values[i];
            }
        }

        return maxIndex;
    }

    //  private static double[][] copyOf2D()

    public static void replaceAll(int oldValue, int newValue, int[] values) {
        for (int i = 1; i < values.length; i++) {
            if (values[i] == oldValue) {
                values[i] = newValue;
            }
        }
    }
}
