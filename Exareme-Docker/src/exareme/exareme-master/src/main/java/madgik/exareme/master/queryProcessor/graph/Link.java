/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import java.io.Serializable;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class Link implements Serializable {
    private static final long serialVersionUID = 1L;

    public int linkId = -1;
    public ConcreteOperator from = null;
    public ConcreteOperator to = null;
    public LinkData data = null;

    public Link(ConcreteOperator from, ConcreteOperator to, LinkData data) {
        this.from = from;
        this.to = to;
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        Link l = (Link) obj;
        return (this.linkId == l.linkId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.linkId;
        return hash;
    }
}
