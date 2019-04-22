/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManager;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManagerInterface;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManagerProxy;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import madgik.exareme.worker.art.remote.RmiRemoteObject;

import java.rmi.RemoteException;
import java.util.UUID;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiPlanSessionManager extends RmiRemoteObject<PlanSessionManagerProxy>
        implements PlanSessionManager {
    private PlanSessionManagerInterface sessionManagerInterface = null;
    private EntityName regEntityName = null;
    private ArtRegistryProxy registryProxy = null;

    public RmiPlanSessionManager(PlanSessionManagerInterface sessionManagerInterface,
                                 EntityName regEntityName, ArtRegistryProxy registryProxy) throws RemoteException {
        super(NetUtil.getIPv4() + "_planSessionManager_" + UUID.randomUUID().toString());

        this.sessionManagerInterface = sessionManagerInterface;
        this.regEntityName = regEntityName;
        this.registryProxy = registryProxy;
        super.register();
    }

    @Override
    public PlanSessionManagerProxy createProxy() throws RemoteException {
        return new RmiPlanSessionManagerProxy(super.getRegEntryName(), regEntityName);
    }

    @Override
    public void execute(ExecutionPlan plan, PlanSessionID sessionID)
            throws RemoteException {
        sessionManagerInterface.execute(plan, sessionID);
    }

    @Override
    public void destroySession(PlanSessionID sessionID) throws RemoteException {
        sessionManagerInterface.destroySession(sessionID);
    }

    @Override
    public PlanSessionID createNewSessionElasticTree() throws RemoteException {
        return sessionManagerInterface.createNewSessionElasticTree();
    }


    @Override
    public void executeElasticTree(ExecutionPlan plan, SLA sla, PlanSessionID sessionID)
            throws RemoteException {
        sessionManagerInterface.executeElasticTree(plan, sla, sessionID);
    }


    @Override
    public void destroyAllSessions() throws RemoteException {
        sessionManagerInterface.destroyAllSessions();
    }

    @Override
    public void stopManager() throws RemoteException {
        stopManager(false);
    }

    @Override
    public void stopManager(boolean force) throws RemoteException {
        sessionManagerInterface.stopManager(force);
        super.unregister();
    }

    @Override
    public PlanSessionID createNewSession() throws RemoteException {
        return sessionManagerInterface.createNewSession();
    }

    @Override
    public ContainerSessionID createContainerSession(PlanSessionID planSessionID)
            throws RemoteException {
        return sessionManagerInterface.createContainerSession(planSessionID);
    }


}
