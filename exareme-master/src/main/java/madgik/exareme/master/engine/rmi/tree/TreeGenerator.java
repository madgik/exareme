package madgik.exareme.master.engine.rmi.tree;

import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.Link;
import madgik.exareme.master.queryProcessor.graph.LinkData;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.combinatorics.CartesianProduct;

import java.util.*;

/**
 * Created by panos on 5/14/14.
 */
public class TreeGenerator {
    // todo: fix me!
    public List<TreeConcreteQueryGraph> generateSupportedTreeQueryGraphs(
            ContainerTopology containerTopology, String adpQuery) {

        List<TreeConcreteQueryGraph> treeQueryGraphs = new ArrayList<TreeConcreteQueryGraph>();
        List<TreeQueryGraphFingerprint> allTreeFingerprints =
                this.generateDecliningFingerprints(containerTopology);

        for (int i = allTreeFingerprints.size() - 1; i >= 0; i--) {
            TreeQueryGraphFingerprint fingerprint = allTreeFingerprints.get(i);
            if (this.fingerprintIsSupportedByTopology(fingerprint, containerTopology)) {
                treeQueryGraphs.add(
                        this.generateTreeQueryGraphFromFingerprint(containerTopology, fingerprint));
            }
        }
        return treeQueryGraphs;
    }

    private List<TreeQueryGraphFingerprint> generateDecliningFingerprints(
            ContainerTopology containerTopology) {
        List<TreeQueryGraphFingerprint> fingerprints = new ArrayList<TreeQueryGraphFingerprint>();
        int leaves =
                containerTopology.getContainersAtLevel(TreeContainerTopology.LEAF_LEVEL).size();
        List<Integer> widestFingerprint = new ArrayList<Integer>();
        widestFingerprint.add(leaves);
        for (int i = 1; i < containerTopology.getRootLevel(); i++) {
            if (containerTopology.getContainersAtLevel(i).size() > widestFingerprint.get(i - 1)) {
                /* Greedy reduction :) */
                widestFingerprint.add(widestFingerprint.get(i - 1));
            } else {
                widestFingerprint.add(containerTopology.getContainersAtLevel(i).size());
            }
        }

        widestFingerprint.add(1);
        fingerprints.add(new TreeQueryGraphFingerprint(widestFingerprint));
        int reductionIndex = leaves - 2;

        for (int i = 0; i < fingerprints.size(); i++) {
            List<Integer> signature = fingerprints.get(i).getSignature();
            if (signature.get(reductionIndex) > signature.get(reductionIndex + 1)) {
                List<Integer> newSignature = new ArrayList<Integer>();
                newSignature.addAll(signature);
                int valueToDecrease = signature.get(reductionIndex);
                newSignature.remove(reductionIndex);
                newSignature.add(reductionIndex, valueToDecrease - 1);
                fingerprints.add(new TreeQueryGraphFingerprint(newSignature));
            }
            if (i == fingerprints.size() - 1 && reductionIndex > 1) {
                reductionIndex--;
                i = 0;
            }
        }
        return fingerprints;
    }

    private boolean fingerprintIsSupportedByTopology(TreeQueryGraphFingerprint fingerprint,
                                                     ContainerTopology containerTopology) {

        Stack<MappingState> states = new Stack<MappingState>();

        /* First state are all the leaves */
        MappingState firstState = new MappingState(new HashSet<Integer>(containerTopology.
                getContainersAtLevel(TreeContainerTopology.LEAF_LEVEL)),
                TreeContainerTopology.LEAF_LEVEL);
        states.push(firstState);

        while (states.peek().getLevel() != containerTopology.getRootLevel()) {
            MappingState state = states.pop();
            int level = state.getLevel();

      /* Generate all valid combinations of links with the next level and push them in the
         State stack */
            List<Pair<Integer, Integer>> allLinks = new ArrayList<Pair<Integer, Integer>>();
            for (Integer node : state.getLevelNodes()) {
                for (Integer parent : containerTopology.getParentsOf(node)) {
                    allLinks.add(new Pair<Integer, Integer>(node, parent));
                }
            }

            int[] indexes = new int[allLinks.size()];

            CartesianProduct cartesianProduct = new CartesianProduct(indexes);
            cartesianProduct.setLimitAll(2);

            boolean noValidCombinationFound = true;

            do {
                List<Pair<Integer, Integer>> combinations = new ArrayList<Pair<Integer, Integer>>();
                for (int i = 0; i < indexes.length; i++) {
                    if (indexes[i] == 1) {
                        combinations.add(allLinks.get(i));
                    }
                }

                if (combinations.size() < state.getLevelNodes().size()) {
                    /* This link combination cannot be valid, skip it! */
                    continue;
                }

                /* Check if links connect with all nodes of previous level, once */
                HashMap<Integer, Boolean> parents = new HashMap<Integer, Boolean>();
                HashMap<Integer, Boolean> nodeConnected = new HashMap<Integer, Boolean>();

                boolean validTree = true;

                for (Pair<Integer, Integer> link : combinations) {
                    if (nodeConnected.get(link.getA()) != null) {
                        /* Level node is connected more than once, not a valid tree */
                        validTree = false;
                        break;
                    } else {
                        nodeConnected.put(link.getA(), true);
                    }

                    if (parents.get(link.getB()) == null) {
                        parents.put(link.getB(), true);
                    }
                }

                if (validTree && nodeConnected.keySet().size() == state.getLevelNodes().size()
                        && parents.keySet().size() == fingerprint.getSignature().get(level + 1)) {

                    MappingState newState = new MappingState(parents.keySet(), level + 1);
                    newState.addLinks(state.getLinks());
                    newState.addLinks(combinations);
                    newState.addNodes(state.getAllNodes());

                    states.push(newState);

                    noValidCombinationFound = false;
                }
            } while (cartesianProduct.next());
            if (noValidCombinationFound) {
                if (states.size() <= 1) {
                    return false;
                }
                states.pop();
            }
        }
        fingerprint.setLinks(states.peek().getLinks());
        fingerprint.setNodes(states.peek().getAllNodes());
        return true;
    }

