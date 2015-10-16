/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.cache;

import madgik.exareme.utils.cache.ReplacementAlgorithm;
import madgik.exareme.utils.cache.time.LRUComparator;
import madgik.exareme.utils.cache.time.TimeReplacementAlgorithm;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * @author heraldkllapi
 */
public class CacheReplacementAlgorithm {
    private static final Logger log = Logger.getLogger(CacheReplacementAlgorithm.class);
    private final HashMap<Long, String> idToNameMap = new HashMap<>();
    private final HashMap<String, Long> nameToIdMap = new HashMap<>();
    // Use LRU replacement policy for the cache
    private final ReplacementAlgorithm lru = new TimeReplacementAlgorithm(new LRUComparator());
    private long objectIdCount = 0;

    public CacheReplacementAlgorithm() {
    }

    public void touch(String id) {
        if (nameToIdMap.containsKey(id) == false) {
            return;
        }
        long objectId = nameToIdMap.get(id);
        // TODO(herald): fix the following!
        lru.pin(objectId);
        lru.unpin(objectId);
    }

    public void clear() {
        idToNameMap.clear();
        nameToIdMap.clear();
        lru.clear();
    }

    public void remove(String id) {
        if (nameToIdMap.containsKey(id) == false) {
            return;
        }
        long objectId = nameToIdMap.remove(id);
        idToNameMap.remove(objectId);
        lru.delete(objectId);
    }

    public void addToCache(String id) {
        log.debug("Added: " + id);
        long objectId = objectIdCount;
        objectIdCount++;

        idToNameMap.put(objectId, id);
        nameToIdMap.put(id, objectId);
        lru.insert(objectId);
        lru.pin(objectId);
        lru.unpin(objectId);
    }

    public String removeLRUFromCache() {
        long objectId = lru.getNext();
        if (objectId < 0) {
            throw new RuntimeException("LRU is empty");
        }
        String nameToRemove = idToNameMap.remove(objectId);
        nameToIdMap.remove(nameToRemove);
        return nameToRemove;
    }

    public int getNumObjects() {
        return idToNameMap.size();
    }
}
