/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art.entity;


/**
 * This class represents an operator entity.
 * Every operator instance in order to be used
 * must exist in the registry.
 * <p/>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.2
 */
public class OperatorEntity implements Accessible {
    private static final long serialVersionUID = 1L;

    private EntityName epr = null;

    /**
     * Constructs a new operator entity specifying it's public ip and port.
     *
     * @param publicIP the public ip.
     * @param port     the port number.
     */
    public OperatorEntity(String publicIP, int port) {
        epr = new EntityName(publicIP + ":" + port, publicIP, port);
    }

    @Override
    public EntityName getEntityName() {
        return epr;
    }
}
