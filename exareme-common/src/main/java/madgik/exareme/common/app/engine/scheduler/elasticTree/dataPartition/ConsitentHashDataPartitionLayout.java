/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.dataPartition;

import madgik.exareme.common.app.engine.scheduler.elasticTree.DataPartitionLayout;
import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeConstants;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;
import java.util.*;

/**
 * @author heraldkllapi
 */
public class ConsitentHashDataPartitionLayout implements DataPartitionLayout {

    private final int numParts;
    private final int replication;
    private final int parts;
    private final int rebalanceOffset = 2;
    private final ArrayList<ContainerIndex> contIdxs;
    private final ContainerIndexComparator comparator = new ContainerIndexComparator();
    private final Random rand = new Random(TreeConstants.SETTINGS.RANDOM_SEED);

    public ConsitentHashDataPartitionLayout(int numParts, int replication) {
        this.numParts = numParts;
        this.replication = replication;
        this.parts = numParts * replication;
        this.contIdxs = new ArrayList<>();
    }

    @Override public void initializeWithContainers(List<Long> containers) {
        double partsPerContainer = (double) numParts * replication / containers.size();
        int part = 0;
        double remaining = 0.0;
        for (int i = 0; i < containers.size(); ++i) {
            contIdxs.add(new ContainerIndex(containers.get(i), part % parts));
            int steps = (int) (partsPerContainer + remaining);
            part += steps;
            remaining = (partsPerContainer + remaining) - steps;
        }
    }

    @Override public void addContainers(List<Long> containers) {
        contIdxs.ensureCapacity(contIdxs.size() + containers.size());
        for (long c : containers) {
            int maxDiff = 0;
            int index = 0;
            for (int i = 0; i < contIdxs.size(); ++i) {
                ContainerIndex before = contIdxs.get(i);
                ContainerIndex after = contIdxs.get((i + 1) % contIdxs.size());
                int diff;
                if (i == contIdxs.size() - 1) {
                    // Last index
                    diff = parts - before.index;
                    diff += after.index;
                } else {
                    diff = after.index - before.index;
                }
                if (diff > maxDiff) {
                    maxDiff = diff;
                    index = i;
                }
            }
            ContainerIndex before = contIdxs.get(index);
            contIdxs.add(index + 1, new ContainerIndex(c, (before.index + maxDiff / 2) % parts));
            rebalanceAround(index + 1, 1);
        }
        rebalanceIfNeeded();
    }

    @Override public void removeContainers(List<Long> containers) {
        for (long c : containers) {
            for (int i = 0; i < contIdxs.size(); ++i) {
                ContainerIndex contIdx = contIdxs.get(i);
                if (contIdx.container == c) {
                    contIdxs.remove(i);
                    rebalanceAround(i, 2);
                    break;
                }
            }
        }
        rebalanceIfNeeded();
    }

    @Override public long getContainer(int part) {
        int randPart = part * replication + rand.nextInt(replication);
        int idx = Collections.binarySearch(contIdxs, new ContainerIndex(-1, randPart), comparator);
        if (idx < 0) {
            idx = -idx - 1;
        }
        if (idx == contIdxs.size()) {
            idx = 0;
        }
        return contIdxs.get(idx).container;
    }

    //  @Override
    //  public void removeContainers(List<Long> containers) {
    //    ArrayList<Long> contArray = new ArrayList<Long>(containers);
    //    Collections.sort(contArray);
    //
    //    ArrayList<ContainerIndex> newContainerIndexes = new ArrayList<ContainerIndex>();
    //    for (ContainerIndex idx : containerIndexes) {
    //      if (Collections.binarySearch(contArray, idx.container) < 0) {
    //        newContainerIndexes.add(idx);
    //      }
    //    }
    //    containerIndexes = newContainerIndexes;
    //    rebalanceIfNeeded();
    //  }

    @Override public int getNumContainers() {
        return contIdxs.size();
    }

    @Override public int getReplication() {
        return replication;
    }

    @Override public int getNumParts() {
        return numParts;
    }

