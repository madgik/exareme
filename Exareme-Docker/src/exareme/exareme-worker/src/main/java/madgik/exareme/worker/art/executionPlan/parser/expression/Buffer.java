/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;

/**
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class Buffer implements Serializable {

    private static final long serialVersionUID = 1L;

    public String bufferName;
    public String QoS;
    public String containerName;
    public LinkedList<Parameter> paramList;

    public Buffer(String bufferName, String QoS, String containerName,
                  LinkedList<Parameter> paramList) {
        this.bufferName = bufferName;
        this.QoS = QoS;
        this.containerName = containerName;
        this.paramList = paramList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Buffer guest = (Buffer) obj;
        //check strings
        if (!(bufferName.equals(guest.bufferName) && QoS.equals(guest.QoS) && containerName
                .equals(guest.containerName))) {
            return false;
        }
        //check lists
        if ((paramList != null && guest.paramList == null) || (paramList == null
                && guest.paramList != null)) {
            return false;
        }

        if (paramList != null && guest.paramList != null) {
            if (!(paramList.size() == guest.paramList.size())) {
                return false;
            }
            for (Parameter param : paramList) {
                if (!guest.paramList.contains(param)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.bufferName);
        hash = 29 * hash + Objects.hashCode(this.QoS);
        hash = 29 * hash + Objects.hashCode(this.containerName);
        hash = 29 * hash + Objects.hashCode(this.paramList);
        return hash;
    }
}
