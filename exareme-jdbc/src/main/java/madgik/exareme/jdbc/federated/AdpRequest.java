/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.jdbc.federated;

/* 
 * A simple class that holds an sql query to be send through the connection, along
 * with some required query metadata e.g. type etc.
 *
 * @author dimitris
 */
public class AdpRequest {
    private String query;
    private String type; //update, query, execute
    private String db;
    private String table;

    public AdpRequest(String q, String t, String db) {
        this.table = "--";
        this.query = q;
        this.type = t;
        this.db = db;
    }

    public String getType() {
        return this.type;
    }

    public String getSQL() {
        return this.query;
    }

    public String getDb() {
        return db;
    }
}
