package madgik.exareme.utils.properties;

import java.util.Properties;

/**
 * @author konikos
 */
public class MutableProperties extends GenericProperties {

    public MutableProperties(Properties properties) {
        super(properties);
    }

    public void setStringProperty(String key, String value) {
        this.properties.setProperty(key, value);
    }

    public void setIntProperty(String key, int value) {
        this.properties.setProperty(key, Integer.toString(value));
    }

    public void setLongProperty(String key, long value) {
        this.properties.setProperty(key, Long.toString(value));
    }

    public void setFloatProperty(String key, float value) {
        this.properties.setProperty(key, Float.toString(value));
    }

    public void setBooleanProperty(String key, boolean value) {
        this.properties.setProperty(key, Boolean.toString(value));
    }
}
