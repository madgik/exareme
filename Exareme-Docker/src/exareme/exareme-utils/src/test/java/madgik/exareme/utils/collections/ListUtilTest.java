/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.collections;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * @author herald
 */
public class ListUtilTest extends TestCase {
    private static Logger log = Logger.getLogger(ListUtilTest.class);

    public ListUtilTest() {
    }

    /**
     * Test of getItem method, of class ListUtil.
     */
    public void testInsertSorted() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        Random rand = new Random(0);
        for (int i = 0; i < 100000; i++) {
            ListUtil.insertSorted(list, rand.nextInt(100));
        }
        for (int i = 1; i < list.size(); ++i) {
            Assert.assertFalse(list.get(i - 1) > list.get(i));
        }
    }

    public void testSort() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        Random rand = new Random(0);
        for (int i = 0; i < 100000; i++) {
            list.add(rand.nextInt(100));
        }
        Collections.sort(list);
        for (int i = 1; i < list.size(); ++i) {
            Assert.assertFalse(list.get(i - 1) > list.get(i));
        }
    }
}
