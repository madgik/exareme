/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.serialization;

import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * @author herald
 */
public class SerializationUtil {
    private static Logger log = Logger.getLogger(SerializationUtil.class);

    private SerializationUtil() {
        throw new RuntimeException("Cannot create instances of this class");
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepCopy(T object) {
        try {
            SerializedObject so = new SerializedObject(object);
            return (T) so.getObject();
        } catch (Exception e) {
            log.error("Cannot serialize object. Returning null.", e);
            return null;
        }
    }
}
