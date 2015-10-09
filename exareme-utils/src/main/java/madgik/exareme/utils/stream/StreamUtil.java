/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.stream;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.units.Metrics;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author herald
 */
public class StreamUtil {
    private static final int ioBufferSize =
        AdpProperties.getArtProps().getInt("art.container.ioBufferSize_kb") * Metrics.KB;
    private static final int zipBufferSize =
        AdpProperties.getArtProps().getInt("art.container.zipBufferSize_kb") * Metrics.KB;
    private static final int ZIPLEVEL =
        AdpProperties.getArtProps().getInt("art.container.zipLevel");

    private StreamUtil() {
    }

    public static OutputStream createZippedOutputStream(OutputStream out) throws IOException {
        return new LZ4BlockOutputStream(out, ioBufferSize,
            LZ4Factory.fastestInstance().fastCompressor(), new NoChecksum(), false);
    }

    public static InputStream createZippedInputStream(InputStream in) throws IOException {
        return new LZ4BlockInputStream(in, LZ4Factory.fastestInstance().fastDecompressor(),
            new NoChecksum());
    }

    public static OutputStream createDeflaterOutputStream(OutputStream out) throws IOException {
        return new DeflaterOutputStream(new BufferedOutputStream(out, ioBufferSize),
            new Deflater(ZIPLEVEL), zipBufferSize);
    }

    public static InputStream createInflaterInputStream(InputStream in) throws IOException {
        return new InflaterInputStream(new BufferedInputStream(in, ioBufferSize), new Inflater(),
            zipBufferSize);
    }

    public static void copyStreams(InputStream from, OutputStream to) throws IOException {
        byte[] buffer = new byte[ioBufferSize];
        int actual = from.read(buffer);
        while (actual >= 0) {
            to.write(buffer, 0, actual);
            actual = from.read(buffer);
        }
    }
}
