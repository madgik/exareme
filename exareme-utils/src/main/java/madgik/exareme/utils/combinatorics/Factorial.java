/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.combinatorics;

import org.apache.log4j.Logger;

import java.math.BigInteger;

/**
 * @author herald
 */
public class Factorial {

    private static Logger log = Logger.getLogger(Factorial.class);

    public static BigInteger[] complete(int n) {
        BigInteger[] fact = new BigInteger[n];
        fact[0] = BigInteger.ONE;
        for (int i = 1; i < n; i++) {
            fact[i] = fact[i - 1].multiply(new BigInteger(i + ""));
        }
        return fact;
    }

    private static int max(int x, int y) {
        //    int absx = x * (x >> 31);
        //    int absy = y * (y >> 31);
        //    int shift = (absx + absy);

        //    x += shift;
        //    y += shift;

        //    System.out.println(": " + x + " " + y);

        int diff = (x - y);
        //    System.out.println("d: " + diff);
        int abs = diff & ~(1 << 31);
        System.out.println(5 & ~(1 << 31));
        //    System.out.println("abs: " + abs);
        int max = (x + y + abs) >> 1;
        //    System.out.println("max: " + max);
        return max;
    }

    private static int max2(int x, int y) {
        int c = x - y;
        int k = (c >> 31) & 0x1;
        int max = x - k * c;
        return max;
    }

    public static void main(String[] args) {
        System.out.println(max(6, 20));
        System.out.println(max2(6, 20));
    }
}
