package madgik.exareme.master.app.cluster;

import madgik.exareme.master.app.cluster.mini.ExaremeMiniCluster;

/**
 * @author alex
 */
public class ExaremeClusterFactory {

    public static ExaremeCluster createMiniCluster(int port, int dtport, int nworkers) {
        return new ExaremeMiniCluster(port, dtport, nworkers);
    }
}
