/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.dag;

import java.util.*;

/**
 * @author jim
 */
public class PlanIterator implements Iterator<Node> {
    private final List<Node> plan;
    private int curIdx;

    public PlanIterator(Node root) {
        plan = new LinkedList<Node>();
        initializeIterator(root);
        curIdx = this.plan.size() - 1;
    }

    /*interface methods*/
    @Override
    public boolean hasNext() {
        return this.curIdx != -1;
    }

    @Override
    public Node next() {
        Node n = this.plan.get(curIdx);
        this.curIdx--;
        return n;
    }

    @Override
    public void remove() {
        this.plan.remove(this.curIdx);
        this.curIdx--;
    }

    public void resetIterator() {
        this.curIdx = 0;
    }

    /*private - helper methods*/
    private void initializeIterator(Node root) {
        Set<Node> visitedNodes = new HashSet<Node>();
        int lastNodeIdx = 0;
        Node curNode = root;
        boolean planExpanded = false;
        int nullChildren = 0;

        this.plan.add(curNode);
        visitedNodes.add(curNode);

        while (planExpanded == false) {
            for (int i = lastNodeIdx; i < this.plan.size(); i++) {
                curNode = this.plan.get(i);

                if (curNode.getChildren() == null)
                    nullChildren++;
                else {
                    for (Node child : curNode.getChildren()) {
                        if (!visitedNodes.contains(child)) {
                            this.plan.add(child);
                            lastNodeIdx++;
                            visitedNodes.add(child);
                        }
                    }
                }
            }

            if (nullChildren == this.plan.size() - 1 - lastNodeIdx)
                planExpanded = true;
            else
                nullChildren = 0;
        }

    }


}
