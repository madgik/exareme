/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi.tree;

/**
 * @author heraldkllapi
 */
public class ContainerTopologyFactory {

    public static ContainerTopology getTopology() {
        // TODO(panos): crete the topology
        return TopologyGenerator.generateDevelopmentContainerTopology();
    }
}
