package madgik.exareme.master.engine.rmi.tree;

import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.ExportToDotty;
import madgik.exareme.master.queryProcessor.graph.Link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by panos on 5/14/14.
 */
public class TreeConcreteQueryGraph extends ConcreteQueryGraph {

    private HashMap<Integer, Integer> links = new HashMap<Integer, Integer>();
    private HashMap<Integer, List<Integer>> levels = new HashMap<Integer, List<Integer>>();

    private int rootLevel = 0;
    private HashMap<Integer, List<Integer>> childrenOf = new HashMap<Integer, List<Integer>>();

    public void addOperator(ConcreteOperator operator, int level) {
        this.addOperator(operator);
        if (this.levels.get(level) == null) {
            this.levels.put(level, new ArrayList<Integer>());
            if (level > this.rootLevel) {
                this.rootLevel = level;
            }
        }
        this.levels.get(level).add(operator.opID);
    }

    public void addLinkInTree(Link link) {
        this.links.put(link.from.opID, link.to.opID);
        this.addLink(link);
    }

    public List<Integer> getOperatorsAtLevel(int level) {
        return this.levels.get(level);
    }

    public List<Integer> getOperatorsAtNextLevel(int level) {
        return this.levels.get(level + 1);
    }

    public Integer getParentOf(int opId) {
        return links.get(opId);
    }

    public int getRootLevel() {
        return this.rootLevel;
    }

    public Integer getLevelOf(int opId) {
        for (Integer level : this.levels.keySet()) {
            for (Integer node : this.levels.get(level)) {
                if (node == opId) {
                    return level;
                }
            }
        }
        return -1;
    }

    public List<Integer> getChildrenOf(int opId) {

        if (this.childrenOf.get(opId) == null) {
            ArrayList<Integer> children = new ArrayList<Integer>();
            for (Integer child : this.links.keySet()) {
                if (this.links.get(child) == opId) {
                    children.add(child);
                }
            }
            this.childrenOf.put(opId, children);
        }
        return this.childrenOf.get(opId);
    }

    public int getChildrenCountOf(int opId) {
        if (this.childrenOf.get(opId) == null) {
            ArrayList<Integer> children = new ArrayList<Integer>();
            for (Integer child : this.links.keySet()) {
                if (this.links.get(child) == opId) {
                    children.add(child);
                }
            }
            this.childrenOf.put(opId, children);
        }
        return this.childrenOf.get(opId).size();
    }

    public String getVizString() {
        return ExportToDotty.exportToDotty(this);
    }
}
