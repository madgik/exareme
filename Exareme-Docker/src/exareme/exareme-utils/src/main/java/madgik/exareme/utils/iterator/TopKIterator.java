/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.iterator;

import java.util.Iterator;

/**
 * @param <T>
 * @author herald
 */
public class TopKIterator<T> implements Iterator<T> {
    private int k = 0;
    private Iterator<T> iter = null;
    private int current = 0;

    public TopKIterator(int k, Iterator<T> iter) {
        this.k = k;
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        if (current >= k) {
            return false;
        }

        return iter.hasNext();
    }

    @Override
    public T next() {
        current++;
        return iter.next();
    }

    @Override
    public void remove() {
        iter.remove();
    }
}
