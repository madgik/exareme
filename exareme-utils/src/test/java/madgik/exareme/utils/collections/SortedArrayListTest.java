/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.collections;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

/**
 * @author herald
 */
public class SortedArrayListTest extends TestCase {

    private static Logger log = Logger.getLogger(SortedArrayListTest.class);

    public SortedArrayListTest() {
    }

    /**
     * Test of getItem method, of class ListUtil.
     */
    public void testInsertSorted() {
        SortedArrayList<Integer> list = new SortedArrayList<Integer>(new Comparator<Integer>() {

            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        Random rand = new Random(0);
        for (int i = 0; i < 100000; i++) {
            list.add(rand.nextInt(100));
        }
        log.debug("Inserted Items: " + list.size());
        Iterator<Integer> iter = list.sortedIterator();
        int previous = iter.next();
        while (iter.hasNext()) {
            int current = iter.next();
            Assert.assertFalse(previous > current);
            previous = current;
        }

        log.info("OK!");
    }
}
