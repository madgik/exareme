package madgik.exareme.master.queryProcessor.decomposer.util;



import java.util.Objects;

/**
 * @param <T>
 * @param <U>
 * @author jim
 */
public class Pair<T, U> {
    private T var1;
    private U var2;

    /*constructor*/
    public Pair(T var1, U var2) {
        this.var1 = var1;
        this.var2 = var2;
    }

    /*simple copy constructor*/
    public Pair(Pair<T, U> p) {
        this.var1 = p.var1;
        this.var2 = p.var2;
    }

    /*getters and setters*/
    public T getVar1() {
        return var1;
    }

    public U getVar2() {
        return var2;
    }

    public void setVar1(T var1) {
        this.var1 = var1;
    }

    public void setVar2(U var2) {
        this.var2 = var2;
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.var1);
        hash = 83 * hash + Objects.hashCode(this.var2);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        if (!Objects.equals(this.var1, other.var1)) {
            return false;
        }
        if (!Objects.equals(this.var2, other.var2)) {
            return false;
        }
        return true;
    }

    @Override public String toString() {
        return "Pair{" + "var1=" + var1 + ", var2=" + var2 + '}';
    }



}
