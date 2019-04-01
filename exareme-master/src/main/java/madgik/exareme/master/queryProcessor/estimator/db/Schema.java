package madgik.exareme.master.queryProcessor.estimator.db;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * @author jim
 */
public class Schema {

    private String schemaName;
    private Map<String, RelInfo> tableIndex;

    /*constructor*/
    public Schema(String schemaName, Map<String, RelInfo> relIndex) {
        this.schemaName = schemaName;
        this.tableIndex = relIndex;
    }

    /*copy constructor*/
    public Schema(Schema schema) {
        this.schemaName = schema.getSchemaName();
        this.tableIndex = new HashMap<String, RelInfo>();

        for (Map.Entry<String, RelInfo> entry : schema.tableIndex.entrySet()) {
            this.tableIndex.put(entry.getKey(), new RelInfo(entry.getValue()));
        }

    }

    /*getters and setters*/
    public String getSchemaName() {
        return schemaName;
    }

    public Map<String, RelInfo> getTableIndex() {
        return tableIndex;
    }

    /*interface methods*/


    /*standard methods*/
    @Override
    public String toString() {
        return "Schema{" + "schemaName=" + schemaName + ", tableIndex=" + tableIndex + '}';
    }


}
