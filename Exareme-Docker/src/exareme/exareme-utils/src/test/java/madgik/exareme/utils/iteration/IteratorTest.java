package madgik.exareme.utils.iteration;

import madgik.exareme.utils.iterator.SkipNullIterable;

import java.util.ArrayList;

/**
 * @author herald
 */
public class IteratorTest {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 20; i++) {
            list.add(i);
        }

        for (int i = 0; i < 20; i++) {
            if (i % 2 == 0) {
                list.set(i, null);
            }
        }

        for (Integer i : new SkipNullIterable<Integer>(list)) {
            System.out.println(i);
        }
    }
}
