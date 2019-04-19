/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.properties;

import java.util.Properties;

/**
 * @author herald
 */
public class GenericProperties {

    protected Properties properties = null;

    public GenericProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Returns a property as an integer.
     *
     * @param name The property name.
     * @return The property 'name' as integer.
     */
    public Integer getInt(String name) {
        String prop = properties.getProperty(name);
        if (prop == null) {
            return null;
        } else {
            return Integer.parseInt(prop);
        }
    }

    /**
     * Returns a property as a long.
     *
     * @param name The property name.
     * @return The property 'name' as long.
     */
    public Long getLong(String name) {
        String prop = properties.getProperty(name);
        if (prop == null) {
            return null;
        } else {
            return Long.parseLong(prop);
        }
    }

    /**
     * Returns a property as a float.
     *
     * @param name The property name.
     * @return The property 'name' as float.
     */
    public Float getFloat(String name) {
        String prop = properties.getProperty(name);
        if (prop == null) {
            return null;
        } else {
            return Float.parseFloat(prop);
        }
    }

    public Boolean getBoolean(String name) {
        String prop = properties.getProperty(name);
        if (prop == null) {
            return null;
        } else {
            return Boolean.parseBoolean(prop);
        }
    }

    /**
     * Returns a property as a string.
     *
     * @param name The property name.
     * @return The property 'name' as string.
     */
    public String getString(String name) {
        return properties.getProperty(name);
    }

    // Return a concatenation of the names
    public String getString(String... names) {
        StringBuilder sb = new StringBuilder();

        for (String name : names) {
            sb.append(properties.getProperty(name));
        }

        return sb.toString();
    }
}
