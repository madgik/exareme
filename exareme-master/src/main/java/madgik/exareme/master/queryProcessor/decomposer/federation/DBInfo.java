/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.util.HashMap;
import java.util.Set;

/**
 * @author dimitris
 */
public class DBInfo {

    private HashMap<String, DB> dbs;

    public DBInfo() {
        this.dbs = new HashMap<String, DB>();
    }

    public void addDB(String db) {
        dbs.put(db, new DB(db));
    }

    public void setUserForDB(String db, String s) {
        dbs.get(db).setUser(s);
    }

    public void setPassForDB(String db, String s) {
        dbs.get(db).setPass(s);
    }

    public void setDriverForDB(String db, String s) {
        dbs.get(db).setDriver(s);
    }

    public void setURLForDB(String db, String s) {
        dbs.get(db).setURL(s);
    }

    public void setMadisStringForDB(String db, String s) {
        dbs.get(db).setMadisString(s);
    }

    public boolean existsDB(String dbID) {
        return dbs.containsKey(dbID);
    }

    public DB getDB(String dbID) {
        return dbs.get(dbID);
    }

    public DB getDBForMadis(String madis) {
        for (DB db : this.dbs.values()) {
            if (db.getMadisString().equals(madis)) {
                return db;
            }
        }
        return null;
    }

    public Set<String> getAllDBIDs() {
        return dbs.keySet();
    }

    void setSchemaForDB(String db, String s) {
        dbs.get(db).setSchema(s);
    }
}
