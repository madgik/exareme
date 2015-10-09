package madgik.exareme.master.app.cluster;


import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;

import java.rmi.RemoteException;

/**
 * @author alex
 */
public interface ExaremeCluster {

    void start() throws RemoteException;

    boolean isUp();

    void stop(boolean force) throws RemoteException;

    AdpDBManager getDBManager() throws RemoteException;

    AdpDBClient getExaremeClusterClient(AdpDBClientProperties properties) throws RemoteException;

    public Container[] getContainers() throws RemoteException;

}
