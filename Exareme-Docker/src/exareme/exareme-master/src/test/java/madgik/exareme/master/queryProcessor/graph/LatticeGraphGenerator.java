/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import java.util.LinkedList;
import java.util.Random;

/**
 * @author herald
 */
public class LatticeGraphGenerator {

    private LatticeGraphGenerator() {
    }

    public static ConcreteQueryGraph createLatticeGraph(int depth, int breadth,
                                                        RandomParameters params, long seed) {
        ConcreteQueryGraph graph = new ConcreteQueryGraph();

        Random rand = new Random(seed);

        LinkedList<LinkedList<ConcreteOperator>> operatorsLevelsUp = new LinkedList<>();
        LinkedList<LinkedList<ConcreteOperator>> operatorsLevelsDown = new LinkedList<>();
        LinkedList<ConcreteOperator> middleOperators = new LinkedList<>();

        int N = 0;
        for (int i = 0; i < depth / 2; i++) {
            N += 2 * Math.pow(breadth, i);
        }
        N += Math.pow(breadth, (depth / 2));

        int opNum = 0;
        for (int i = 0; i < depth / 2; i++) {
            LinkedList<ConcreteOperator> upOperators = new LinkedList<>();
            LinkedList<ConcreteOperator> downOperators = new LinkedList<>();

            // up
            for (int j = 0; j < Math.pow(breadth, i); j++) {
                opNum++;
                ConcreteOperator op = GraphGenerator.createOperator("op" + opNum, 1, params, rand);
                upOperators.add(op);
                graph.addOperator(op);
            }

            // down
            for (int j = 0; j < Math.pow(breadth, i); j++) {
                opNum++;
                ConcreteOperator op =
                        GraphGenerator.createOperator("op" + opNum, breadth, params, rand);
                downOperators.add(op);
                graph.addOperator(op);
            }

            operatorsLevelsUp.addLast(upOperators);
            operatorsLevelsDown.addFirst(downOperators);
        }

        for (int j = 0; j < Math.pow(breadth, (depth / 2)); j++) {
            opNum++;
            ConcreteOperator op = GraphGenerator.createOperator("op" + opNum, 1, params, rand);
            middleOperators.add(op);
            graph.addOperator(op);
        }

        // create links
        for (int i = operatorsLevelsUp.size() - 1; i > 0; i--) {
            LinkedList<ConcreteOperator> opList = operatorsLevelsUp.get(i);
            LinkedList<ConcreteOperator> opListParent = operatorsLevelsUp.get(i - 1);

            for (int j = 0; j < opList.size(); j++) {
                ConcreteOperator from = opList.get(j);
                ConcreteOperator to = opListParent.get((j / breadth));

                LinkData data = from.getOutputData(0);
                Link link = new Link(from, to, data);
                data.updateLinks(link);

                graph.addLink(link);
                to.addInputData(data, j % breadth);

                LocalFileData lfd = new LocalFileData(data.name + ".file", data.size_MB);
                from.addFileOutputData(lfd);
                to.addFileInputData(lfd);
            }
        }

        for (int j = 0; j < middleOperators.size(); j++) {
            ConcreteOperator from = middleOperators.get(j);
            ConcreteOperator to =
                    operatorsLevelsUp.get(operatorsLevelsUp.size() - 1).get((j / breadth));

            LinkData data = from.getOutputData(0);
            Link link = new Link(from, to, data);
            data.updateLinks(link);

            graph.addLink(link);
            to.addInputData(data, j % breadth);

            LocalFileData lfd = new LocalFileData(data.name + ".file", data.size_MB);
            from.addFileOutputData(lfd);
            to.addFileInputData(lfd);
        }

        for (int j = 0; j < middleOperators.size(); j++) {
            ConcreteOperator to = middleOperators.get(j);
            ConcreteOperator from = operatorsLevelsDown.get(0).get((j / breadth));

            LinkData data = from.getOutputData(j % breadth);
            Link link = new Link(from, to, data);
            data.updateLinks(link);

            graph.addLink(link);
            to.addInputData(data, 0);

            LocalFileData lfd = new LocalFileData(data.name + ".file", data.size_MB);
            from.addFileOutputData(lfd);
            to.addFileInputData(lfd);
        }

        for (int i = 1; i < operatorsLevelsDown.size(); i++) {
            LinkedList<ConcreteOperator> opList = operatorsLevelsDown.get(i);
            LinkedList<ConcreteOperator> opListParent = operatorsLevelsDown.get(i - 1);

            for (int j = 0; j < opListParent.size(); j++) {
                ConcreteOperator to = opListParent.get(j);
                ConcreteOperator from = opList.get((j / breadth));

                LinkData data = from.getOutputData(j % breadth);
                Link link = new Link(from, to, data);
                data.updateLinks(link);

                graph.addLink(link);
                to.addInputData(data, 0);

                LocalFileData lfd = new LocalFileData(data.name + ".file", data.size_MB);
                from.addFileOutputData(lfd);
                to.addFileInputData(lfd);
            }
        }
        return graph;
    }
}
