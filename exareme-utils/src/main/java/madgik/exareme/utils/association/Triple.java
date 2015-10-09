/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.association;

import java.io.Serializable;

/**
 * @param <A>
 * @param <B>
 * @param <C>
 * @author herald
 */
public class Triple<A, B, C> implements Serializable {

    private static final long serialVersionUID = 1L;
    public A a;
    public B b;
    public C c;

    public Triple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    @Override public String toString() {
        return "(" + a.toString() + "," + b.toString() + "," + c.toString() + ")";
    }
}
