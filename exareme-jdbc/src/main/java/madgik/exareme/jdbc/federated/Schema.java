/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.jdbc.federated;

import java.util.Objects;

/**
 * @author dimitris
 */
public class Schema {
    private String schema;
    private String id;

    public Schema(String dbID, String schemaName) {
        id = dbID;
        schema = schemaName;
    }

    public String getSchema() {
        return schema;
    }

    public String getId() {
        return id;
    }

    @Override public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.schema);
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Schema other = (Schema) obj;
        if (!Objects.equals(this.schema, other.schema)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }


}
