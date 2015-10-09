/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.util.LinkedList;
import java.util.Objects;

public class SwitchLink {

    public String containerName;
    public String from;
    public String to;
    public LinkedList<Parameter> paramList;

    public SwitchLink(String from, String to, String containerName,
        LinkedList<Parameter> paramList) {
        this.from = from;
        this.to = to;
        this.containerName = containerName;
        this.paramList = paramList;
    }

    @Override public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        SwitchLink guest = (SwitchLink) obj;
        //check strings
        if (!(from.equals(guest.from) && to.equals(guest.to) && containerName
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

    @Override public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.containerName);
        hash = 83 * hash + Objects.hashCode(this.from);
        hash = 83 * hash + Objects.hashCode(this.to);
        hash = 83 * hash + Objects.hashCode(this.paramList);
        return hash;
    }
}
