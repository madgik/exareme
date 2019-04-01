/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art.entity;

/**
 * This class represents a datasource entity. A datasource can be a database or Google!
 *
 * @author Herald Kllapi
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.2
 */
public class DataSourceEntity implements Accessible {

    private static final long serialVersionUID = 1L;
    private EntityName epr = null;

    /**
     * Constructs a datasource specifying the ip and the port number.
     *
     * @param publicIP the ip of the datasource
     * @param port     the port that the datasource is listening to.
     */
    public DataSourceEntity(String publicIP, int port) {
        epr = new EntityName(publicIP + ":" + port, publicIP, port);
    }

    public EntityName getEntityName() {
        return epr;
    }
}