    private TreeConcreteQueryGraph generateTreeQueryGraphFromFingerprint(ContainerTopology topology,
                                                                         TreeQueryGraphFingerprint fingerprint) {
        TreeConcreteQueryGraph graph = new TreeConcreteQueryGraph();

        HashMap<Integer, List<Integer>> links = new HashMap<Integer, List<Integer>>();
        HashMap<Integer, ConcreteOperator> idToOperatorMap =
                new HashMap<Integer, ConcreteOperator>();

        for (Integer node : fingerprint.getNodes()) {
            //todo
            double cpuUtil = 1.0;
            int mem = 60;
            double runTime = 30.0;

            ConcreteOperator operator = new ConcreteOperator(node.toString(), runTime, cpuUtil, mem,
                    OperatorBehavior.store_and_forward);

            graph.addOperator(operator, topology.getContainerLevel(node));
            idToOperatorMap.put(node, operator);
        }

        for (Pair<Integer, Integer> link : fingerprint.getLinks()) {
            if (links.get(link.getA()) == null) {
                links.put(link.getA(), new ArrayList<Integer>());
            }
            links.get(link.getA()).add(link.getB());
        }

        for (Integer node : links.keySet()) {
            for (Integer parent : links.get(node)) {
                ConcreteOperator from = graph.getOperator(idToOperatorMap.get(node).opID);
                ConcreteOperator to = graph.getOperator(idToOperatorMap.get(parent).opID);

                // todo
                double sizeMB = 100.0f;

                LinkData data = new LinkData(from.operatorName + "-" + to.operatorName, sizeMB);
                Link link = new Link(from, to, data);
                data.setFromOpID(from.opID);
                data.setToOpID(to.opID);
                graph.addLinkInTree(link);
            }
        }

        /* Name the operators */
        for (ConcreteOperator operator : graph.getOperators()) {
            if (graph.getLevelOf(operator.opID) == graph.getRootLevel()) {
                operator.operatorName = "root";
            } else if (graph.getLevelOf(operator.opID) == 0) {
                operator.operatorName = "leaf" + operator.operatorName;
            } else {
                operator.operatorName = "internal" + operator.operatorName;
            }
        }

        return graph;
    }

    /* Mapping State class is used to capture the states
     * in the CSP of mapping a signature on the topology.
     */
    public class MappingState {

        private int level;
        private Set<Integer> levelNodes;
        private Set<Integer> allNodes = new HashSet<Integer>();
        private List<Pair<Integer, Integer>> links = new ArrayList<Pair<Integer, Integer>>();

        public MappingState(Set<Integer> levelNodes, int level) {

            this.levelNodes = levelNodes;
            this.allNodes.addAll(levelNodes);
            this.level = level;
        }

        public int getLevel() {
            return this.level;
        }

        public List<Pair<Integer, Integer>> getLinks() {
            return this.links;
        }

        public Set<Integer> getAllNodes() {
            return this.allNodes;
        }

        public Set<Integer> getLevelNodes() {
            return this.levelNodes;
        }

        public void addNodes(Set<Integer> nodes) {
            this.allNodes.addAll(nodes);
        }

        public void addLinks(List<Pair<Integer, Integer>> newLinks) {
            this.links.addAll(newLinks);
        }
    }


    /* Helper class for creating a Tree Query Graph that
     * is supported from the given topology.
     */
    class TreeQueryGraphFingerprint {

        private List<Integer> signature;
        private Set<Integer> nodes;
        private List<Pair<Integer, Integer>> links;

        public TreeQueryGraphFingerprint(List<Integer> signature) {
            this.signature = signature;
        }

        public List<Integer> getSignature() {
            return this.signature;
        }

        public List<Pair<Integer, Integer>> getLinks() {
            return this.links;
        }

        public void setLinks(List<Pair<Integer, Integer>> links) {
            this.links = links;
        }

        public Set<Integer> getNodes() {
            return this.nodes;
        }

        public void setNodes(Set<Integer> nodes) {
            this.nodes = nodes;
        }
    }
}
