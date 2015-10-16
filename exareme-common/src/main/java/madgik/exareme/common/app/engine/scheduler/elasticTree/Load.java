/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree;

/**
 * @author heraldkllapi
 */
public class Load implements Comparable<Load> {
    private final int id;
    private final boolean isInt;
    private double load_double;
    private int load_int;

    public Load(int id) {
        this.id = id;
        isInt = true;
    }

    public Load(int id, double load) {
        this.id = id;
        this.load_double = load;
        this.isInt = false;
    }

    public Load(int id, int load) {
        this.id = id;
        this.load_int = load;
        this.isInt = true;
    }

    @Override public int compareTo(Load o) {
        if (isInt != o.isInt) {
            throw new RuntimeException("Not same objects");
        }
        if (isInt) {
            if (load_int == o.load_int) {
                return Integer.compare(id, o.id);
            }
            return Integer.compare(load_int, o.load_int);
        } else {
            if (load_double == o.load_double) {
                return Integer.compare(id, o.id);
            }
            return Double.compare(load_double, o.load_double);
        }
    }

    public int getId() {
        return id;
    }

    public int getIntLoad() {
        return load_int;
    }

    public double getLoad() {
        return (isInt) ? load_int : load_double;
    }

    public void loadDelta(int delta) {
        load_int += delta;
    }

    @Override public String toString() {
        return "(" + id + " = " + ((isInt) ? load_int : load_double) + ")";
    }
}
