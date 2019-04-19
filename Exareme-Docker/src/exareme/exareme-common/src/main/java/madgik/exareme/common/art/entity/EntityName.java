/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.art.entity;

import java.io.Serializable;

/**
 * This class represents an end point reference.
 * <p/>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class EntityName implements Comparable<EntityName>, Serializable {

    private static final long serialVersionUID = 1L;
    private String name = null;
    private String ip = null;
    private int port = 0;
    private int dataTransferPort = 0;

    public EntityName(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public EntityName(String name, String ip, int port, int dataTransferPort) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.dataTransferPort = dataTransferPort;
    }

    public EntityName(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;

    }

    public String getName() {
        return name;
    }

    public String getIP() {
        return this.ip;
    }

    public int getDataTransferPort() {
        return dataTransferPort;
    }

    public void setDataTransferPort(int dataTransferPort) {
        this.dataTransferPort = dataTransferPort;
    }

    public int getPort() {
        return this.port;
    }

    @Override
    public boolean equals(Object name) {
        if (name instanceof EntityName == false) {
            throw new ClassCastException();
        }
        return this.compareTo((EntityName) name) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 41 * hash + (this.ip != null ? this.ip.hashCode() : 0);
        hash = 41 * hash + this.port;
        return hash;
    }

    @Override
    public int compareTo(EntityName name) {
        return (this.name + this.ip + this.port)
                .compareToIgnoreCase(name.name + name.ip + name.port);
    }

    @Override
    public String toString() {
        return name;
    }
}
