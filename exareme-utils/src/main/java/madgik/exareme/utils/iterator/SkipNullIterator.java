/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.iterator;

import java.util.Iterator;

/**
 * @param <T> The object type.
 * @author herald
 */
public class SkipNullIterator<T> implements Iterator<T> {

    private Iterator<T> iter = null;
    private T current = null;

    public SkipNullIterator(Iterator<T> iter) {
        this.iter = iter;
    }

    @Override public boolean hasNext() {
        if (current != null) {
            return true;
        }

        while (iter.hasNext()) {
            current = iter.next();
            if (current != null) {
                break;
            }
        }

        return (current != null);
    }

    @Override public T next() {
        T next = current;
        current = null;
        return next;
    }

    @Override public void remove() {
        throw new RuntimeException("Not supported");
    }
}
