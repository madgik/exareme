/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.math;

/**
 * @author heraldkllapi
 */
public class Logarithm {

    public static int DiscreeteLogCeil(int num, int base) {
        long total = 1;
        int log = 0;
        while (total < num) {
            total *= base;
            ++log;
        }
        return log;
    }

    public static int DiscreeteLogFloor(int num, int base) {
        long total = 1;
        int log = 1;
        while (total < num) {
            total *= base;
            ++log;
        }
        return (total == num) ? log : (log - 1);
    }

    public static int DiscreeteLogCeil2(int num, int base) {
        return (int) Math.ceil(Math.log10(num) / Math.log10(base));
    }

    public static int DiscreeteLogFloor2(int num, int base) {
        return (int) Math.floor(Math.log10(num) / Math.log10(base));
    }
}
