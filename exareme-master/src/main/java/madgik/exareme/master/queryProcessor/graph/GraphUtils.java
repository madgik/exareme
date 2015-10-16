/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.utils.check.Check;
import madgik.exareme.utils.serialization.SerializationUtil;

import java.util.*;

/**
 * @author heraldkllapi
 */
public class GraphUtils {

    public static boolean containsLink(int op1, int op2, ConcreteQueryGraph abstractGraph) {
        Collection<Link> out1 = abstractGraph.getOutputLinks(op1);
        for (Link l : out1) {
            if (l.to.opID == op2) {
                return true;
            }
        }
        Collection<Link> out2 = abstractGraph.getOutputLinks(op2);
        for (Link l : out2) {
            if (l.to.opID == op1) {
                return true;
            }
        }
        return false;
    }

    public static ConcreteQueryGraph normalizeGraph(ConcreteQueryGraph graph) {
        ConcreteQueryGraph normGraph = new ConcreteQueryGraph();
        HashMap<Integer, ConcreteOperator> oldIdNewIdMap = new HashMap<Integer, ConcreteOperator>();
        // Add operators
        for (ConcreteOperator co : graph.getOperators()) {
            ConcreteOperator newCo =
                new ConcreteOperator(co.operatorName, co.runTime_SEC, co.cpuUtilization,
                    co.memory_MB, co.behavior);
            oldIdNewIdMap.put(co.opID, newCo);
            // Local files
            for (LocalFileData in : co.inputFileDataArray) {
                newCo.addFileInputData(new LocalFileData(in));
            }
            for (LocalFileData out : co.outputFileDataArray) {
                newCo.addFileOutputData(new LocalFileData(out));
            }
            normGraph.addOperator(newCo);
        }
        // Add links
        for (Link link : graph.getLinks()) {
            LinkData linkData = new LinkData(link.data);
            Link newLink =
                new Link(oldIdNewIdMap.get(link.from.opID), oldIdNewIdMap.get(link.to.opID),
                    linkData);
            linkData.updateLinks(newLink);
            newLink.from.addOutputData(linkData);
            newLink.to.addInputData(linkData);
            normGraph.addLink(newLink);
        }
        return normGraph;
    }

    public static ConcreteQueryGraph combineGraphs(List<ConcreteQueryGraph> queryGraphs,
        List<Map<Integer, Integer>> opIdToCombinedIdMap) {
        ConcreteQueryGraph combined = new ConcreteQueryGraph();
        for (ConcreteQueryGraph graph : queryGraphs) {
            Map<Integer, Integer> idToCombId = new HashMap<Integer, Integer>();
            // Add operators
            for (ConcreteOperator cOp : graph.getOperators()) {
                ConcreteOperator newCOp = SerializationUtil.deepCopy(cOp);
                newCOp.opID = -1;
                combined.addOperator(newCOp);
                idToCombId.put(cOp.opID, newCOp.opID);
            }
            // Add links
            for (Link l : graph.getLinks()) {
                int newFrom = idToCombId.get(l.from.opID);
                int newTo = idToCombId.get(l.to.opID);

                LinkData data = new LinkData(l.data.name, l.data.size_MB);
                data.setFromOpID(newFrom);
                data.setToOpID(newTo);

                Link newL =
                    new Link(combined.getOperator(newFrom), combined.getOperator(newTo), data);
                combined.addLink(newL);
            }
            if (opIdToCombinedIdMap != null) {
                opIdToCombinedIdMap.add(idToCombId);
            }
        }
        return combined;
    }

    public static ConcreteQueryGraph combineAndConnectGraphs(List<ConcreteQueryGraph> queryGraphs) {
        List<Map<Integer, Integer>> opIdToCombinedIdMap = new ArrayList<Map<Integer, Integer>>();
        return combineAndConnectGraphs(queryGraphs, opIdToCombinedIdMap);
    }

    public static ConcreteQueryGraph combineAndConnectGraphs(List<ConcreteQueryGraph> queryGraphs,
        List<Map<Integer, Integer>> opIdToCombinedIdMap) {
        ConcreteQueryGraph combined = GraphUtils.combineGraphs(queryGraphs, opIdToCombinedIdMap);
        for (int g = 1; g < queryGraphs.size(); ++g) {
            ArrayList<ConcreteOperator> fromGraphProd = new ArrayList<ConcreteOperator>();
            {
                ConcreteQueryGraph fromGraph = queryGraphs.get(g - 1);
                Map<Integer, Integer> fromIdToCombinedId = opIdToCombinedIdMap.get(g - 1);

                for (ConcreteOperator op : fromGraph.getOperators()) {
                    if (fromGraph.getOutputLinks(op.opID).isEmpty()) {
                        fromGraphProd.add(combined.getOperator(fromIdToCombinedId.get(op.opID)));
                    }
                }
            }
            ArrayList<ConcreteOperator> toGraphCons = new ArrayList<ConcreteOperator>();
            {
                ConcreteQueryGraph toGraph = queryGraphs.get(g);
                Map<Integer, Integer> toIdToCombinedId = opIdToCombinedIdMap.get(g);
                for (ConcreteOperator op : toGraph.getOperators()) {
                    if (toGraph.getInputLinks(op.opID).isEmpty()) {
                        toGraphCons.add(combined.getOperator(toIdToCombinedId.get(op.opID)));
                    }
                }
            }
            Check.True(!fromGraphProd.isEmpty(), "No producer found");
            for (int f = 0; f < fromGraphProd.size(); ++f) {
                ConcreteOperator from = fromGraphProd.get(f);
                for (int t = 0; t < toGraphCons.size(); ++t) {
                    ConcreteOperator to = toGraphCons.get(t);
                    LinkData data = new LinkData("MR_" + from.getName() + ":" + to.getName(),
                        from.outputDataArray.get(0).size_MB / toGraphCons.size());
                    Link link = new Link(from, to, data);
                    data.updateLinks(link);
                    combined.addLink(link);
                }
            }
        }
        for (ConcreteOperator op : combined.getOperators()) {
            int in = combined.getInputLinks(op.opID).size();
            int out = combined.getOutputLinks(op.opID).size();
            Check.True(in + out > 0, "Graph not connected!");
        }
        return combined;
    }

    public static ConcreteQueryGraph removeAllLinks(ConcreteQueryGraph graph) {
        ConcreteQueryGraph newGraph = new ConcreteQueryGraph();
        for (ConcreteOperator co : graph.getOperators()) {
            ConcreteOperator newCo = SerializationUtil.deepCopy(co);
            newCo.opID = -1;
            newCo.inputLinks.clear();
            newCo.inputDataArray.clear();
            newCo.outputLinks.clear();
            newCo.outputDataArray.clear();
            newGraph.addOperator(newCo);
        }
        return newGraph;
    }
}
