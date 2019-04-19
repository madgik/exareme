/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.check;

/**
 * @author heraldkllapi
 */
public class Check {
    public static <T> T NotNull(T obj) throws RuntimeException {
        return NotNull(obj, "Object should be not null");
    }

    public static <T> T NotNull(T obj, String msg) throws RuntimeException {
        if (obj == null) {
            throw new RuntimeException(msg);
        }
        return obj;
    }

    public static void Equals(Object o1, Object o2) throws RuntimeException {
        if (!o1.equals(o2)) {
            throw new RuntimeException("Objects should be equal (" + o1 + " != " + o2 + ")");
        }
    }

    public static void True(boolean cond) throws RuntimeException {
        True(cond, "Condition should be true");
    }

    public static void True(boolean cond, String msg) throws RuntimeException {
        if (!cond) {
            throw new RuntimeException(msg);
        }
    }
}
