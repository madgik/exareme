/*
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache.implementation;

import madgik.exareme.master.engine.remoteQuery.impl.cache.Cache;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation.QueryInfo;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public interface QueryCache extends Cache {

    public CacheInfo getInfo();

    public QueryInfo getQueryInfo(String query);

    public double getTotalCacheSize();

    public void setTotalCacheSize(double size);

}
