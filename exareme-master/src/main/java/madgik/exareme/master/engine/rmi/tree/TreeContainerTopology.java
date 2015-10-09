/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi.tree;

import madgik.exareme.master.queryProcessor.optimizer.ContainerResources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author panos
 */
public class TreeContainerTopology implements ContainerTopology {

    public static final int LEAF_LEVEL = 0;

    // todo: Replace HashMaps with bit search! 3:)
    private HashMap<Integer, ContainerTopologyNode> roots =
        new HashMap<Integer, ContainerTopologyNode>();
    private HashMap<Integer, ContainerTopologyNode> internals =
        new HashMap<Integer, ContainerTopologyNode>();
    private HashMap<Integer, ContainerTopologyNode> leaves =
        new HashMap<Integer, ContainerTopologyNode>();

    private int rootLevel = TreeContainerTopology.LEAF_LEVEL;
    private HashMap<Integer, ContainerTopologyNode> allNodes =
        new HashMap<Integer, ContainerTopologyNode>();

    public int addLeafContainer() {
        ContainerTopologyNode leafNode =
            new ContainerTopologyNode(TreeContainerTopology.LEAF_LEVEL);
        this.leaves.put(leafNode.getId(), leafNode);
        this.allNodes.put(leafNode.getId(), leafNode);

        return leafNode.getId();
    }

    public int addInternalContainer(List<Integer> children) {
    /* Container's level is greater by one, of his children's highest level */
        int maxLevel = this.getMaxLevelOfChildren(children);
        ContainerTopologyNode internalNode = new ContainerTopologyNode(maxLevel + 1);

        internalNode.addChildren(children);
        this.internals.put(internalNode.getId(), internalNode);
        this.allNodes.put(internalNode.getId(), internalNode);

        return internalNode.getId();
    }

    public int addRootContainer(List<Integer> children) {
    /* Container's level is greater by one, of his children's highest level */
        int maxLevel = this.getMaxLevelOfChildren(children);
        ContainerTopologyNode rootNode = new ContainerTopologyNode(maxLevel + 1);
        rootNode.addChildren(children);
        this.roots.put(rootNode.getId(), rootNode);
        this.allNodes.put(rootNode.getId(), rootNode);
        if (rootNode.getLevel() > this.rootLevel) {
            rootLevel = rootNode.getLevel();
        }
        return rootNode.getId();
    }

    private int getMaxLevelOfChildren(List<Integer> children) {
        int maxLevel = TreeContainerTopology.LEAF_LEVEL;
        for (ContainerTopologyNode node : this.allNodes.values()) {
            if (children.contains(node.getId()) && node.getLevel() > maxLevel) {
                maxLevel = node.getLevel();
            }
        }
        return maxLevel;
    }

    @Override public int getContainerLevel(int containerId) {
        ContainerTopologyNode node = this.allNodes.get(containerId);
        if (node != null) {
            return node.getLevel();
        } else {
            return -1;
        }
    }

    @Override public List<Integer> getContainersAtLevel(int level) {
        List<Integer> containersAtLevel = new ArrayList<Integer>();
        for (ContainerTopologyNode node : this.allNodes.values()) {
            if (node.getLevel() == level) {
                containersAtLevel.add(node.getId());
            }
        }
        return containersAtLevel;
    }

    @Override public List<Integer> getContainersAtNextLevel(int level) {
        return this.getContainersAtLevel(level + 1);
    }

    @Override public Integer getContainerAtLevel(int level, int index) {
        List<Integer> containersAtLevel = new ArrayList<Integer>();
        for (ContainerTopologyNode node : this.allNodes.values()) {
            if (node.getLevel() == level) {
                containersAtLevel.add(node.getId());
            }
        }
        return containersAtLevel.get(index);
    }

    @Override public int getHeight() {
        return this.rootLevel + 1;
    }

    @Override public int getRootLevel() {
        return this.rootLevel;
    }

    @Override public List<Integer> getChildrenOf(int containerId) {
        ContainerTopologyNode node = this.allNodes.get(containerId);
        if (node != null) {
            return node.getChildren();
        }
        return null;
    }

    @Override public int getChildrenCountOf(int containerId) {
        ContainerTopologyNode node = this.allNodes.get(containerId);
        if (node != null) {
            return node.getChildren().size();
        }
        return 0;
    }

    @Override public List<Integer> getParentsOf(int containerId) {
        List<Integer> parents = new ArrayList<Integer>();
        for (ContainerTopologyNode node : this.allNodes.values()) {
            if (node.getChildren().contains(containerId)) {
                parents.add(node.getId());
            }
        }
        return parents;
    }

    @Override public ContainerResources getContainerResourcesOf(int containerId) {
        return this.allNodes.get(containerId).getContainerResources();
    }

    @Override public ArrayList<ContainerResources> getAllContainerResources() {
        ArrayList<ContainerResources> resources = new ArrayList<ContainerResources>();
        for (ContainerTopologyNode node : this.allNodes.values()) {
            resources.add(node.getContainerResources());
        }
        return resources;
    }

    @Override public Set<Integer> getAllContainers() {
        return this.allNodes.keySet();
    }

    @Override public String getVizString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("digraph g{");
        for (ContainerTopologyNode root : this.allNodes.values()) {
            stringBuilder.append(root.getVizString());
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}


class ContainerTopologyNode implements Serializable {

    protected static int containersCount = 0;

    private int id;
    private int level;
    private ContainerResources containerResources;
    private List<Integer> children = new ArrayList<Integer>();

    public ContainerTopologyNode(int rank) {
        this.id = ContainerTopologyNode.containersCount++;
        this.level = rank;
        this.containerResources = new ContainerResources(); // todo: fix me (?)
    }

    public int getId() {
        return this.id;
    }

    public ContainerResources getContainerResources() {
        return this.containerResources;
    }

    public void addChildren(List<Integer> children) {
        this.children.addAll(children);
    }

    public int getLevel() {
        return this.level;
    }

    public List<Integer> getChildren() {
        return this.children;
    }

    public String getVizString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer child : this.children) {
            stringBuilder.append(" " + this.id + "->" + child + " ");
        }
        return stringBuilder.toString();
    }
}
