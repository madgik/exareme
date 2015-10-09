/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.cache;

import madgik.exareme.common.app.engine.scheduler.elasticTree.system.registry.Entry;
import madgik.exareme.common.app.engine.scheduler.elasticTree.system.registry.ObjectEntry;
import madgik.exareme.utils.check.Check;
import madgik.exareme.utils.units.Metrics;
import madgik.exareme.worker.arm.storage.client.ArmStorageClient;
import madgik.exareme.worker.arm.storage.client.ArmStorageClientException;
import madgik.exareme.worker.arm.storage.client.ArmStorageClientFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author heraldkllapi
 */
public class Cache {
    private static final Logger log = Logger.getLogger(Cache.class);
    private final double diskSize_MB;
    private final double PREDICTED_SIZE_MB = 50;
    private final HashMap<String, Entry> cached = new HashMap<>();
    private final HashMap<String, ActiveTransfer> scheduled = new HashMap<>();
    private final CacheReplacementAlgorithm lru = new CacheReplacementAlgorithm();
    private final ReentrantLock lock = new ReentrantLock();
    // Statistics
    private double totalIOData = 0;
    private double cacheSize = 0;
    private double maxCacheSize = 0;
    private int totalRequestCount = 0;
    private int hitCount = 0;

    public Cache(double diskSize_MB) {
        this.diskSize_MB = diskSize_MB;
        log.info("DISK CACHE SIZE: " + diskSize_MB);
    }

    public void deleteAll() {
        cached.clear();
        scheduled.clear();
        lru.clear();
        cacheSize = 0;
    }

    public void fetch(String file) throws RemoteException {
        ActiveTransfer active;
        boolean get = false;
        try {
            lock.lock();
            totalRequestCount++;
            if (cached.containsKey(file)) {
                lru.touch(file);
                hitCount++;
                return;
            }
            active = scheduled.get(file);
            if (active != null) {
                // Wait to transfer if already scheduled
                hitCount++;
                get = false;
            } else {
                // Schedule transfer
                active = new ActiveTransfer(file);
                scheduled.put(file, active);
                get = true;
            }
        } finally {
            lock.unlock();
        }
        if (get) {
            scheduleGet(active);
        } else {
            waitToComplete(active);
        }
    }

    private void waitToComplete(ActiveTransfer transfer) throws RemoteException {
        log.info(Thread.currentThread().getId() + ": " +
            "WAITING TO COMPLETE: " + transfer.getFileName());
        if (transfer.waitToCompete() == false) {
            throw new RemoteException("Cannot get file: " + transfer.getFileName());
        }
    }

    private void scheduleGet(ActiveTransfer active) throws RemoteException {
        log.info(Thread.currentThread().getId() + ": " + "GETTING: " + active.getFileName());
        try {
            lock.lock();
            // Make an intial space for the transfer
            // TODO: get this metadata from HDFS?
            makeFreeSpace(PREDICTED_SIZE_MB);
            deltaCacheSize(PREDICTED_SIZE_MB);
            totalRequestCount++;
        } finally {
            lock.unlock();
        }

        // Do the fetching without locking in parallel
        double fileSize_MB = 0.0;
        boolean success = false;
        ArmStorageClientException exception = null;
        // try to fetch 3 times
        for (int i = 0; i < 3; ++i) {
            try {
                ArmStorageClient storageClient = ArmStorageClientFactory.createArmStorageClient();
                storageClient.connect();
                log.debug("try to fetch (" + i + ") : " + active.getFileName());
                storageClient.fetch(active.getFileName(), active.getFileName());
                fileSize_MB = new File(active.getFileName()).length() / Metrics.MB;
                // Success
                storageClient.disconnect();
                success = true;
                break;
            } catch (ArmStorageClientException e) {
                exception = e;
            }
        }

        try {
            lock.lock();
            if (success == false) {
                deltaCacheSize(-PREDICTED_SIZE_MB);
                scheduled.remove(active.getFileName());
                active.setError();
                throw new RemoteException("Cannot fetch: " + active.getFileName(), exception);
            }
            Entry old = cached
                .put(active.getFileName(), new ObjectEntry(active.getFileName(), fileSize_MB));
            lru.addToCache(active.getFileName());
            Check.True(old == null, "Old object is not null!");
            totalIOData += fileSize_MB;
            double sizeDifference = fileSize_MB - PREDICTED_SIZE_MB;
            if (sizeDifference > 0) {
                // File is larger than predicted: make more space available
                makeFreeSpace(sizeDifference);
            }
            // If file is smaller than predicted, the difference is nagative so the following is ok!
            deltaCacheSize(sizeDifference);
            // Set ready
            scheduled.remove(active.getFileName());
            active.setReady();
        } finally {
            lock.unlock();
        }
    }

    private void makeFreeSpace(double size) {
        // If there is enough space, then do nothing!
        if (cacheSize + size <= diskSize_MB) {
            return;
        }
        // Remove the least recently used objects till the new object fits in the cache
        while (cacheSize + size > diskSize_MB) {
            String idToRemove = lru.removeLRUFromCache();
            Entry entry = cached.remove(idToRemove);
            if (entry == null) {
                if (scheduled.containsKey(idToRemove) == false) {
                    throw new RuntimeException("Object not exists!");
                }
                // Object is being read. Add it back to lru
                lru.addToCache(idToRemove);
            } else {
                // Remove it
                deltaCacheSize(-(entry.getSize_MB()));
                log.debug("Removed: " + idToRemove + "\n Size: " + cacheSize +
                    "\n Object size: " + size +
                    "\n LRU objects: " + lru.getNumObjects());
            }
        }
        log.debug("Final size: " + cacheSize);
    }

    private void deltaCacheSize(double delta) {
        cacheSize += delta;
        log.debug("Cache Size: " + cacheSize);
        if (maxCacheSize < cacheSize) {
            maxCacheSize = cacheSize;
        }
    }

    public double getTotalIOData() {
        return totalIOData;
    }

    public double getCacheSize() {
        return cacheSize;
    }

    public double getMaxCacheSize() {
        return maxCacheSize;
    }

    public int getTotalRequestCount() {
        return totalRequestCount;
    }

    public int getHitCount() {
        return hitCount;
    }
}
