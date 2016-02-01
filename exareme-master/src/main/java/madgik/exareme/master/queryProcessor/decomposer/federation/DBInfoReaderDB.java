/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.sql.*;

/**
 * @author dimitris
 */
public class DBInfoReaderDB {

    public static DBInfo dbInfo;

    public static void read(String filename) throws ClassNotFoundException {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");
        dbInfo = new DBInfo();
        Connection connection = null;
        try {
            // create a database connection
            if (!filename.endsWith("/")) {
                filename += "/";
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + filename + "endpoints.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.



            ResultSet rs = statement.executeQuery("select * from endpoint");
            while (rs.next()) {
                String db = rs.getString("id");
                dbInfo.addDB(db);
                dbInfo.setUserForDB(db, rs.getString("user"));
                dbInfo.setPassForDB(db, rs.getString("pass"));
                dbInfo.setDriverForDB(db, rs.getString("driver"));
                dbInfo.setURLForDB(db, rs.getString("url"));
                dbInfo.setMadisStringForDB(db, rs.getString("madis"));
                dbInfo.setSchemaForDB(db, rs.getString("schema"));
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }
        }
    }
    
    public static NamesToAliases readAliases(String filename) throws ClassNotFoundException {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");
        NamesToAliases n2a=new NamesToAliases();
        Connection connection = null;
        try {
            // create a database connection
            if (!filename.endsWith("/")) {
                filename += "/";
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + filename + "endpoints.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.


            
            ResultSet rs = statement.executeQuery("select * from aliases");
            int counter=0;
            while (rs.next()) {
                String t = rs.getString("tablename");
                String[] aliases=rs.getString("aliases").split(" ");
                //for(int i=0;i<aliases.length;i++){
                	n2a.addTable(t, aliases);
                	counter+=aliases.length;
               // }
            }
            n2a.setCounter(counter);
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }
        }
        return n2a;
    }
}

