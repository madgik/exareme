package madgik.exareme.master.engine.executor;

import madgik.exareme.master.engine.executor.cache.Cache;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author heraldkllapi
 */
public class CacheTest {

    public CacheTest() {
    }

    @Test
    public void testFetch2() throws Exception {
        Cache cache = new Cache(1000);
        ExecutorService exec = Executors.newFixedThreadPool(1000);
        for (int i = 0; i < 1000; ++i) {
            exec.submit(new Client("/tmp", cache));
            //      Thread.sleep(500);
            exec.submit(new Client("/tmp2", cache));
            //      Thread.sleep(600);
            exec.submit(new Client("/tmp3", cache));
            //      Thread.sleep(600);
        }
        Thread.sleep(3000);
        for (int i = 0; i < 5; ++i) {
            exec.submit(new Client("/tmp1", cache));
            exec.submit(new Client("/tmp4", cache));
        }
        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.DAYS);
    }

    @Test
    public void testFetch() throws Exception {
        Cache cache = new Cache(1000);
        ExecutorService exec = Executors.newFixedThreadPool(1000);
        exec.submit(new Client("/tmp", cache));
        Thread.sleep(5000);

        for (int i = 0; i < 10; ++i) {
            exec.submit(new Client("/tmp", cache));
            Thread.sleep(500);
            exec.submit(new Client("/tmp2", cache));
        }
        //    Thread.sleep(5000);
        //    for (int i = 0; i < 5; ++i) {
        //      exec.submit(new Client("/tmp1", cache));
        //      exec.submit(new Client("/tmp4", cache));
        //    }
        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.DAYS);
    }
}


class Client extends Thread {
    String file;
    Cache cache;

    public Client(String file, Cache cache) {
        this.file = file;
        this.cache = cache;
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
//            System.out.println(Thread.currentThread().getId() + ": " +
//                Calendar.getInstance().get(Calendar.MINUTE) + ":" +
//                Calendar.getInstance().get(Calendar.SECOND) + " Fetching: " + file);
            cache.fetch(file);
            long end = System.currentTimeMillis();
//            System.out.println(Thread.currentThread().getId() + ": " +
//                Calendar.getInstance().get(Calendar.MINUTE) + ":" +
//                Calendar.getInstance().get(Calendar.SECOND) + " Done: " + file +
//                " in " + (end - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
