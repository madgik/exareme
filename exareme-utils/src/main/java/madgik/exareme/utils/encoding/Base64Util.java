/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.encoding;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author herald
 */
public class Base64Util {

    private static Logger log = Logger.getLogger(Base64Util.class);

    private Base64Util() {
    }

    public static String simpleEncodeBase64(String str) throws IOException{
        return Base64.encodeBase64URLSafeString(str.getBytes(Charset.forName("UTF-8")));
    }

    public static String encodeBase64(Object object) throws IOException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        OutputStream os = ostream;
        ObjectOutputStream p;

        p = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(os)));

        p.writeObject(object);
        p.flush();
        p.close(); // used to be ostream.close()
        byte[] bytes = ostream.toByteArray();

        return Base64.encodeBase64URLSafeString(bytes);
    }

    public static <T> T decodeBase64(String base64) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.decodeBase64(base64);

        ByteArrayInputStream istream = new ByteArrayInputStream(bytes);
        ObjectInputStream p;
        Object toReturn = null;

        p = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(istream)));
        toReturn = p.readObject();

        istream.close();
        return (T) toReturn;
    }

}
