/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.combinatorics;

import org.apache.log4j.Logger;

import java.util.LinkedList;

/**
 * @author herald
 */
public class ConstantSumGenerator {

    private static Logger log = Logger.getLogger(ConstantSumGenerator.class);
    public LinkedList<Integer> lastSolution = null;
    int n = 0;
    int k = 0;

    public ConstantSumGenerator(int n, int k) {

        this.n = n;
        this.k = k;

        lastSolution = new LinkedList<Integer>();
        lastSolution.add(n);
    }

    public LinkedList<Integer> getNext() {
        if (lastSolution == null) {
            return null;
        }
        if (k == 1) {
            LinkedList<Integer> solution = lastSolution;
            lastSolution = null;
            return solution;
        }
        while (true) {
            generateNext();
            if (lastSolution == null) {
                return null;
            }
            if (lastSolution.size() == k) {
                return lastSolution;
            }
        }
    }

    private void generateNext() {
        if (lastSolution.get(0) == 1) {
            lastSolution = null;
            return;
        }
        LinkedList<Integer> newSolution = new LinkedList<Integer>();
        for (int i = lastSolution.size() - 1; i >= 0; i--) {
            int number = lastSolution.get(i);
            if (number > 1) {
                if (i != 0) {
                    newSolution.addFirst(1);
                    newSolution.addFirst(number - 1);
                    i--;

                    for (; i >= 0; i--) {
                        newSolution.addFirst(lastSolution.get(i));
                    }

                    lastSolution = newSolution;
                    return;
                } else {
                    break;
                }
            } else {
                newSolution.addFirst(number);
            }
        }

        newSolution.clear();
        int first = lastSolution.get(0) - 1;
        newSolution.addLast(first);
        int remaining = n - first;
        while (remaining > 0) {
            if (remaining >= first) {
                newSolution.addLast(first);
                remaining -= first;
            } else {
                newSolution.addLast(remaining);
                remaining = 0;
            }
        }

        lastSolution = newSolution;
    }
}
