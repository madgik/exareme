/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.registry;

import madgik.exareme.common.art.entity.EntityName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * @author Dimitris Paparas<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ArtRegistryProxyCache implements Serializable {
    Map<EntityName, Registerable> cache;
    ArrayBlockingQueue<EntityName> lruQueue;
    Semaphore sem;

    public ArtRegistryProxyCache(int size) {
        sem = new Semaphore(1);

        cache = new HashMap<EntityName, Registerable>();
        lruQueue = new ArrayBlockingQueue<EntityName>(size);
    }

    public Registerable get(EntityName entityName) {

        Registerable r = null;

        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return null;
        }

        if (lruQueue.remove(entityName)) {
            lruQueue.offer(entityName);

            r = cache.get(entityName);
        }

        sem.release();

        return r;
    }

    public boolean put(Registerable r) {
        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }

        cache.put(r.getEntityName(), r);
        lruQueue.remove(r.getEntityName());

        if (!lruQueue.offer(r.getEntityName())) {
            lruQueue.poll();
            lruQueue.offer(r.getEntityName());
        }

        sem.release();

        return true;
    }

    public boolean delete(EntityName entityName) {

        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }

        cache.remove(entityName);

        boolean exists = lruQueue.remove(entityName);

        sem.release();

        return exists;
    }
}

