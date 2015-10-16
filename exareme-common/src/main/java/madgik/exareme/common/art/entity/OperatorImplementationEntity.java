/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art.entity;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class OperatorImplementationEntity implements Accessible {
    private static final long serialVersionUID = 1L;
    private String className = null;
    private List<URL> locations = null;

    public OperatorImplementationEntity(String name) {
        this.className = name;
    }

    public OperatorImplementationEntity(String name, URL... urls) {
        this.className = name;
        this.locations = new ArrayList<>(urls.length);
        this.locations.addAll(Arrays.asList(urls));
    }

    public OperatorImplementationEntity(String name, List<URL> locations) {
        this.className = name;
        this.locations = locations;
    }

    public String getClassName() {
        return className;
    }

    public List<URL> getLocations() {
        return locations;
    }

    @Override public EntityName getEntityName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
