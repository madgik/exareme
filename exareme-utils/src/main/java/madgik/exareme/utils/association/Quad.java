/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.association;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author herald
 */
public class Quad<A, B, C, D> implements Serializable {

    private static final long serialVersionUID = 1L;
    public A a = null;
    public B b = null;
    public C c = null;
    public D d = null;

    public Quad(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public static <A, B, C extends Comparable<C>, D> Comparator<Quad<A, B, C, D>> getCComparator() {
        return new Comparator<Quad<A, B, C, D>>() {
            @Override public int compare(Quad<A, B, C, D> o1, Quad<A, B, C, D> o2) {
                return o1.c.compareTo(o2.c);
            }
        };
    }

    public static <A, B, C extends Comparable<C>, D> Comparator<Quad<A, B, C, D>> getCComparatorDesc() {
        return new Comparator<Quad<A, B, C, D>>() {
            @Override public int compare(Quad<A, B, C, D> o1, Quad<A, B, C, D> o2) {
                return o2.c.compareTo(o1.c);
            }
        };
    }
}
