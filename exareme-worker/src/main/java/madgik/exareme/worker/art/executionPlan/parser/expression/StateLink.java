/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.util.LinkedList;
import java.util.Objects;

/**
 * @author herald
 */
public class StateLink {

    public String containerName;
    public String operatorName;
    public String stateName;
    public LinkedList<Parameter> paramList;

    public StateLink(String operatorName, String stateName, String containerName,
        LinkedList<Parameter> paramList) {
        this.containerName = containerName;
        this.operatorName = operatorName;
        this.stateName = stateName;
        this.paramList = paramList;
    }

    @Override public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        StateLink guest = (StateLink) obj;
        //check strings
        if (!(operatorName.equals(guest.operatorName) && stateName.equals(guest.stateName)
            && containerName.equals(guest.containerName))) {
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
        hash = 37 * hash + Objects.hashCode(this.containerName);
        hash = 37 * hash + Objects.hashCode(this.operatorName);
        hash = 37 * hash + Objects.hashCode(this.stateName);
        hash = 37 * hash + Objects.hashCode(this.paramList);
        return hash;
    }
}
