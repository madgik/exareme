package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.utils.graph.edge.AbstractEdge;

/**
 * @author konikos
 */
final public class AbstractQueryGraphEdge extends AbstractEdge<AbstractOperator> {

    private double size_MB = 0.0;

    public AbstractQueryGraphEdge(AbstractOperator source, AbstractOperator target) {
        this(source, target, 0.0);
    }

    public AbstractQueryGraphEdge(AbstractOperator source, AbstractOperator target,
        double size_MB) {
        super(source, target);
        this.size_MB = size_MB;
    }

    public double getSize_MB() {
        return this.size_MB;
    }
}
