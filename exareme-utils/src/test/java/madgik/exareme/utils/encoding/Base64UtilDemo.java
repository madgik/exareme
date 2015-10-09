/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.encoding;

import org.apache.log4j.Logger;

/**
 * @author herald
 */
public class Base64UtilDemo {

    private static Logger log = Logger.getLogger(Base64UtilDemo.class);

    private Base64UtilDemo() {
    }

    public static void main(String[] args) throws Exception {
        String str = new String("This is a test for Base64 encoding!");

        String encoded = Base64Util.encodeBase64(str);
        log.debug(encoded);

        String str2 = Base64Util.decodeBase64(encoded);
        log.debug(str2);
    }
}
