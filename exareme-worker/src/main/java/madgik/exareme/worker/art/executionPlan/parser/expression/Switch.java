/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.util.LinkedList;
import java.util.Objects;

public class Switch {

    public String switchName;
    public String QoS;
    public String containerName;
    public LinkedList<Parameter> paramList;

    public Switch(String switchName, String QoS, String containerName,
                  LinkedList<Parameter> paramList) {
        this.switchName = switchName;
        this.containerName = containerName;
        this.QoS = QoS;
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

        Switch guest = (Switch) obj;
        //check strings
        if (!(switchName.equals(guest.switchName) && QoS.equals(guest.QoS) && containerName
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
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.switchName);
        hash = 61 * hash + Objects.hashCode(this.QoS);
        hash = 61 * hash + Objects.hashCode(this.containerName);
        hash = 61 * hash + Objects.hashCode(this.paramList);
        return hash;
    }
}
