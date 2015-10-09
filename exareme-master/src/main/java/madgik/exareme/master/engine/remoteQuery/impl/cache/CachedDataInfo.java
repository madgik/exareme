/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public class CachedDataInfo {

    public CacheInfo cacheInfo;
    public QueryRequests requests;
    public String query;
    public String database;
    public String table;
    public String storagePath;
    public String storageTime;
    public String lastUpdate;
    public double size;
    public double benefit;

    public CachedDataInfo(String database, String table, String query, String storagePath,
        String lastUpdate, CacheInfo cacheInfo, String storageTime, double size) {

        this.cacheInfo = cacheInfo;
        this.database = database;
        this.table = table;
        this.query = query;
        this.storagePath = storagePath;
        this.lastUpdate = lastUpdate;
        this.storageTime = storageTime;
        this.size = size;
    }

    public CachedDataInfo(String database, String table, String query, String storagePath,
        String lastUpdate, CacheInfo cacheInfo, String storageTime, double size, double benefit) {

        this.cacheInfo = cacheInfo;
        this.database = database;
        this.table = table;
        this.query = query;
        this.storagePath = storagePath;
        this.lastUpdate = lastUpdate;
        this.storageTime = storageTime;
        this.size = size;
        this.benefit = benefit;
    }

    public void setQueryRequests(QueryRequests requests) {
        this.requests = requests;
    }

    public String getCachePath() {
        return cacheInfo.directory + "/" + database;
    }
}
