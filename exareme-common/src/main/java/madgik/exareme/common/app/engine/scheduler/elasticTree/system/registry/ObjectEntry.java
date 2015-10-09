/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.registry;

/**
 * @author heraldkllapi
 */
public class ObjectEntry implements Entry {
    private final String id;
    private final double size_MB;

    public ObjectEntry(String id, double size_MB) {
        this.id = id;
        this.size_MB = size_MB;
    }

    @Override public String getId() {
        return id;
    }

    @Override public double getSize_MB() {
        return size_MB;
    }
}
