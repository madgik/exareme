/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.combinatorics;

import java.math.BigInteger;

/**
 * @author herald
 */
public class Choose {

    public static BigInteger[][] complete(int n) {
        BigInteger[][] choose = new BigInteger[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                choose[i][j] = BigInteger.ZERO;
            }
        }
        choose[0][0] = BigInteger.ONE;
        for (int i = 1; i < n; i++) {
            choose[i][0] = BigInteger.ONE;
            for (int j = 1; j <= i; j++) {
                if (choose[i - 1][j - 1] == null) {
                    choose[i - 1][j - 1] = BigInteger.ZERO;
                }

                if (choose[i - 1][j] == null) {
                    choose[i - 1][j] = BigInteger.ZERO;
                }
                choose[i][j] = choose[i - 1][j - 1].add(choose[i - 1][j]);
            }
        }
        return choose;
    }
}
