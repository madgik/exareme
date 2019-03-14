/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl;

import madgik.exareme.master.engine.remoteQuery.RemoteQuery;
import madgik.exareme.master.engine.remoteQuery.RemoteQueryListener;
import madgik.exareme.master.engine.remoteQuery.ServerInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheAlgorithm;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public final class SynchronousRemoteQuery implements RemoteQuery {

    private final RemoteQuery asynchronousRemoteQuery;

    public SynchronousRemoteQuery(File metadataPath, File storagePath, File cachePath,
                                  CacheAlgorithm algorithm) throws Exception {
        asynchronousRemoteQuery =
                new AsynchronousRemoteQuery(metadataPath, storagePath, cachePath, algorithm);
    }

    public SynchronousRemoteQuery(File metadataPath, File storagePath, File cachePath,
                                  CacheAlgorithm algorithm, int storageSize) throws Exception {
        asynchronousRemoteQuery =
                new AsynchronousRemoteQuery(metadataPath, storagePath, cachePath, algorithm,
                        storageSize);
    }

    @Override
    public void schedule(ServerInfo server, String query, final RemoteQueryListener listener,
                         ProcessManager procManager, String table) throws RemoteException, IOException {

        final Object wake = new Object();

        RemoteQueryListener synchronizedListener;
        synchronizedListener = new RemoteQueryListener() {

            private final Object lock = new Object();
            public boolean finish = false;
            CachedDataInfo results;

            @Override
            public void finished(CachedDataInfo file, String error) {

                if (listener != null) {
                    listener.finished(file, error);
                }
                results = file;
                synchronized (lock) {
                    finish = true;
                }
                synchronized (wake) {
                    wake.notify();
                }
            }

            @Override
            public boolean isFinished() {

                synchronized (lock) {
                    return finish;
                }
            }

            @Override
            public CachedDataInfo getResults() {
                return results;
            }

        };
        asynchronousRemoteQuery.schedule(server, query, synchronizedListener, procManager, table);

        synchronized (wake) {
            while (!synchronizedListener.isFinished()) {
                try {
                    wake.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SynchronousRemoteQuery.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void schedule(ServerInfo server, String query, final RemoteQueryListener listener,
                         ProcessManager procManager, String table, double staleLimit)
            throws RemoteException, IOException {

        final Object wake = new Object();

        RemoteQueryListener synchronizedListener;
        synchronizedListener = new RemoteQueryListener() {

            private final Object lock = new Object();
            public boolean finish = false;
            CachedDataInfo results;

            @Override
            public void finished(CachedDataInfo file, String error) {
                listener.finished(file, error);
                results = file;
                synchronized (lock) {
                    finish = true;
                }
                synchronized (wake) {
                    wake.notify();
                }
            }

            @Override
            public boolean isFinished() {

                synchronized (lock) {
                    return finish;
                }
            }

            @Override
            public CachedDataInfo getResults() {
                return results;
            }

        };
        asynchronousRemoteQuery
                .schedule(server, query, synchronizedListener, procManager, table, staleLimit);

        synchronized (wake) {
            while (!synchronizedListener.isFinished()) {
                try {
                    wake.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SynchronousRemoteQuery.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void finished(String query, CachedDataInfo info) throws RemoteException {

        asynchronousRemoteQuery.finished(query, info);
    }

    @Override
    public void close() {

        asynchronousRemoteQuery.close();
    }

    @Override
    public void printData() {
        asynchronousRemoteQuery.printData();
    }

    @Override
    public void setReplacementAlgorithm(CacheAlgorithm algorithm, int storageSize) {

        asynchronousRemoteQuery.setReplacementAlgorithm(algorithm, storageSize);
    }

}
