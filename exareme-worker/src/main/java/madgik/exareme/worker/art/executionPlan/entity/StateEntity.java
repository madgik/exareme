/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.io.Serializable;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * @author herald
 */
public class StateEntity implements Comparable<StateEntity>, Serializable {

    private static final long serialVersionUID = 1L;
    public String containerName = null;
    public EntityName container = null;
    public String stateName = null;
    public String state = null;
    public LinkedList<Parameter> paramList;
    public String queryString;
    public List<URL> locations = null;

    public StateEntity(String stateName, String state, LinkedList<Parameter> paramList,
                       String queryString, List<URL> locations, String containerName, EntityName container) {
        this.containerName = containerName;
        this.container = container;
        this.stateName = stateName;
        this.state = state;

        if (paramList != null) {
            this.paramList = paramList;
        } else {
            this.paramList = new LinkedList<Parameter>();
        }

        if (queryString != null) {
            this.queryString = new String(queryString.toCharArray());
        } else {
            this.queryString = new String();
        }

        if (locations != null) {
            this.locations = locations;
        } else {
            this.locations = new LinkedList<URL>();
        }
    }

    public int compareTo(StateEntity entity) {
        return entity.stateName.compareTo(stateName);
    }

    @Override
    public boolean equals(Object entityObj) {
        StateEntity entity = (StateEntity) entityObj;
        return entity.stateName.equals(stateName);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.stateName != null ? this.stateName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return stateName + "(" + containerName + ")";
    }
}
