/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * @author herald
 */
public class State {

    public String containerName = null;
    public String stateName = null;
    public String state = null;
    public LinkedList<Parameter> paramList;
    public String queryString;
    public List<URL> locations = null;

    public State(String stateName, String state, LinkedList<Parameter> paramList,
                 String queryString, String containerName) {
        this.stateName = stateName;
        this.containerName = containerName;
        this.state = state.substring(1, state.length() - 1);
        this.paramList = paramList;
        this.queryString = queryString;
        this.locations = null;
    }

    public State(String stateName, String state, LinkedList<Parameter> paramList,
                 String queryString, String containerName, List<URL> locations) {
        this.stateName = stateName;
        this.containerName = containerName;
        this.state = state.substring(1, state.length() - 1);
        this.paramList = paramList;
        this.queryString = queryString;
        this.locations = locations;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        State guest = (State) obj;
        //check strings
        if (!(stateName.equals(guest.stateName) && state.equals(guest.state) && queryString
                .equals(guest.queryString) && containerName.equals(guest.containerName))) {
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

        if ((locations != null && guest.locations == null) || (locations == null
                && guest.locations != null)) {
            return false;
        }

        if (locations != null && guest.locations != null) {
            if (!(locations.size() == guest.locations.size())) {
                return false;
            }
            for (URL url : locations) {
                if (!guest.locations.contains(url)) {
                    return false;
                }
            }
        }
        return true;
    }
}
