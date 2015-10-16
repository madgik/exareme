/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.combinatorics;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class Permutations implements Serializable {
    private final int itemsCount;
    private final int[] items;
    private int total = 0;

    public Permutations(int itemsCount) {
        this.itemsCount = itemsCount;
        this.items = new int[itemsCount];
        for (int i = 0; i < itemsCount; ++i) {
            items[i] = i;
        }
    }

    public static void main(String[] args) {
        Permutations p = new Permutations(4);
        p.compute();
    }

    public void compute() {
        total = 0;
        compute(0);
        System.out.println("Total: " + total);
    }

    private void compute(int start) {
        // Only two items
        if (start == itemsCount - 2) {
            print();
            total++;
            swap(start, start + 1);
            print();
            total++;
            swap(start + 1, start);
            return;
        }
        compute(start + 1);
        for (int i = start + 1; i < itemsCount; ++i) {
            swap(start, i);
            compute(start + 1);
        }
    }

    private void swap(int i, int j) {
        int tmp = items[i];
        items[i] = items[j];
        items[j] = tmp;
    }

    private void print() {
        for (int i = 0; i < itemsCount; ++i) {
            System.out.print(items[i] + " ");
        }
        System.out.println("");
    }
}
