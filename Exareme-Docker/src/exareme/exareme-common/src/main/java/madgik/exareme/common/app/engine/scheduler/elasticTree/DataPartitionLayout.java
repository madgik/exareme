/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author heraldkllapi
 */
public interface DataPartitionLayout extends Serializable {

    void initializeWithContainers(List<Long> containers);

    void addContainers(List<Long> containers);

    void removeContainers(List<Long> containers);

    long getContainer(int part);

    void getPartContainers(int part, Set<Long> containers);

    int getNumContainers();

    int getNumParts();

    int getReplication();
}
