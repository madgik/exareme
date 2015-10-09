/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.properties;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author herald
 * @author alex
 */
public class PropertiesFactory {

    private static Logger log = Logger.getLogger(PropertiesFactory.class);

    public static GenericProperties loadProperties(String propertiesName) throws Exception {
        Properties properties = new Properties();
        try {
            log.debug("Loading default properties (" + propertiesName + ") ...");
            ResourceBundle rb = ResourceBundle.getBundle(propertiesName);
            Enumeration<String> keys = rb.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                properties.setProperty(key, rb.getObject(key).toString());
            }
            String sitePath =
                System.getenv("EXAREME_HOME") + "/etc/exareme/" + propertiesName + ".properties";
            File sitePropFile = new File(sitePath);

            if (sitePropFile.exists()) {
                log.debug("Loading site properties.");
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(sitePropFile);
                    properties.load(inputStream);
                } finally {
                    if (inputStream != null)
                        inputStream.close();
                }
            }
        } catch (Exception e) {
            log.warn("Unable to load  properties (" + propertiesName + ").");
        }

        return new GenericProperties(properties);
    }

    public static MutableProperties loadMutableProperties(String propertiesName) throws Exception {

        Properties properties = new Properties();
        try {
            log.debug("Loading default properties (" + propertiesName + ") ...");
            ResourceBundle rb = ResourceBundle.getBundle(propertiesName);
            Enumeration<String> keys = rb.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                properties.setProperty(key, rb.getObject(key).toString());
            }


            String sitePath =
                System.getenv("EXAREME_HOME") + "/etc/exareme/" + propertiesName + ".properties";
            File sitePropFile = new File(sitePath);

            if (sitePropFile.exists()) {
                log.debug("Loading site properties.");
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(sitePropFile);
                    properties.load(inputStream);
                } finally {
                    if (inputStream != null)
                        inputStream.close();
                }
            }
        } catch (Exception e) {
            log.warn("Unable to load  properties (" + propertiesName + ").");
        }
        return new MutableProperties(properties);
    }
}
