package madgik.exareme.master.engine.rmi.tree;

import java.util.Arrays;

/**
 * Created by panos on 5/13/14.
 */
public class TopologyGenerator {

    public static TreeContainerTopology generateDemoContainerTopology() {
        // returns the following container topology
        // https://onedrive.live.com/?cid=4CCF638A7AB4D5AB&id=4CCF638A7AB4D5AB%2110176&v=3
        TreeContainerTopology fixedContainerTopology = new TreeContainerTopology();

        fixedContainerTopology.addLeafContainer();
        fixedContainerTopology.addLeafContainer();
        fixedContainerTopology.addLeafContainer();
        fixedContainerTopology.addLeafContainer();

        fixedContainerTopology.addInternalContainer(Arrays.asList(0, 1));
        fixedContainerTopology.addInternalContainer(Arrays.asList(0, 1, 2));
        fixedContainerTopology.addInternalContainer(Arrays.asList(1, 2, 3));
        fixedContainerTopology.addInternalContainer(Arrays.asList(3));

        fixedContainerTopology.addInternalContainer(Arrays.asList(4, 5));
        fixedContainerTopology.addInternalContainer(Arrays.asList(5, 6));
        fixedContainerTopology.addInternalContainer(Arrays.asList(6, 7));

        fixedContainerTopology.addRootContainer(Arrays.asList(8, 9));
        fixedContainerTopology.addRootContainer(Arrays.asList(9, 10));

        return fixedContainerTopology;
    }

    public static TreeContainerTopology generateDevelopmentContainerTopology() {
        TreeContainerTopology topology = new TreeContainerTopology();

        // 0, 1
        topology.addLeafContainer();
        topology.addLeafContainer();
        // 2, 3
        topology.addInternalContainer(Arrays.asList(0, 1));
        topology.addInternalContainer(Arrays.asList(0, 1));
        // 3
        topology.addRootContainer(Arrays.asList(2, 3));

        return topology;
    }
    // todo elasticity! 3:)
}
