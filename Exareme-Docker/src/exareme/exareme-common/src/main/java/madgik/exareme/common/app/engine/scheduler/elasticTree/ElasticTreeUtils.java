/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree;

/**
 * @author heraldkllapi
 */
public class ElasticTreeUtils {

    public static int findOperatorLevel(String operatorName) {
        return Integer.parseInt(operatorName.split("_")[1]);
    }

    public static int findOperatorRank(String operatorName) {
        return Integer.parseInt(operatorName.split("_")[2]);
    }

    public static void findProcOpLevelAndRank(String operatorName, int[] lvlRank) {
        // Example: R_internal_0.4.0 -> [internal,0]
        int level = 0;
        int rank = 0;
        if (operatorName.startsWith("R_leaf")) {
            rank = Integer.parseInt(operatorName.split("_")[2].split("\\.")[0]);
        }
        // Internal
        if (operatorName.startsWith("R_internal")) {
            level = 1;
            rank = Integer.parseInt(operatorName.split("_")[2].split("\\.")[0]);
        }
        // Root
        if (operatorName.startsWith("R_root")) {
            level = 2;
            rank = 0;
        }
        lvlRank[0] = level;
        lvlRank[1] = rank;
    }

    public static void findDataOpLevelAndRank(String operatorName, int[] lvlRank) {
        // Example: b_R_internal_0_R_root_0.ICM_TO.5.0  -> [root,0]
        String[] parts = operatorName.split("_");
        String levelString = parts[5];
        int level = 0;
        int rank = Integer.parseInt(parts[6].split("\\.")[0]);
        if (levelString.equals("internal")) {
            level = 1;
        }
        if (levelString.equals("root")) {
            level = 2;
        }
        lvlRank[0] = level;
        lvlRank[1] = rank;
    }
}
