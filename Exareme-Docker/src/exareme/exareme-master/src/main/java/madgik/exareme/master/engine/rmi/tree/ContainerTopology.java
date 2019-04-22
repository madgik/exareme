/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi.tree;

import madgik.exareme.master.queryProcessor.optimizer.ContainerResources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author panos
 */
public interface ContainerTopology extends Serializable {

    int getContainerLevel(int containerId);

    List<Integer> getContainersAtLevel(int level);

    Integer getContainerAtLevel(int level, int index);

    List<Integer> getContainersAtNextLevel(int level);

    int getHeight();

    int getRootLevel();

    List<Integer> getChildrenOf(int containerId);

    int getChildrenCountOf(int containerId);

    List<Integer> getParentsOf(int containerId);

    ContainerResources getContainerResourcesOf(int containerId);

    ArrayList<ContainerResources> getAllContainerResources();

    Set<Integer> getAllContainers();

    String getVizString();
}
