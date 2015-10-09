/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.collections;

import java.util.*;

/**
 * @param <T>
 * @author herald
 */
public class SortedArrayList<T> extends ArrayList<T> {
    private static final long serialVersionUID = 1L;

    private Comparator<T> comparator = null;
    private boolean sorted = true;

    public SortedArrayList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void invalidate() {
        sorted = false;
    }

    @Override public boolean add(T e) {
        boolean v = super.add(e);
        if (v) {
            sorted = false;
        }

        return v;
    }

    @Override public void add(int index, T element) {
        super.add(index, element);
        sorted = false;
    }

    @Override public boolean addAll(Collection<? extends T> c) {
        boolean v = super.addAll(c);
        if (v) {
            sorted = false;
        }

        return v;
    }

    @Override public boolean addAll(int index, Collection<? extends T> c) {
        boolean v = super.addAll(index, c);
        if (v) {
            sorted = false;
        }

        return v;
    }

    public Iterator<T> sortedIterator() {
        if (sorted == false) {
            Collections.sort(this, comparator);
        }
        return iterator();
    }
}
