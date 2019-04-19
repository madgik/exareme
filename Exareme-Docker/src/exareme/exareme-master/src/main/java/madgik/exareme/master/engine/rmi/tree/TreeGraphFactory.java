package madgik.exareme.master.engine.rmi.tree;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by panos on 5/30/14.
 */
public class TreeGraphFactory {

    public static TreeConcreteQueryGraph createQueryGraph(ContainerTopology topology)
            throws RemoteException {
        // TODO(panos): fix me
        TreeGenerator gen = new TreeGenerator();
        List<TreeConcreteQueryGraph> trees = gen.generateSupportedTreeQueryGraphs(topology, "");
        // todo select the query graph with the better reduction
        return trees.get(0);
    }
}
