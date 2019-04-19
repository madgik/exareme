/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi.tree;

/**
 * @author heraldkllapi
 */
public class TreeUtils {

    public static int getReductionPerLevel(int leafs, int height) {
        // height = log_b(leafs)
        // b^height = leafs
        double b = Math.pow(leafs, 1.0 / (height - 1));
        return (int) Math.ceil(b);
    }
}
