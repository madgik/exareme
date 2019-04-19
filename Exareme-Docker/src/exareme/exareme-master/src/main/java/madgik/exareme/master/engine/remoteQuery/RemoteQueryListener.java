/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery;

import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public interface RemoteQueryListener {

    /*
     * Function which will be called by the server to inform a client
     * that it's request is completed
     *
     * @param   file    Info about the results of the request
     * @param   error   Exception about any error which happened
     */
    public void finished(CachedDataInfo file, String error);

    /**
     * Function which validates if the listener has been called by the cache
     *
     * @return
     */
    public boolean isFinished();

    /**
     * Function which returns the results
     *
     * @return
     */
    public CachedDataInfo getResults();
}
