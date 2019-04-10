/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.statistics;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ZipfTest {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.OFF);
        long start = 0;
        long end = 0;
        int items = 10000;
        int calls = 30000000;

        for (double z = 1.0; z < 10.0; z += 1.0) {
            int count[] = new int[items];
            Zipf zipf = new Zipf(items, z, 1, true);

            start = System.currentTimeMillis();
            for (int i = 0; i < calls; i++) {
                count[zipf.next()]++;
            }
            end = System.currentTimeMillis();

            System.out.println("Bin : (" + z + ") " +
                    (zipf.binComparisons + zipf.seqComparisons) + "(" + (end - start) + ")");
        }
    }
}
