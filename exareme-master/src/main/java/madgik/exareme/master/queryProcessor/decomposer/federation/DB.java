/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

/**
 * @author dimitris
 */
public class DB {

    private String id;
    private String jdbcURL;
    private String user;
    private String pass;
    private String driver;
    private String schema;
    private String madisFunctionString;

    DB(String db) {
        this.id = db;
    }

    public void setUser(String s) {
        user = s;
    }

    public void setPass(String s) {
        pass = s;
    }

    public void setURL(String s) {
        jdbcURL = s;
    }

    public void setDriver(String s) {
        driver = s;
    }

    public void setMadisString(String s) {
        madisFunctionString = s;
    }

    public String getMadisString() {
        return this.madisFunctionString;
    }

    void setSchema(String s) {
        schema = s;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getDriver() {
        return driver;
    }

    public String getURL() {
        return this.jdbcURL;
    }

    public String getPass() {
        return this.pass;
    }

    public String getUser() {
        return this.user;
    }

}
