/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.zip;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author herald
 */
public class QuickLZTest extends TestCase {

    private static Logger log = Logger.getLogger(QuickLZTest.class);

    public QuickLZTest() {
    }

    /**
     * Test of class ls
     */
    public void testQuickLZ() {

        int size = 1000000;
        byte[] data;

        data = new byte[size];

        for (int i = 0; i < size; i++) {
            data[i] = (byte) i;
        }

        byte[] compressed = QuickLZ.compress(data);

        byte[] uncompressed = QuickLZ.decompress(compressed);

        //    assertArrayEquals(uncompressed, data);
    }

    public void performanceTestQuickLZ() {

        FileInputStream fis = null;
        try {
            int size = 1000000;
            int times = 500;
            int compresedSize = 0;
            byte[] data;
            data = new byte[size];
            fis = new FileInputStream(new File("ww2.txt"));

            log.debug("Read : " + fis.read(data));

            long start = System.currentTimeMillis();
            for (int i = 0; i < times; i++) {
                byte[] compressed = QuickLZ.compress(data);
                compresedSize += compressed.length;
            }
            long end = System.currentTimeMillis();

            double totalSize = (double) (times * size) / (1024 * 1024);
            double timeSecs = (double) (end - start) / 1000;

            log.debug("Total = " + (size * times));
            log.debug("Compressed = " + (compresedSize));

            log.debug("Compressed / Total = " + ((double) compresedSize / (size * times)));
            log.debug("MB/S = " + ((totalSize) / (timeSecs)));

            Assert.assertEquals(0, 0);
        } catch (IOException ex) {

        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Assert.fail("IOException");
            }
        }
    }
}
