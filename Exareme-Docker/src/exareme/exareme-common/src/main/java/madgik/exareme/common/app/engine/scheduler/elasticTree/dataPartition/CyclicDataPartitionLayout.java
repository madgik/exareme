/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.dataPartition;


import madgik.exareme.common.app.engine.scheduler.elasticTree.DataPartitionLayout;
import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeConstants;
import madgik.exareme.utils.check.Check;

import java.util.*;

/**
 * @author heraldkllapi
 */
public class CyclicDataPartitionLayout implements DataPartitionLayout {
    private final int numParts;
    private final int replication;

    // The partition is the index
    private final ArrayList<Long> partToContainerAssignment;
    private final ArrayList<Long> allContainers;
    private final Random rand = new Random(TreeConstants.SETTINGS.RANDOM_SEED);

    public CyclicDataPartitionLayout(int numParts, int replication) {
        this.numParts = numParts;
        this.replication = replication;
        this.partToContainerAssignment = new ArrayList<>();
        for (int i = 0; i < numParts * replication; ++i) {
            partToContainerAssignment.add(-1L);
        }
        this.allContainers = new ArrayList<>();
    }

    @Override
    public void initializeWithContainers(List<Long> containers) {
        allContainers.addAll(containers);
        distributePartsToContainers();
    }

    @Override
    public void addContainers(List<Long> containers) {
        allContainers.addAll(containers);
        distributePartsToContainers();
    }

    @Override
    public void removeContainers(List<Long> containers) {
        int initialNumber = allContainers.size();
        HashSet<Long> containersToDelete = new HashSet<>(containers);
        allContainers.removeAll(containersToDelete);
        Check.True(initialNumber - allContainers.size() == containers.size(),
                "Not all containers found");
        distributePartsToContainers();
    }

    @Override
    public long getContainer(int part) {
        int randPart = rand.nextInt(replication);
        return partToContainerAssignment.get(part * replication + randPart);
    }

    private void distributePartsToContainers() {
        double partsPerContainer = (double) numParts * replication / allContainers.size();
        int part = 0;
        double remaining = 0.0;
        for (int i = 0; i < allContainers.size() - 1; ++i) {
            int steps = 0;
            for (int p = 0; p < partsPerContainer + remaining; ++p) {
                if (part >= numParts * replication) {
                    return;
                }
                partToContainerAssignment.set(part, allContainers.get(i));
                //        System.out.println(part + " -> " + allContainers.get(i));
                part++;
                steps++;
            }
            remaining = (partsPerContainer + remaining) - steps;
        }
        Long lastContainer = allContainers.get(allContainers.size() - 1);
        for (int p = part; p < numParts * replication; ++p) {
            partToContainerAssignment.set(part, lastContainer);
            //      System.out.println(part + " -> " + lastContainer);
            part++;
        }
        //    System.out.println("---\n");
    }

    @Override
    public int getNumContainers() {
        return allContainers.size();
    }

    @Override
    public int getReplication() {
        return replication;
    }

    @Override
    public int getNumParts() {
        return numParts;
    }

    @Override
    public void getPartContainers(int part, Set<Long> containers) {
        for (int r = 0; r < replication; ++r) {
            containers.add(partToContainerAssignment.get(part * replication + r));
        }
    }
}
