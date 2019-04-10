/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.unlimited;

import madgik.exareme.master.engine.remoteQuery.impl.bootstrapping.Bootstrap;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.QueryCache;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation.QueryInfo;
import madgik.exareme.master.engine.remoteQuery.impl.metadata.Metadata;
import madgik.exareme.master.engine.remoteQuery.impl.utility.Files;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */

/*
 * Cache implementation which has unlimited storage
 */
public class UnlimitedCache implements QueryCache {

    private final CacheInfo cacheInfo;
    private final Metadata metadata;
    private final String directory;
    private final HashMap<String, CachedDataInfo> cacheIndex;
    private static final Logger log = Logger.getLogger(UnlimitedCache.class);

    public UnlimitedCache(String dir, Metadata metadata) throws Exception {

        boolean created;

        directory = dir;
        cacheIndex = new HashMap<String, CachedDataInfo>();

        //Creation of cache directory
        File file = Files.createDir(directory);

        cacheInfo = new CacheInfo(directory, 0);

        this.metadata = metadata;
    }

    @Override
    public void boot(Bootstrap bootstrap, String storagePath) {

        CacheInfo info = null;
        System.out.println("arxi boot unlimited");

        try {
            info = bootstrap.getCacheInfo();
            if (info != null) {
                cacheInfo.currentSize = info.currentSize;
                if (!directory.equals(info.directory)) {
                    metadata.updateCacheDirectory(directory);
                }
                info = null;
            } else {
                this.metadata.initializeCacheInfo(directory);
            }
        } catch (SQLException ex) {
            return;
        }

        List<CachedDataInfo> list = bootstrap.getCacheIndexList(cacheInfo, storagePath);

        for (CachedDataInfo cachedDataInfo : list) {
            cacheIndex.put(cachedDataInfo.query, cachedDataInfo);
        }
    }

    @Override
    public CacheInfo getInfo() {
        return cacheInfo;
    }

    @Override
    public CachedDataInfo getCacheInfo(String query) {

        return cacheIndex.get(query);
    }

    @Override
    public void setCacheInfo(CachedDataInfo info) {

        cacheInfo.currentSize += info.size;

        try {
            metadata.updateCacheSize(cacheInfo.currentSize);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(UnlimitedCache.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        cacheIndex.put(info.query, info);
    }

    @Override
    public void unpinQueryResults(String query, CachedDataInfo info) {

        //No need of this action in unlimited cache. It does not throw
        //Exception in order to have same interface with the other cache
        //implementations
    }

    @Override
    public void updateCache(String query) {

        //No need of this action in unlimited cache. It does not throw
        //Exception in order to have same interface with the other cache
        //implementations
    }

    @Override
    public boolean isEmpty() {

        return cacheIndex.isEmpty();
    }

    @Override
    public double getTotalCacheSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTotalCacheSize(double size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pinQuery(String query, boolean firstRequestOfTheBatch) {

        //No need of this action in unlimited cache. It does not throw
        //Exception in order to have same interface with the other cache
        //implementations
    }

    @Override
    public boolean isPinned(String query, CachedDataInfo info) {

        return false;
    }

    @Override
    public QueryInfo getQueryInfo(String query) {

        return null;
    }
}
