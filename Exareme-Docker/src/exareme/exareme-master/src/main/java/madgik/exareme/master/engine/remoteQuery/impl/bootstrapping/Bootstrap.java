/*
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.bootstrapping;

import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;
import madgik.exareme.utils.association.Pair;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public interface Bootstrap {

    public CacheInfo getCacheInfo() throws SQLException;

    public List<CachedDataInfo> getCacheIndexList(CacheInfo cacheInfo, String storagePath);

    public Pair<String, Long> getStorageDirectory() throws SQLException;

    public void updateDirectories(String directory) throws SQLException;

    public void close() throws SQLException;
}
