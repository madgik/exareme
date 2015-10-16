/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.association;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An object pair.
 *
 * @param <A>
 * @param <B>
 * @author herald
 */
public class Pair<A, B> implements Serializable {

    private static final long serialVersionUID = 1L;
    public A a = null;
    public B b = null;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A extends Comparable<A>, B> Comparator<Pair<A, B>> getAComparator() {
        return new Comparator<Pair<A, B>>() {
            @Override public int compare(Pair<A, B> o1, Pair<A, B> o2) {
                return o1.a.compareTo(o2.a);
            }
        };
    }

    public static <A extends Comparable<A>, B> Comparator<Pair<A, B>> getAComparatorDesc() {
        return new Comparator<Pair<A, B>>() {
            @Override public int compare(Pair<A, B> o1, Pair<A, B> o2) {
                return o2.a.compareTo(o1.a);
            }
        };
    }

    public static <A, B extends Comparable<B>> Comparator<Pair<A, B>> getBComparator() {
        return new Comparator<Pair<A, B>>() {
            @Override public int compare(Pair<A, B> o1, Pair<A, B> o2) {
                return o1.b.compareTo(o2.b);
            }
        };
    }

    public static <A, B extends Comparable<B>> Comparator<Pair<A, B>> getBComparatorDesc() {
        return new Comparator<Pair<A, B>>() {
            @Override public int compare(Pair<A, B> o1, Pair<A, B> o2) {
                return o2.b.compareTo(o1.b);
            }
        };
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override public String toString() {
        return "<" + a + "," + b + ">";
    }
}
