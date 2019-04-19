/*
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.metadata;

import madgik.exareme.master.engine.remoteQuery.impl.cache.QueryRequests;

import java.sql.SQLException;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public interface Metadata {

    public void addStorageDirectory(String storageDirectory) throws SQLException;

    public void initializeCacheInfo(String cacheDirectory) throws SQLException;

    public void updateStorageDirectory(String storageDirectory) throws SQLException;

    public void updateDBID(long maxID) throws SQLException;

    public void updateCacheDirectory(String cacheDirectory) throws SQLException;

    public void updateCacheSize(double cacheSize) throws SQLException;

    public void addNewCacheRecord(String database, String table, String query, double size,
                                  double benefit, QueryRequests request) throws SQLException;

    public void updateCacheRecord(String query, double benefit, QueryRequests request)
            throws SQLException;

    public void updateNumberTotalRequests() throws SQLException;

    public void deleteCacheRecord(String database) throws SQLException;

    public void close() throws SQLException;
}
