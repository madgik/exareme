/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.zip;

import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.utils.units.Metrics;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * @author Herald Kllapi <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ZipDemo {
    private static final Logger log = Logger.getLogger(ZipDemo.class);

    public static void main(String[] args) throws Exception {
        int times = 1000000;
        int ioBufferSize = 512 * Metrics.KB;

        String dataString = FileUtil.readFile(new File("/tmp/data4.txt"));
        byte[] data = dataString.getBytes("UTF-8");

        FileOutputStream ostream = new FileOutputStream("/tmp/test.data");
        OutputStream os = ostream;

        DeflaterOutputStream zos =
                new DeflaterOutputStream(new BufferedOutputStream(os), new Deflater(2), ioBufferSize);

        long start = System.nanoTime();
        for (int i = 0; i < times; i++) {
            zos.write(data);
            if (i % 50000 == 0) {
                long end = System.currentTimeMillis();
                double mbs = 50000 * ((double) data.length / (1024.0 * 1024.0));
                log.debug(((i + 1) * ((double) data.length / (1024.0 * 1024.0))) + " MB");
                log.debug((mbs / ((double) (end - start) / 1000.0)) + " MB/s");
                start = System.currentTimeMillis();
                log.debug("");
            }
        }
        zos.close();
    }
}
