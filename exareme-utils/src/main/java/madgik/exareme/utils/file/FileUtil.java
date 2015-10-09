/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.file;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.units.Metrics;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class FileUtil {

    private static final int ioBufferSize =
        AdpProperties.getArtProps().getInt("art.container.ioBufferSize_kb") * Metrics.KB;
    private static Logger log = Logger.getLogger(FileUtil.class);

    private FileUtil() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    public static String readFile(URL url) throws IOException {
        StringBuilder sb = new StringBuilder(100);
        InputStreamReader in = null;
        BufferedReader input = null;

        try {
            in = new InputStreamReader(url.openStream());
            input = new BufferedReader(in);
            String line = null;
            while ((line = input.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            try {
                if (input != null)
                    input.close();
                if (in != null)
                    in.close();
            } catch (Exception e) {
                log.warn("Unable to close stream.", e);
            }
        }
        return sb.toString();
    }

    public static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder(100);
        InputStreamReader in = null;
        BufferedReader input = null;
        try {
            in = new InputStreamReader(new FileInputStream(file));
            input = new BufferedReader(in);
            String line;
            while ((line = input.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            try {
                if (input != null)
                    input.close();
                if (in != null)
                    in.close();
            } catch (Exception e) {
                log.warn("Unable to close stream.", e);
            }
        }
        return sb.toString();
    }

    public static List<String> realFileLines(File file) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            lines.add(line);
        }
        scanner.close();
        return lines;
    }

    public static void writeFile(String content, File file) throws IOException {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.append(content);
            fileWriter.flush();
        } finally {
            try {
                if (fileWriter != null)
                    fileWriter.close();
            } catch (Exception e) {
                log.warn("Unable to close stream.", e);
            }
        }
    }

    public static void writeObjectFile(Serializable object, File file) throws IOException {
        file.delete();
        ObjectOutputStream oos =
            new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

        oos.writeObject(object);
        oos.flush();
        oos.close();
    }

    public static Serializable readObjectFile(File file)
        throws IOException, ClassNotFoundException {
        ObjectInputStream ois =
            new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
        Object obj = ois.readObject();
        ois.close();
        return (Serializable) obj;
    }

    public static String consume(InputStream stream) throws IOException {
        return consume(stream, false);
    }

    public static String consume(InputStream stream, boolean print) throws IOException {
        StringBuilder sb = new StringBuilder(100);
        byte[] buffer = new byte[ioBufferSize];
        int length;
        while ((length = stream.read(buffer)) > 0) {
            for (int i = 0; i < length; i++) {
                sb.append((char) buffer[i]);
                if (print) {
                    System.err.print((char) buffer[i]);
                }
            }
        }
        stream.close();
        return sb.toString();
    }

    public static long consumeAndIgnore(InputStream stream) throws IOException {
        byte[] buffer = new byte[ioBufferSize];
        int length = 0;
        long totalLength = 0;
        while ((length = stream.read(buffer)) > 0) {
            totalLength += length;
        }
        stream.close();
        return totalLength;
    }

    //  public static void transferFromFile(
    //          WriteAdaptorWrapper adaptorProxy,
    //          ObjectInputStream inputStream) throws Exception {
    //    while (true) {
    //      RecordGroup rg = (RecordGroup) inputStream.readObject();
    //      adaptorProxy.write(rg);
    //    }
    //  }
    //
    //  public static void transferToFile(
    //          ReadAdaptorWrapper adaptorProxy,
    //          ObjectOutputStream outputStream) throws Exception {
    //    while (adaptorProxy.hasNext()) {
    //      outputStream.writeObject(adaptorProxy.readNext());
    //    }
    //  }

    public static void write(OutputStream out, String object) throws IOException {
        char[] cs = object.toCharArray();
        byte[] bs = new byte[cs.length];

        for (int i = 0; i < cs.length; i++) {
            bs[i] = (byte) cs[i];
        }

        out.write(bs);
    }

    private static void intToByteArray(int val, byte[] b) {
        b[3] = (byte) (val >>> 0);
        b[2] = (byte) (val >>> 8);
        b[1] = (byte) (val >>> 16);
        b[0] = (byte) (val >>> 24);
    }

    private static int byteArrayToInt(byte[] b) {
        return ((b[3] & 0xFF) << 0) + ((b[2] & 0xFF) << 8) + ((b[1] & 0xFF) << 16) + ((b[0]) << 24);
    }

    private static int readFully(byte[] buffer, int offset, int length, InputStream in)
        throws IOException {
        int b = 0;
        while (b < length) {
            b += in.read(buffer, offset + b, length - b);
        }
        return b;
    }

    public static void writeToStream(File file, OutputStream out) throws Exception {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            byte buffer[] = new byte[ioBufferSize];
            byte[] intVal = new byte[4];
            long totalBytes = 0;
            int bytes = in.read(buffer);
            while (bytes > 0) {
                intToByteArray(bytes, intVal);
                out.write(intVal, 0, 4);
                out.write(buffer, 0, bytes);
                totalBytes += bytes;
                bytes = in.read(buffer);
            }
            intToByteArray(-1, intVal);
            out.write(intVal, 0, 4);
            in.close();
            log.debug("Written file with (" + bytes + ") " + totalBytes + " bytes: " + file
                .getAbsolutePath());
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception e) {
                log.warn("Unable to close stream.", e);
            }
        }
    }

    public static void readFromStream(InputStream in, File file) throws Exception {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte buffer[] = new byte[ioBufferSize];
            byte[] intVal = new byte[4];
            readFully(intVal, 0, 4, in);
            int bytes = byteArrayToInt(intVal);
            long totalBytes = 0;
            while (bytes >= 0) {
                readFully(buffer, 0, bytes, in);
                out.write(buffer, 0, bytes);
                totalBytes += bytes;
                readFully(intVal, 0, 4, in);
                bytes = byteArrayToInt(intVal);
            }
            out.flush();
            log.debug("Read file with (" + bytes + ") " + totalBytes + " bytes: " + file
                .getAbsolutePath());
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                log.warn("Unable to close stream.", e);
            }
        }
    }

    public static void createIfNotExists(File file) throws Exception {
        if (file.exists() == false) {
            file.createNewFile();
        }
    }
}
