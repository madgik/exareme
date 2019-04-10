/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class ServerInfo {

    public String sqlDatabase;          /* Sql Database(eg SQLite, MySQL) */

    public String ip;           /* Server ip  */

    public String port;         /* Server port */

    public String DBName;       /* Specify the database of execution */

    public String username;     /* Username of the server */

    public String password;     /* Password of the server */


    public ServerInfo(String serverDatabase, String serverIP, String serverPort, String dbName,
                      String dbUsername, String dbPassword) {

        sqlDatabase = serverDatabase;
        ip = serverIP;
        port = serverPort;
        DBName = dbName;
        username = dbUsername;
        password = dbPassword;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ServerInfo other = (ServerInfo) obj;

        if ((this.ip == null) ? (other.ip != null) : !this.ip.equals(other.ip)) {
            return false;
        }
        if ((this.port == null) ? (other.port != null) : !this.port.equals(other.port)) {
            return false;
        }
        if ((this.DBName == null) ? (other.DBName != null) : !this.DBName.equals(other.DBName)) {
            return false;
        }
        if ((this.username == null) ?
                (other.username != null) :
                !this.username.equals(other.username)) {
            return false;
        }

        return !((this.password == null) ?
                (other.password != null) :
                !this.password.equals(other.password));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.ip != null ? this.ip.hashCode() : 0);
        hash = 43 * hash + (this.port != null ? this.port.hashCode() : 0);
        hash = 43 * hash + (this.DBName != null ? this.DBName.hashCode() : 0);
        hash = 43 * hash + (this.username != null ? this.username.hashCode() : 0);
        hash = 43 * hash + (this.password != null ? this.password.hashCode() : 0);
        return hash;
    }
}
