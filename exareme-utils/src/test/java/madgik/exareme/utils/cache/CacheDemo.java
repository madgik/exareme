/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.cache;

import madgik.exareme.utils.cache.time.MRUComparator;
import madgik.exareme.utils.cache.time.TimeReplacementAlgorithm;
import org.apache.log4j.Logger;

/**
 * @author herald
 */
public class CacheDemo {

    private static Logger log = Logger.getLogger(CacheDemo.class);

    public static void main(String[] args) {

        ReplacementAlgorithm alg = new TimeReplacementAlgorithm(new MRUComparator());

        for (int i = 0; i < 10; i++) {
            alg.insert(i);
        }

        for (int i = 9; i >= 0; i--) {
            alg.pin(i);
            alg.unpin(i);
        }

        long next = alg.getNext();
        while (next >= 0) {
            log.debug(next);
            next = alg.getNext();
        }
    }
}
