/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator;

import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.Link;

import java.util.BitSet;

/**
 * @author herald
 */
public class OperatorDependencySolver {
    private ConcreteQueryGraph queryGraph = null;
    private BitSet readyOperators = null;
    private BitSet terminatedOperators = null;

    public OperatorDependencySolver(ConcreteQueryGraph graph) {
        this.queryGraph = graph;
        this.readyOperators = new BitSet(graph.getMaxOpId());
        this.terminatedOperators = new BitSet(graph.getMaxOpId());
    }

    public BitSet getInitial() {
        BitSet activated = getLeafs();
        BitSet activatedMap = new BitSet();
        activatedMap.or(activated);
        while (true) {
            BitSet addAlso = getActivated(activatedMap, activated);
            if (addAlso.cardinality() == 0) {
                break;
            }
            readyOperators.or(addAlso);
            activated.or(addAlso);
        }
        return activated;
    }

    public BitSet addTerminated(BitSet terminated) {
        BitSet activated = new BitSet();
        BitSet activatedMap = new BitSet();

        readyOperators.andNot(terminated);
        terminatedOperators.or(terminated);

        activatedMap.or(terminated);
        activated.or(terminated);

        while (true) {
            BitSet addAlso = getActivated(activatedMap, activated);
            if (addAlso.cardinality() == 0) {
                break;
            }
            readyOperators.or(addAlso);
            activated.or(addAlso);
        }
        activated.andNot(terminated);
        return activated;
    }

    private BitSet getLeafs() {
        BitSet graphLeafs = new BitSet();
        for (ConcreteOperator start : queryGraph.getLeafOperators()) {
            readyOperators.set(start.opID);
            graphLeafs.set(start.opID);
        }
        return graphLeafs;
    }

    private BitSet getActivated(BitSet activatedMap, BitSet activatedList) {
        BitSet activatedAlso = new BitSet();
        for (int id = activatedList.nextSetBit(0); id >= 0; id = activatedList.nextSetBit(id + 1)) {
            for (Link fromLink : queryGraph.getOutputLinks(id)) {
                int toOpId = fromLink.to.opID;
                if (activatedMap.get(toOpId)) {
                    continue;
                }
                if (readyOperators.get(toOpId)) {
                    continue;
                }
                if (terminatedOperators.get(toOpId)) {
                    continue;
                }
                switch (fromLink.to.behavior) {
                    case pipeline: {
                        boolean add = true;
            /* All S/F operators in it's inputs must have terminated
            and all PL operators must have been activated */
                        for (Link toLink : queryGraph.getInputLinks(toOpId)) {
                            if (toLink.from.behavior == OperatorBehavior.pipeline) {
                                if (terminatedOperators.get(toLink.from.opID) == false) { /* not terminated */
                                    if (readyOperators.get(toLink.from.opID) == false) { /* not ready */
                                        add = false;
                                        break;
                                    }
                                }
                            } else {
                                if (terminatedOperators.get(toLink.from.opID) == false) {
                                    add = false;
                                    break;
                                }
                            }
                        }
                        if (add) {
                            activatedAlso.set(toOpId);
                            activatedMap.set(toOpId);
                        }
                        break;
                    }
                    case store_and_forward: {
                        boolean add = true;
            /* All operators in it's inputs must have terminated */
                        for (Link toLink : queryGraph.getInputLinks(toOpId)) {
                            if (terminatedOperators.get(toLink.from.opID) == false) {
                                add = false;
                                break;
                            }
                        }
                        if (add) {
                            activatedAlso.set(toOpId);
                            activatedMap.set(toOpId);
                        }
                        break;
                    }
                }
            }
        }
        return activatedAlso;
    }
}
