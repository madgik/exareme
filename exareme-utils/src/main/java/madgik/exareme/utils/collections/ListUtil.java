/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.collections;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author herald
 */
public class ListUtil {

    private ListUtil() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static <T> T getItem(ArrayList<T> list, int index) {
        if (list.size() <= index) {
            ListUtil.setItem(list, index, null);
        }
        return list.get(index);
    }

    public static <T> void setItem(ArrayList<T> list, int index, T value) {
        if (list.size() > index) {
            list.set(index, value);
            return;
        }
        list.ensureCapacity(index + 1);
        int addBefore = index - list.size();
        for (int i = 0; i < addBefore; i++) {
            list.add(null);
        }
        list.add(value);
    }

    public static <T extends Comparable<T>> void insertSorted(ArrayList<T> list, T object) {
        int index = Collections.binarySearch(list, object);
        if (index < 0) {
            index = -index - 1;
        }
        list.add(index, object);
    }
}
