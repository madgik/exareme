package madgik.exareme.utils.collections;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author konikos
 */
public class CollectionsUtilTest extends TestCase {

    private static List<Integer> makeIntegerArrayList() {
        List<Integer> list = new ArrayList();

        for (int i = 0; i < 25; i++) {
            list.add(i);
        }

        return list;
    }

    /**
     * Test of randomChoice method, of class CollectionsUtil.
     */
    public void testRandomChoice_Collection() {
        System.out.println("randomChoice");

        Collection<Integer> collection = makeIntegerArrayList();

        Integer i = CollectionsUtil.randomChoice(collection);
        Assert.assertTrue(i >= 0 && i < 25);
    }

    /**
     * Test of randomChoice method, of class CollectionsUtil.
     */
    public void testRandomChoice_List() {
        System.out.println("randomChoice");

        List<Integer> list = makeIntegerArrayList();

        Integer i = CollectionsUtil.randomChoice(list);
        Assert.assertTrue(i >= 0 && i < 25);
    }
}
