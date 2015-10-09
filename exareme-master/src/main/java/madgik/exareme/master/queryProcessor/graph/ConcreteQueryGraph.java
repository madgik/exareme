/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.utils.iterator.SkipNullIterable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * @author Herald Kllapi <br>
 * @since 1.0
 */
public class ConcreteQueryGraph implements Serializable {

    private static final long serialVersionUID = 1L;
    // Operators
    private int totalOpCount = 0;
    private int opCount = 0;
    private ArrayList<ConcreteOperator> operators = null;
    private SkipNullIterable<ConcreteOperator> snOperators = null;
    // Links
    private int totalLinkCount = 0;
    private int linkCount = 0;
    private ArrayList<Link> links = null;
    private SkipNullIterable<Link> snLinks = null;

    public ConcreteQueryGraph() {
        operators = new ArrayList<>();
        snOperators = new SkipNullIterable<>(operators);
        links = new ArrayList<>();
        snLinks = new SkipNullIterable<>(links);
    }

    public void addOperator(ConcreteOperator op) {
        if (op.opID < 0) {
            op.opID = totalOpCount;
            totalOpCount++;
            if (operators.size() >= totalOpCount) {
                operators.set(op.opID, op);
            } else {
                operators.add(op);
            }
        } else {
            operators.set(op.opID, op);
        }
        opCount++;
    }

    public void removeOperator(ConcreteOperator op) {
        if (operators.set(op.opID, null) != null) {
            opCount--;
        }
    }

    public void addLink(Link link) {
        if (link.linkId < 0) {
            link.linkId = totalLinkCount;
            totalLinkCount++;
            if (links.size() >= totalLinkCount) {
                links.set(link.linkId, link);
            } else {
                links.add(link);
            }
        } else {
            links.set(link.linkId, link);
        }
        link.from.outputLinks.add(link);
        link.to.inputLinks.add(link);
        linkCount++;
    }

    public void removeLink(Link link) {
        links.set(link.linkId, null);
        link.from.outputLinks.remove(link);
        link.to.inputLinks.remove(link);
        linkCount--;
    }

    public Collection<Link> getOutputLinks(int opID) {
        LinkedHashSet<Link> outputCo = operators.get(opID).outputLinks;
        return outputCo;
    }

    public Collection<Link> getInputLinks(int opID) {
        LinkedHashSet<Link> intputCo = operators.get(opID).inputLinks;
        return intputCo;
    }

    public Iterable<ConcreteOperator> getOperators() {
        return snOperators;
    }

    public Iterable<ConcreteOperator> getLeafOperators() {
        // TODO: Poor performance for many operators
        // addOperator all operators that have no inputs
        LinkedList<ConcreteOperator> leafOps = new LinkedList<>();
        for (ConcreteOperator op : snOperators) {
            if (getInputLinks(op.opID).isEmpty()) {
                leafOps.add(op);
            }
        }
        return leafOps;
    }

    public ConcreteOperator getOperator(int opID) {
        return operators.get(opID);
    }

    public boolean hasOperator(int opID) {
        if (opID >= totalOpCount) {
            return false;
        }
        return (operators.get(opID) != null);
    }

    public int getNumOfOperators() {
        return opCount;
    }

    public int getMaxOpId() {
        return totalOpCount;
    }

    public Iterable<Link> getLinks() {
        return snLinks;
    }

    public int getNumOfLinks() {
        return linkCount;
    }
}
