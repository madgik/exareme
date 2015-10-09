/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.data;


import madgik.exareme.common.app.engine.scheduler.elasticTree.system.registry.Registry;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.registry.TablePartitionEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class Table implements Serializable {
    private final String name;
    private final String[] attributes;
    private final ArrayList<Double> partitions = new ArrayList<>();
    private double totalSize = 0.0;

    public Table(String name, String... attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public Table(String name, List<String> attributes) {
        this.name = name;
        this.attributes = attributes.toArray(new String[] {});
    }

    public void addPartition(double size_MB) {
        partitions.add(size_MB);
        totalSize += size_MB;
    }

    public String getName() {
        return name;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public int getNumParts() {
        return partitions.size();
    }

    public ArrayList<Double> getPartitions() {
        return partitions;
    }

    public double getTotalSize() {
        return totalSize;
    }

    public void registerTable() {
        for (int i = 0; i < partitions.size(); ++i) {
            TablePartitionEntry entry = new TablePartitionEntry(this, i, partitions.get(i));
            Registry.register(entry);
        }
    }
}
