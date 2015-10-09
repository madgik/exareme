package madgik.exareme.utils.association;

/**
 * @author Christoforos Svingos
 *         Container to ease passing around a tuple of two objects. This object provides a sensible
 *         implementation of equals(), returning true if equals() is true on each of the contained
 *         objects.
 */
public class SimplePair<F, S> {
    public final F first;
    public final S second;

    /**
     * Constructor for a Pair.
     *
     * @param first  the first object in the Pair
     * @param second the second object in the pair
     */
    public SimplePair(F first, S second) {
        this.first = first;
        this.second = second;
    }
}
