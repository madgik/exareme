/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.io.Serializable;
import java.util.Objects;

/**
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class Container implements Serializable {

    public String name;
    public String ip;
    public int port;
    public int dataTransferPort;

    public Container(String containerName, String ip, int port, int dataTransferPort) {
        this.name = containerName;
        this.ip = ip;
        this.port = port;
        this.dataTransferPort = dataTransferPort;
    }

    public int getDataTransferPort() {
        return dataTransferPort;
    }

    @Override public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Container guest = (Container) obj;
        return name.equals(guest.name) && ip.equals(guest.ip) && port == guest.port;
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + Objects.hashCode(this.ip);
        hash = 89 * hash + this.port;
        return hash;
    }


}
