package madgik.exareme.master.queryProcessor.graph;

/**
 * An immutable class that describes an Abstract Operator.
 * Each abstract operator has a unique name.
 *
 * @author konikos
 */
final public class AbstractOperator {
    private final String name;

    public AbstractOperator(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
