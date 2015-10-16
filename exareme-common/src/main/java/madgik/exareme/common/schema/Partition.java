/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema;

import java.io.Serializable;
import java.util.*;

/**
 * @author herald
 */
public class Partition implements Serializable {
    private static final long serialVersionUID = 1L;

    private String table = null;
    private int pNum = 0;
    private Set<String> locations = null;
    private Set<String> partitionColumns = null;

    public Partition(String table, int pNum) {
        this.table = table;
        this.pNum = pNum;
        this.locations = new HashSet<String>();
        this.partitionColumns = new HashSet<String>();
    }

    public String getTable() {
        return table;
    }

    public int getpNum() {
        return pNum;
    }

    public void addLocation(String location) {
        locations.add(location);
    }

    public List<String> getLocations() {
        return new ArrayList<String>(locations);
    }

    public void addPartitionColumn(String column) {
        this.partitionColumns.add(column);
    }

    public List<String> getPartitionColumns() {
        return Collections.unmodifiableList(new ArrayList<String>(partitionColumns));
    }
}
