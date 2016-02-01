/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import org.apache.log4j.Logger;

import java.sql.*;

/**
 * @author dimitris
 */
public class DBInfoWriterDB {

    private static final Logger log = Logger.getLogger(DBInfoWriterDB.class);

    public static void write(String query, String directory) throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        log.debug("Adding endpoint " + query);
        Connection connection = null;
        try {
            // create a database connection
            if (!directory.endsWith("/")) {
                directory += "/";
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + directory + "endpoints.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.



            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "endpoint", null);
            if (!tables.next()) {
                statement.executeUpdate(
                    "create table endpoint (id string, url string, driver string, user string, pass string, madis string, schema string)");
            }


            String sqlParams = query.substring(query.indexOf("(") + 1, query.lastIndexOf(")"));
            if (query.startsWith("addFederatedEndpoint")) {
                sqlParams = sqlParams.replaceAll(" ", "");
                String[] params = sqlParams.split(",");
                String madisString;
                madisString = "";
                String host = "";
                if (params[2].equalsIgnoreCase("com.mysql.jdbc.Driver")) {
                    host = params[1].split(":")[2];
                    if (host.startsWith("//")) {
                        host = host.substring(2);
                    }
                    madisString =
                        "mysql " + "h:" + host + " u:" + params[3] + " p:" + params[4] + " db:"
                            + params[1].split("/")[params[1].split("/").length - 1];
                } else if (params[2].contains("OracleDriver")) {
                    host = params[1];
                    madisString = "oracle " + host + " u:" + params[3] + " p:" + params[4];
                } 
                else if (params[2].contains("postgresql")) {
                    host = params[1].split(":")[2];
                    if (host.startsWith("//")) {
                        host = host.substring(2);
                    }
                    String[] splitted = host.split("/");
                    host = splitted[0];
                    String db = "";
                    String port = "5432";
                    if (splitted.length == 1) {
                        String dbPort = params[1].split(":")[3];
                        port = dbPort.split("/")[0];
                        db = dbPort.split("/")[1];

                    } else {
                        db = splitted[1].split("\\?")[0];
                    }

                    madisString =
                        "postgres " + "h:" + host + " port:" + port + " u:" + params[3] + " p:"
                            + params[4] + " db:" + db;
                }
                statement.executeUpdate("DELETE FROM endpoint WHERE id = '" + params[0] + "';");
                statement.executeUpdate(
                    "INSERT INTO endpoint VALUES ('" + params[0] + "','" + params[1] + "','"
                        + params[2] + "','" + params[3] + "','" + params[4] + "','" + madisString
                        + "','" + params[5] + "');");
                statement.close();

            }

        } catch (SQLException e) {
            // if the error message is "out of memory", 
            // it probably means no database file is found
            log.error(e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                log.error(e);
            }
        }
    }
    
    public static void writeAliases(NamesToAliases n2a, String directory) throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        log.debug("Writing aliases");
        Connection connection = null;
        try {
            // create a database connection
            if (!directory.endsWith("/")) {
                directory += "/";
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + directory + "endpoints.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.



            DatabaseMetaData dbm = connection.getMetaData();
            // check if "employee" table is there
            ResultSet tables = dbm.getTables(null, null, "aliases", null);
            if (!tables.next()) {
                statement.executeUpdate(
                    "create table aliases (tablename string, aliases string)");
            }


           
            for(String tablename:n2a.getTables()){
            	statement.executeUpdate("DELETE FROM aliases WHERE tablename = '" +tablename + "';");
            	String aliasesForTable="";
            	String del="";
            	for(String alias:n2a.getAllAliasesForBaseTable(tablename)){
            		aliasesForTable+=del;
            		aliasesForTable+=alias;
            		del=" ";
            	}
            	statement.executeUpdate(
                        "INSERT INTO aliases VALUES ('" + tablename + "','"  + aliasesForTable + "');");
                    
            }
                
                statement.close();

        } catch (SQLException e) {
            // if the error message is "out of memory", 
            // it probably means no database file is found
            log.error(e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                log.error(e);
            }
        }
    }
}
