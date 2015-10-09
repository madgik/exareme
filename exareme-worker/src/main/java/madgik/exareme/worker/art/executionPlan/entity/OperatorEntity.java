/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * operator opName contName('Operator', paramList);
 *
 * @author Herald Kllapi <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class OperatorEntity implements Comparable<OperatorEntity>, Serializable {

    public static final String BEHAVIOR_PARAM = "behavior";
    public static final String TYPE_PARAM = "type";
    public static final String CATEGORY_PARAM = "category";
    public static final String MEMORY_PARAM = "memoryPercentage";
    public static final String FROM_CONTAINER_IP_PARAM = "fromContainerName";
    public static final String TO_CONTAINER_IP_PARAM = "toContainerName";
    public static final String FROM_CONTAINER_PORT_PARAM = "fromContainerPort";
    public static final String TO_CONTAINER_PORT_PARAM = "toContainerPort";
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(OperatorEntity.class);
    public final String containerName;
    public final EntityName container;
    public final String operatorName;
    public final String operator;
    public final String queryString;
    public final LinkedList<Parameter> paramList;
    public final Map<String, LinkedList<Parameter>> linksparams;
    public final List<URL> locations;
    public OperatorBehavior behavior = null;
    public OperatorType type = null;
    public String category = null;
    public HashMap<String, Parameters> params;
    /* Space shared constraints */
    public double memory = 0.0;

    public OperatorEntity(String operatorName, String operator, LinkedList<Parameter> paramList,
        String queryString, List<URL> locations, String containerName, EntityName container,
        Map<String, LinkedList<Parameter>> linksparams) {
        this.containerName = containerName;
        this.container = container;
        this.operatorName = operatorName;
        this.operator = operator;
        if (linksparams == null) {
            this.linksparams = new HashMap<>();
        } else {
            this.linksparams = linksparams;
        }
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
        this.locations = (locations == null) ? new LinkedList<URL>() : locations;
        if (paramList != null) {
            for (Parameter param : paramList) {
                if (param.name.equals(BEHAVIOR_PARAM)) {
                    behavior = OperatorBehavior.valueOf(param.value);
                    continue;
                }
                if (param.name.equals(TYPE_PARAM)) {
                    type = OperatorType.valueOf(param.value);
                    continue;
                }
                if (param.name.equals(MEMORY_PARAM)) {

                    memory = Double.parseDouble(param.value);
                    continue;
                }
                if (param.name.equals(CATEGORY_PARAM)) {
                    category = param.value;
                    continue;
                }
            }
        }
        // By default the behavior is SnF and the type is processing
        if (behavior == null) {
            behavior = OperatorBehavior.store_and_forward;
        }
        if (type == null) {
            type = OperatorType.processing;
        }
        params = null;
    }

    public void addLinkParam(String toOperator, LinkedList<Parameter> params) {
        log.trace("Adding link params: " + toOperator + " -> " + params);
        this.linksparams.put(toOperator, params);
    }

    public void setLinkParams(HashMap<String, Parameters> params) {

        this.params = params;
    }

    @Override public int compareTo(OperatorEntity entity) {
        return entity.operatorName.compareTo(operatorName);
    }

    @Override public boolean equals(Object entityObj) {
        if (entityObj instanceof OperatorEntity) {
            OperatorEntity entity = (OperatorEntity) entityObj;
            return entity.operatorName.equals(operatorName);
        }
        throw new ClassCastException("Cannot cast to ConcreteOperatorEntity");
    }

    @Override public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.operatorName != null ? this.operatorName.hashCode() : 0);
        return hash;
    }

    @Override public String toString() {
        return operatorName + "(" + containerName + ")";
    }

    public void clearLinkMap() {
        linksparams.clear();
    }
}
