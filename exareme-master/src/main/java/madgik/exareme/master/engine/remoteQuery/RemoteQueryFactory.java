/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery;

import madgik.exareme.master.engine.remoteQuery.impl.AsynchronousRemoteQuery;
import madgik.exareme.master.engine.remoteQuery.impl.SynchronousRemoteQuery;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheAlgorithm;

import java.io.File;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class RemoteQueryFactory {

    public static RemoteQuery remoteQuery;

    public static RemoteQuery createDefaultRemoteQuery(CacheAlgorithm algorithm) throws Exception {

        remoteQuery = new AsynchronousRemoteQuery(null, null, null, algorithm);

        return remoteQuery;
    }

    public static RemoteQuery createDefaultRemoteQuery(CacheAlgorithm algorithm, File metadataPath,
                                                       File storagePath, File cachePath) throws Exception {

        remoteQuery = new AsynchronousRemoteQuery(metadataPath, storagePath, cachePath, algorithm);

        return remoteQuery;
    }

    public static RemoteQuery createRemoteQuery(CacheAlgorithm algorithm, boolean synchronization)
            throws Exception {

        if (synchronization) {
            remoteQuery = new SynchronousRemoteQuery(null, null, null, algorithm);
        } else {
            remoteQuery = new AsynchronousRemoteQuery(null, null, null, algorithm);
        }

        return remoteQuery;
    }

    public static RemoteQuery createRemoteQuery(CacheAlgorithm algorithm, boolean synchronization,
                                                File metadataPath, File storagePath, File cachePath) throws Exception {

        if (synchronization) {
            remoteQuery =
                    new SynchronousRemoteQuery(metadataPath, storagePath, cachePath, algorithm);
        } else {
            remoteQuery =
                    new AsynchronousRemoteQuery(metadataPath, storagePath, cachePath, algorithm);
        }

        return remoteQuery;
    }

}
