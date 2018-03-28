package madgik.exareme.master.gateway;

import madgik.exareme.master.app.cluster.ExaremeCluster;
import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;

/**
 * @author alex
 */
public class ExaremeGatewayStart {

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.ALL);

        final ExaremeCluster cluster = ExaremeClusterFactory.createMiniCluster(1098, 8088, 2);
        cluster.start();
        final ExaremeGateway gateway =
            ExaremeGatewayFactory.createHttpServer(cluster.getDBManager());

        gateway.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                gateway.stop();
                try {
                    cluster.stop(true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
