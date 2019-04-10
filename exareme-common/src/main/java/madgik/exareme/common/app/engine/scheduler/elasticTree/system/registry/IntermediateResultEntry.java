/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.registry;

/**
 * @author heraldkllapi
 */
public class IntermediateResultEntry implements Entry {
    public final int dataflowId;
    public final String name;
    private final String id;
    private final double size_MB;

    public IntermediateResultEntry(int dataflowId, String name, double size_MB) {
        this.dataflowId = dataflowId;
        this.name = name;
        this.id = Registry.createId(name, dataflowId);
        this.size_MB = size_MB;
    }

    @Override
    public double getSize_MB() {
        return size_MB;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "" + getId();
    }
}