    @Override public void getPartContainers(int part, Set<Long> containers) {
        int partStart = part * replication;
        int idx = Collections
            .binarySearch(contIdxs, new ContainerIndex(-1, part * replication), comparator);
        if (idx < 0) {
            idx = -idx - 1;
        }
        if (idx == contIdxs.size()) {
            idx = 0;
        }
        ContainerIndex container = contIdxs.get(idx);
        containers.add(container.container);
        for (int i = idx; i < contIdxs.size(); ++i) {
            ContainerIndex next = contIdxs.get(i);
            if (next.index < partStart + replication) {
                containers.add(next.container);
            }
        }
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contIdxs.size(); ++i) {
            ContainerIndex before = contIdxs.get(i);
            ContainerIndex after = contIdxs.get((i + 1) % contIdxs.size());
            int diff;
            if (i == contIdxs.size() - 1) {
                // Last index
                diff = parts - before.index;
                diff += after.index;
            } else {
                diff = after.index - before.index;
            }
            sb.append(diff + "\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private void rebalanceAround(int middleCont, int offsetBoost) {
        int offset = rebalanceOffset * offsetBoost;
        int startCont = (middleCont - offset);
        if (startCont < 0) {
            startCont = (contIdxs.size() + (contIdxs.size() + startCont)) % contIdxs.size();
        }
        int endCont = (middleCont + offset) % contIdxs.size();
        if (startCont < 0) {
            startCont = 0;
            endCont = contIdxs.size() - 1;
        }
        ContainerIndex startContIdx = contIdxs.get(startCont);
        ContainerIndex endContIdx = contIdxs.get(endCont);
        //    System.out.println("RB: " + startCont + " (" + middleCont + ") " + endCont + " / " + contIdxs.size());

        //    System.out.println("   " + startContIdx.index + " --- " + endContIdx.index);
        int range = 0;
        if (startCont < endCont) {
            range = endContIdx.index - startContIdx.index;
        } else {
            range = parts - startContIdx.index;
            range += endContIdx.index;
        }
        //    System.out.println("RANGE: " + range);
        double partsPerContainer = (double) range / (2 * offset);
        int part = startContIdx.index;
        double remaining = 0.0;
        for (int i = 0; i < 2 * offset - 1; ++i) {
            int steps = (int) (partsPerContainer + remaining);
            part += steps;
            remaining = (partsPerContainer + remaining) - steps;
            ContainerIndex cont = contIdxs.get((startCont + 1 + i) % contIdxs.size());
            cont.index = part % parts;
            //      System.out.println("   " + (startCont + 1 + i) % contIdxs.size() + " > " + cont.index);
        }
        // Preserve index order
        ContainerIndex first = contIdxs.get(0);
        ContainerIndex last = contIdxs.get(contIdxs.size() - 1);
        while (first.index > last.index) {
            // swap
            contIdxs.add(first);
            contIdxs.remove(0);

            first = contIdxs.get(0);
            last = contIdxs.get(contIdxs.size() - 1);
        }
    }

    private void rebalanceIfNeeded() {
        if (true) {
            return;
        }
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int i = 0; i < contIdxs.size(); ++i) {
            ContainerIndex before = contIdxs.get(i);
            ContainerIndex after = contIdxs.get((i + 1) % contIdxs.size());
            int diff;
            if (i == contIdxs.size() - 1) {
                // Last index
                diff = parts - before.index;
                diff += after.index;
            } else {
                diff = after.index - before.index;
            }
            stats.addValue(diff);
        }

        if (stats.getMax() > stats.getMean() + 1.5 * stats.getStandardDeviation()) {
            System.out.println("REPARTITION ::: " + stats.getMean() + " - " + stats.getMax());
            double partsPerContainer = (double) numParts * replication / contIdxs.size();
            int part = 0;
            double remaining = 0.0;
            for (ContainerIndex idx : contIdxs) {
                idx.index = part % parts;
                int steps = (int) (partsPerContainer + remaining);
                part += steps;
                remaining = (partsPerContainer + remaining) - steps;
            }
        }
    }


    class ContainerIndex implements Serializable {
        private final long container;
        private int index;

        public ContainerIndex(long container, int index) {
            this.container = container;
            this.index = index;
        }

        @Override public String toString() {
            return "" + index;
        }
    }


    class ContainerIndexComparator implements Comparator<ContainerIndex>, Serializable {
        @Override public int compare(ContainerIndex o1, ContainerIndex o2) {
            return Integer.compare(o1.index, o2.index);
        }
    }
}
