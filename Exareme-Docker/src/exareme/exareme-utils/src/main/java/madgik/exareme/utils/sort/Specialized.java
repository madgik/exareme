/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.sort;

import madgik.exareme.utils.check.Check;

/**
 * @author heraldkllapi
 */
public class Specialized {

    // Network for N=5, Bose-Nelson Algorithm
    public static void fastSortFive(int[] n) {
        Check.True(n.length == 5, "Length of array not 5: " + n.length);

        // Use registers for efficiency
        int a0 = n[0];
        int a1 = n[1];
        int a2 = n[2];
        int a3 = n[3];
        int a4 = n[4];

        //      SWAP(0, 1);
        if (a0 > a1) {
            a0 = a0 ^ a1;
            a1 = a0 ^ a1;
            a0 = a0 ^ a1;
        }

        //      SWAP(3, 4);
        if (a3 > a4) {
            a3 = a3 ^ a4;
            a4 = a3 ^ a4;
            a3 = a3 ^ a4;
        }

        //      SWAP(2, 4);
        if (a2 > a4) {
            a2 = a2 ^ a4;
            a4 = a2 ^ a4;
            a2 = a2 ^ a4;
        }

        //      SWAP(2, 3);
        if (a2 > a3) {
            a2 = a2 ^ a3;
            a3 = a2 ^ a3;
            a2 = a2 ^ a3;
        }

        //      SWAP(0, 3);
        if (a0 > a3) {
            a0 = a0 ^ a3;
            a3 = a0 ^ a3;
            a0 = a0 ^ a3;
        }

        //      SWAP(0, 2);
        if (a0 > a2) {
            a0 = a0 ^ a2;
            a2 = a0 ^ a2;
            a0 = a0 ^ a2;
        }
        //      SWAP(1, 4);
        if (a1 > a4) {
            a1 = a1 ^ a4;
            a4 = a1 ^ a4;
            a1 = a1 ^ a4;
        }
        //      SWAP(1, 3);
        if (a1 > a3) {
            a1 = a1 ^ a3;
            a3 = a1 ^ a3;
            a1 = a1 ^ a3;
        }
        //      SWAP(1, 2);
        if (a1 > a2) {
            a1 = a1 ^ a2;
            a2 = a1 ^ a2;
            a1 = a1 ^ a2;
        }

        n[0] = a0;
        n[1] = a1;
        n[2] = a2;
        n[3] = a3;
        n[4] = a4;
    }
}
