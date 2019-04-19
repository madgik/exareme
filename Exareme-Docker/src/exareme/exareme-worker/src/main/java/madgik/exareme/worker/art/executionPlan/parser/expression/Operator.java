/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @since 1.0
 */
public class Operator implements Serializable {

    private static final long serialVersionUID = 1L;
    public final String operatorName;
    public final String operator;
    public final LinkedList<Parameter> paramList;
    public final String queryString;
    public final List<URL> locations;
    public final String containerName;
    public final Map<String, LinkedList<Parameter>> linksparams;

    public Operator(String operatorName, String operator, LinkedList<Parameter> paramList,
                    String queryString, String containerName, Map<String, LinkedList<Parameter>> linksparams) {
        this.operatorName = operatorName;
        this.containerName = containerName;
        this.operator = operator;
        this.paramList = paramList;
        this.queryString = queryString;
        this.locations = null;
        if (linksparams == null) {
            this.linksparams = new HashMap<>();
        } else {
            this.linksparams = linksparams;
        }
    }

    public Operator(String operatorName, String operator, LinkedList<Parameter> paramList,
                    String queryString, String containerName, Map<String, LinkedList<Parameter>> linksparams,
                    List<URL> locations) {
        this.operatorName = operatorName;
        this.containerName = containerName;
        this.operator = operator;
        this.paramList = paramList;
        this.queryString = queryString;
        this.locations = locations;
        if (linksparams == null) {
            this.linksparams = new HashMap<>();
        } else {
            this.linksparams = linksparams;
        }
    }


    public void addLinkParam(String toOperator, LinkedList<Parameter> params) {
        this.linksparams.put(toOperator, params);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Operator guest = (Operator) obj;
        //check strings

        if ((queryString != null && guest.queryString == null) || (queryString == null
                && guest.queryString != null)) {
            return false;
        } else if (queryString != null) {
            if (!queryString.equals(guest.queryString)) {
                return false;
            }
        }

        if (!(operatorName.equals(guest.operatorName) && operator.equals(guest.operator)
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.operatorName);
        hash = 41 * hash + Objects.hashCode(this.operator);
        hash = 41 * hash + Objects.hashCode(this.paramList);
        hash = 41 * hash + Objects.hashCode(this.queryString);
        hash = 41 * hash + Objects.hashCode(this.locations);
        hash = 41 * hash + Objects.hashCode(this.containerName);
        return hash;
    }

    @Override
    public String toString() {
        return "Operator{" + "\noperatorName=" + operatorName + ", \noperator=" + operator
                + ", \nparamList=" + paramList + ", \ncontainerName=" + containerName
                + ", \nlinksparams=" + linksparams + '}';
    }

}
