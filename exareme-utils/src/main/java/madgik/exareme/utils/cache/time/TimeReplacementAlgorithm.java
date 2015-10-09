/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.cache.time;

import madgik.exareme.utils.association.Triple;
import madgik.exareme.utils.cache.ReplacementAlgorithm;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * @author herald
 */
public class TimeReplacementAlgorithm implements ReplacementAlgorithm {

    private static final long serialVersionUID = 1L;
    private final HashMap<Long, Triple<Long, BigInteger, Boolean>> objectMap;
    private final PriorityQueue<Triple<Long, BigInteger, Boolean>> free;
    private final Comparator<Triple<Long, BigInteger, Boolean>> comparator;
    private BigInteger clock = BigInteger.ZERO;

    public TimeReplacementAlgorithm(Comparator<Triple<Long, BigInteger, Boolean>> comparator) {
        this.comparator = comparator;
        this.objectMap = new HashMap<Long, Triple<Long, BigInteger, Boolean>>();
        this.free = new PriorityQueue<Triple<Long, BigInteger, Boolean>>(10, this.comparator);
    }

    @Override public void insert(long objectNum) {
        clock = clock.add(BigInteger.ONE);
        Triple<Long, BigInteger, Boolean> obj = objectMap.get(objectNum);
        if (obj == null) {
            obj = new Triple<Long, BigInteger, Boolean>(objectNum, clock, false);
            objectMap.put(objectNum, obj);
        }
        obj.b = clock;
    }

    @Override public void delete(long objectNum) {
        Triple<Long, BigInteger, Boolean> obj = objectMap.get(objectNum);
        free.remove(obj);
    }

    @Override public long getNext() {
        if (free.isEmpty()) {
            return -1;
        }
        Triple<Long, BigInteger, Boolean> head = free.poll();
        objectMap.remove(head.a);
        if (head.c) {
            throw new IllegalArgumentException("Is pinned: " + head.a);
        }
        return head.a;
    }

    @Override public void pin(long objectNum) {
        clock = clock.add(BigInteger.ONE);
        Triple<Long, BigInteger, Boolean> obj = objectMap.get(objectNum);
        free.remove(obj);
        if (obj.c == false) {
            obj.c = true;
        } else {
            throw new IllegalArgumentException("Already pinned!");
        }
        obj.b = clock;
    }

    @Override public void unpin(long objectNum) {
        clock = clock.add(BigInteger.ONE);
        Triple<Long, BigInteger, Boolean> obj = objectMap.get(objectNum);
        obj.b = clock;
        if (obj.c == true) {
            obj.c = false;
        } else {
            throw new IllegalArgumentException("Already unpinned!");
        }
        free.offer(obj);
    }

    @Override public void clear() {
        objectMap.clear();
        free.clear();
    }

    @Override public String toString() {
        return free.toString() + ":" + objectMap.toString();
    }
}
