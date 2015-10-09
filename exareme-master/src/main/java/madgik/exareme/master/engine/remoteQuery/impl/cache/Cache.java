/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache;

import madgik.exareme.master.engine.remoteQuery.impl.bootstrapping.Bootstrap;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public interface Cache {

    public void boot(Bootstrap bootstrap, String storagePath);

    public CachedDataInfo getCacheInfo(String query);

    public void setCacheInfo(CachedDataInfo info);

    public void updateCache(String query);

    void pinQuery(String query, boolean firstEequestOfTheBatch);

    void unpinQueryResults(String query, CachedDataInfo info);

    public boolean isPinned(String query, CachedDataInfo info);

    public boolean isEmpty();
}
