/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.zip;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.units.Metrics;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ZipUtil {

    private static final int ioBufferSize =
            AdpProperties.getArtProps().getInt("art.container.ioBufferSize_kb") * Metrics.KB;
    private static Logger log = Logger.getLogger(ZipUtil.class);

    private ZipUtil() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static void extract(URL url, String destinationDir) throws Exception {
        ZipInputStream zipInputStream = null;
        ZipEntry zipentry;
        zipInputStream = new ZipInputStream(url.openStream());
        zipentry = zipInputStream.getNextEntry();
        while (zipentry != null) {
            if (zipentry.isDirectory()) {
                // Assume directories are stored parents first then children.
                log.trace("Extracting directory: " + zipentry.getName());
                // This is not robust, just for demonstration purposes.
                new File(destinationDir + zipentry.getName()).mkdirs();
            } else {
                log.trace("Extracting file: " + zipentry.getName());
                copyInputStream(zipInputStream, new BufferedOutputStream(
                        new FileOutputStream(destinationDir + zipentry.getName())));
            }
            zipInputStream.closeEntry();
            zipentry = zipInputStream.getNextEntry();
        }

        zipInputStream.close();
    }

    private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[ioBufferSize];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        out.close();
    }
}
