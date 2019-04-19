/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author herald
 */
public class SwitchLinkEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    public LinkedList<Parameter> paramList;

    public SwitchLinkEntity(LinkedList<Parameter> paramList) {
        this.paramList = paramList;
    }
}
