/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.iterator;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @param <T> the object type.
 * @author herald
 */
public class SkipNullIterable<T> implements Iterable<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private Iterable<T> collection = null;

    public SkipNullIterable(Iterable<T> collection) {
        this.collection = collection;
    }

    @Override
    public Iterator<T> iterator() {
        return new SkipNullIterator<T>(collection.iterator());
    }
}
