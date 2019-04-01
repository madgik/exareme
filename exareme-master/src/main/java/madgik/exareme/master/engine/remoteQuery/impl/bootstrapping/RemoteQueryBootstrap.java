/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.bootstrapping;

import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.QueryRequests;
import madgik.exareme.master.engine.remoteQuery.impl.metadata.RemoteQueryMetadata;
import madgik.exareme.master.engine.remoteQuery.impl.utility.Files;
import madgik.exareme.master.engine.remoteQuery.impl.utility.QueryParser;
import madgik.exareme.utils.association.Pair;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class RemoteQueryBootstrap implements Bootstrap {

    private final String metadataDirectory;
    private final String metadataTable;
    private String cacheDirectory;
    private String storageDirectory;
    private final Statement statement;
    private final Connection connection;

    public RemoteQueryBootstrap() throws ClassNotFoundException, SQLException {

        metadataDirectory = RemoteQueryMetadata.getDirectoryPath();
        metadataTable = RemoteQueryMetadata.getMetadataTableName();

        Class.forName("org.sqlite.JDBC");

        String databaseName = metadataDirectory + "/" + metadataTable;
        String jdbcName = "jdbc:sqlite";
        String sDbUrl = jdbcName + ":" + databaseName;

        connection = DriverManager.getConnection(sDbUrl);

        statement = connection.createStatement();
    }

    @Override
    public void updateDirectories(String directory) throws SQLException {

    }

    @Override
    public void close() throws SQLException {

        statement.close();
        connection.close();
    }

    @Override
    public CacheInfo getCacheInfo() throws SQLException {

        CacheInfo cacheInfo = null;

        String select_query = "SELECT `cache_directory`,`cache_size` " + "FROM general_info";

        ResultSet rs = statement.executeQuery(select_query);

        if (rs.next()) {

            String cacheDir = rs.getString("cache_directory");
            double cacheSize = rs.getDouble("cache_size");
            cacheInfo = new CacheInfo(cacheDir, cacheSize);
            System.out.println("cache info " + cacheDir + "|" + cacheSize + "|");

            cacheDirectory = cacheDir;
        }
        rs.close();

        return cacheInfo;
    }

    @Override
    public List<CachedDataInfo> getCacheIndexList(CacheInfo cacheInfo, String storagePath) {

        HashSet<String> lastResults = new HashSet<String>();
        LinkedList<String> existedDatabases = new LinkedList<String>();
        LinkedList<CachedDataInfo> list = new LinkedList<CachedDataInfo>();

        String select_query = "SELECT `database`,`table`,`query`,`last_update`, "
                + "`storage_time`, `size`, `benefit`, `number_of_requests`,  "
                + "`number_of_last_version_requests`, `number_of_total_requests`,  "
                + "`query_response_time`, `number_of_versions` FROM queries "
                + "ORDER BY DATETIME(`last_update`)";

        try {

            ResultSet rs = statement.executeQuery(select_query);
            CachedDataInfo cachedDataInfo;

            String db, table, query, lastUpdate, storageTime;
            double size, benefit;
            int numberOfRequests, numberOfLastVersionRequests, numberOfTotalRequests;
            int queryResponseTime, numberOfVersions;
            QueryRequests requests;
            while (rs.next()) {
                db = rs.getString("database");
                table = rs.getString("table");
                query = QueryParser.retrieveSingleQuotes(rs.getString("query"));
                lastUpdate = rs.getString("last_update");
                storageTime = rs.getString("storage_time");
                size = rs.getDouble("size");
                benefit = rs.getDouble("benefit");
                System.out.println(
                        "cache data " + db + "|" + table + "|" + query + "|" + lastUpdate + "|"
                                + storageTime + "|" + size + "|" + benefit);

                //If it is the newest version of a query
                if (!lastResults.contains(query)) {
                    //add the database to the existed ones
                    existedDatabases.add(db);
                    //add the query, so as to not insert it again
                    lastResults.add(query);

                    numberOfRequests = rs.getInt("number_of_requests");
                    numberOfLastVersionRequests = rs.getInt("number_of_last_version_requests");
                    numberOfTotalRequests = rs.getInt("number_of_total_requests");
                    queryResponseTime = rs.getInt("query_response_time");
                    numberOfVersions = rs.getInt("number_of_versions");
                    requests = new QueryRequests(numberOfRequests, numberOfLastVersionRequests,
                            numberOfTotalRequests, queryResponseTime, numberOfVersions);

                    cachedDataInfo =
                            new CachedDataInfo(db, table, query, storagePath + "/" + db, lastUpdate,
                                    cacheInfo, storageTime, size, benefit);
                    cachedDataInfo.setQueryRequests(requests);

                    list.add(cachedDataInfo);
                }
            }

            rs.close();

            File[] files = Files.getFilesOfdirectory(storageDirectory);
            if (files != null) {
                for (File file : files) {
                    if (!existedDatabases.contains(file.getName())) {
                        //remove from cache and the storage
                        Files.deleteDBFile(storageDirectory, cacheInfo.directory, file.getName());
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(RemoteQueryBootstrap.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RemoteQueryBootstrap.class.getName()).log(Level.SEVERE, null, ex);
        }

        return list;
    }

    @Override
    public Pair<String, Long> getStorageDirectory() throws SQLException {

        String directory = null;
        long maxID = 0;

        String select_query =
                "SELECT `storage_directory`, `max_database_id`" + " FROM general_info";

        ResultSet rs = statement.executeQuery(select_query);

        if (rs.next()) {
            directory = rs.getString("storage_directory");
            maxID = (long) rs.getInt("max_database_id");

            storageDirectory = directory;
        }
        rs.close();

        return new Pair(directory, maxID);
    }
}
