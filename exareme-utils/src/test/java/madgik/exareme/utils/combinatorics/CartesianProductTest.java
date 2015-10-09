/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.combinatorics;

import junit.framework.TestCase;

/**
 * @author heraldkllapi
 */
public class CartesianProductTest extends TestCase {

    public CartesianProductTest() {
    }

    public static void main(String[] args) {
        int[] idxs = new int[4];
        CartesianProduct product = new CartesianProduct(idxs);
        product.setLimit(0, 2);
        product.setLimit(1, 3);
        product.setLimit(2, 1);
        product.setLimit(3, 3);

        do {
            System.out.println(idxs[0] + " : " + idxs[1] + " : " + idxs[2] + " : " + idxs[3]);
        } while (product.next());
    }

    public void testGetTotalCount() {
        int[] indexes = new int[4];
        CartesianProduct cartesianProduct = new CartesianProduct(indexes);
        for (int i = 0; i < indexes.length; i++) {
            cartesianProduct.setLimit(i, 2);
        }

        do {
            for (int i = 0; i < indexes.length; i++) {
                System.out.print(indexes[i]);
            }
            System.out.println();
        } while (cartesianProduct.next());
    }
}
