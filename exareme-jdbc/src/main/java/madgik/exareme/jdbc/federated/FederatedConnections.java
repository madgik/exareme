/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.jdbc.federated;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author dimitris
 */
public class FederatedConnections {
    private Map<Schema, Connection> fedCons;

    public FederatedConnections() {
        this.fedCons=new HashMap<>();
    }
    public List<String> getSchemasForDB(String dbID){
        List res=new ArrayList<>();
        for(Schema s:fedCons.keySet()){
            if(s.getId().equals(dbID)){
                res.add(s.getSchema());
            }
        }
        return res;
    }

    public void putSchema(Schema schema, Connection conn) {
        this.fedCons.put(schema, conn);
    }

    public void removeSchema(Schema schema) {
        this.fedCons.remove(schema);
    }

    public boolean isEmpty() {
        return this.fedCons.isEmpty();
    }

    public Connection getFirstConnection() {
        return this.fedCons.values().iterator().next();
    }

    

    public Set<Connection> getDistinctDBConnections() {
        Set<Connection> res=new HashSet<>();
        for(Connection c:this.fedCons.values()){
            res.add(c);
        }
        return res;
    }

    public Set<Schema> getSchemas() {
        return this.fedCons.keySet();
    }

    public Connection getConnection(Schema nextEndpoint) {
        return this.fedCons.get(nextEndpoint);
    }
}
