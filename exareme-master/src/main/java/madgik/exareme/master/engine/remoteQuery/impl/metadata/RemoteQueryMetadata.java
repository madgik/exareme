/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.metadata;

import madgik.exareme.master.engine.remoteQuery.impl.cache.QueryRequests;
import madgik.exareme.master.engine.remoteQuery.impl.utility.Date;
import madgik.exareme.master.engine.remoteQuery.impl.utility.Files;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.*;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class RemoteQueryMetadata implements Metadata {

    //Variables' declaration
    private final File directory;
    private static String directory_path = "/home/christos/metadata";
    private final String table;
    private final Statement statement;
    private final Connection connection;
    //Log Declaration
    private static final Logger log = Logger.getLogger(RemoteQueryMetadata.class);

    public RemoteQueryMetadata(String file) throws ClassNotFoundException, SQLException, Exception {

        boolean created;

        table = getMetadataTableName();

        if (file != null) {
            directory_path = file + "/metadata";
        }

        directory = Files.createDir(directory_path);

        Class.forName("org.sqlite.JDBC");

        String databaseName = directory + "/" + table;
        String jdbcName = "jdbc:sqlite";
        String sDbUrl = jdbcName + ":" + databaseName;

        connection = DriverManager.getConnection(sDbUrl);

        statement = connection.createStatement();

        createTables();

    }

    /*
     * Function which returns the metadata directory path
     */
    public static String getDirectoryPath() {
        return directory_path;
    }

    /*
     * Function which returns the table name of the metadata
     */
    public static String getMetadataTableName() {
        String metadataTableName = "metadata.db";
        return metadataTableName;
    }

    /*
     * Function which adds the storage directory
     */
    @Override
    public void addStorageDirectory(String storageDirectory) throws SQLException {

        String insert_query =
                "INSERT INTO general_info(`storage_directory`, " + "`max_database_id`) VALUES('"
                        + storageDirectory + "', 0)";

        statement.executeUpdate(insert_query);
    }

    /*
     * Function which initialize the cache directory and size
     */
    @Override
    public void initializeCacheInfo(String cacheDirectory) throws SQLException {

        String update_query = "UPDATE general_info set `cache_directory` = '" + cacheDirectory
                + "', `cache_size` = 0";

        statement.executeUpdate(update_query);
    }

    /*
     * Function which updates the directory of the storage
     */
    @Override
    public void updateStorageDirectory(String storageDirectory) throws SQLException {

        String update_query =
                "UPDATE general_info set `storage_directory` = '" + storageDirectory + "'";

        statement.executeUpdate(update_query);
    }

    /*
     * Function which updates the directory of the cache
     */
    @Override
    public void updateCacheDirectory(String cacheDirectory) throws SQLException {

        String update_query =
                "UPDATE general_info set `cache_directory` = '" + cacheDirectory + "'";

        statement.executeUpdate(update_query);
    }

    /*
     * Function which updates the cache size
     */
    @Override
    public void updateCacheSize(double cacheSize) throws SQLException {

        String update_query = "UPDATE general_info set `cache_size` = '" + cacheSize + "'";

        statement.executeUpdate(update_query);
    }

    /*
     * Function which adds a new cache record
     */
    @Override
    public void addNewCacheRecord(String database, String table, String query,
                                  double size, double benefit, QueryRequests request) throws SQLException {

        if (request == null) {
            request = new QueryRequests();
        }

        PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT INTO queries(`database`, `table`, "
                        + "`query`, `size`, `last_update`, `storage_time`, `benefit`, "
                        + "`number_of_requests`, " + "`number_of_last_version_requests`, "
                        + "`number_of_total_requests`, " + "`query_response_time`, "
                        + "`number_of_versions`) " + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        insertStatement.setString(1, database);
        insertStatement.setString(2, table);
        insertStatement.setString(3, query);
        insertStatement.setDouble(4, size);
        insertStatement.setString(5, Date.getCurrentDateTime());
        insertStatement.setString(6, Date.getCurrentDateTime());
        insertStatement.setDouble(7, benefit);
        insertStatement.setInt(8, request.numberOfRequests);
        insertStatement.setInt(9, request.numberOfLastVersionRequests);
        insertStatement.setInt(10, request.numberOfTotalRequests);
        insertStatement.setInt(11, request.queryResponseTime);
        insertStatement.setInt(12, request.numberOfVersions);

        insertStatement.execute();
    }

    /*
     * Function which update the maximum databaseID
     */
    @Override
    public void updateDBID(long maxID) throws SQLException {

        String update_query = "UPDATE general_info set `max_database_id` = " + maxID;

        statement.executeUpdate(update_query);
    }

    /*
     * Function which updates a cache record
     */
    @Override
    public void updateCacheRecord(String query, double benefit, QueryRequests request)
            throws SQLException {

        if (request == null) {
            request = new QueryRequests();
        }

        PreparedStatement insertStatement = connection.prepareStatement(
                "UPDATE queries set `last_update` = ?, `benefit` = ?, "
                        + "`number_of_requests` = ?, `number_of_last_version_requests` = ?,"
                        + " `number_of_total_requests` = ?, `query_response_time` = ?, "
                        + "`number_of_versions` = ? WHERE `query` = ?");

        insertStatement.setString(1, Date.getCurrentDateTime());
        insertStatement.setDouble(2, benefit);
        insertStatement.setInt(3, request.numberOfRequests);
        insertStatement.setInt(4, request.numberOfLastVersionRequests);
        insertStatement.setInt(5, request.numberOfTotalRequests);
        insertStatement.setInt(6, request.queryResponseTime);
        insertStatement.setInt(7, request.numberOfVersions);
        insertStatement.setString(8, query);

        insertStatement.execute();
    }

    /*
     * Function which updates the number of total requests
     */
    @Override
    public void updateNumberTotalRequests() throws SQLException {

        PreparedStatement updateStatement = connection.prepareStatement(
                "UPDATE queries set `number_of_total_requests` " + "= `number_of_total_requests` + 1");

        updateStatement.execute();
    }

    /*
     * Function which deletes a cache record
     */
    @Override
    public void deleteCacheRecord(String database) throws SQLException {

        String delete_query = "DELETE FROM queries WHERE `database` = '" + database + "'";

        statement.executeUpdate(delete_query);
    }

    /*
     * Function which closes the sql connection
     */
    @Override
    public void close() throws SQLException {
        statement.close();
        connection.close();
    }
    /*
     * Function which creates the metadata tables
     */

    private void createTables() throws SQLException {

        String create_query = "CREATE TABLE IF NOT EXISTS queries(`database` "
                + "TEXT PRIMARY KEY, `table` TEXT, `query` BLOB, `size` REAL, "
                + "`last_update` DATETIME DEFAULT current_timestamp, "
                + "`storage_time` DATETIME DEFAULT current_timestamp, "
                + "`benefit` REAL, `number_of_requests` INTEGER, "
                + "`number_of_last_version_requests` INTEGER, " + "`number_of_total_requests` INTEGER, "
                + "`query_response_time` INTEGER, " + "`number_of_versions` INTEGER)";

        statement.executeUpdate(create_query);

        create_query = "CREATE TABLE IF NOT EXISTS general_info("
                + "`storage_directory` TEXT, `cache_directory` TEXT, "
                + "`cache_size` REAL, `max_database_id` INTEGER)";

        statement.executeUpdate(create_query);
    }
}
