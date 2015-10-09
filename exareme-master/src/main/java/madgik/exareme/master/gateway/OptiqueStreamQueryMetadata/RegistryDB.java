package madgik.exareme.master.gateway.OptiqueStreamQueryMetadata;

import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.utils.association.SimplePair;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoforos Svingos
 */
public class RegistryDB {

    private static final Logger log = Logger.getLogger(RegistryDB.class);
    //  private static String registryPath =  PropertyFactory.getRestServerProperty().
    //          getString("registry.path");

    public static synchronized void createSchema() {
        Connection c = null;
        Statement stmt = null;
        try {
            File file = new File(ExaremeGatewayUtils.GW_REGISTRY_PATH);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + ExaremeGatewayUtils.GW_REGISTRY_PATH);

            stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS register_query " +
                "(name TEXT PRIMARY KEY NOT NULL, " +
                " query TEXT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch (Exception ex) {
            log.error("SQLError: " + ex.getMessage(), ex);
        }
    }

    public static synchronized void addToRegistry(String streamName, String sqlQuery) {
        Connection c = null;
        PreparedStatement prep = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + ExaremeGatewayUtils.GW_REGISTRY_PATH);

            String sql = "INSERT OR IGNORE INTO register_query (name, query) " + "VALUES (?, ?);";
            prep = c.prepareStatement(sql);

            prep.setString(1, streamName);
            prep.setString(2, sqlQuery);

            prep.execute();

            prep.close();
            c.close();
        } catch (Exception ex) {
            log.error("SQLError: " + ex.getMessage(), ex);
        }

    }

    public static synchronized List<SimplePair<String, String>> getRegisterQueries() {
        List<SimplePair<String, String>> map = new ArrayList<SimplePair<String, String>>();
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + ExaremeGatewayUtils.GW_REGISTRY_PATH);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM register_query;");
            while (rs.next()) {
                String streamName = rs.getString("name");
                String query = rs.getString("query");

                map.add(new SimplePair(streamName, query));
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception ex) {
            log.error("SQLError: " + ex.getMessage(), ex);
        }

        return map;
    }

    public static synchronized void removeFromRegistry(String streamName) {
        Connection c = null;
        PreparedStatement prep = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + ExaremeGatewayUtils.GW_REGISTRY_PATH);

            String sql = "DELETE FROM register_query where name = ?;";
            prep = c.prepareStatement(sql);

            prep.setString(1, streamName);
            prep.execute();

            prep.close();
            c.close();
        } catch (Exception ex) {
            log.error("SQLError: " + ex.getMessage(), ex);
        }
    }

}
