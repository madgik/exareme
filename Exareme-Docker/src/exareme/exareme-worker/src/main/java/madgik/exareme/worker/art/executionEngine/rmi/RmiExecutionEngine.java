/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.executionEngine.ExecutionEngine;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineStatus;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManager;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManagerInterface;
import madgik.exareme.worker.art.executionEngine.clockTickManager.ClockTickManagerProxy;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManager;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManagerInterface;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManagerProxy;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManager;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManagerInterface;
import madgik.exareme.worker.art.executionEngine.sessionMgr.PlanSessionManagerProxy;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManager;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManagerInterface;
import madgik.exareme.worker.art.executionEngine.statisticsMgr.PlanSessionStatisticsManagerProxy;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManager;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerInterface;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerProxy;
import madgik.exareme.worker.art.managementBean.ExecutionEngineManagement;
import madgik.exareme.worker.art.managementBean.ManagementUtil;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import madgik.exareme.worker.art.registry.ArtRegistryProxy;
import madgik.exareme.worker.art.registry.PolicyFactory;
import madgik.exareme.worker.art.registry.updateDeamon.RegistryUpdateDeamon;
import madgik.exareme.worker.art.registry.updateDeamon.RegistryUpdateDeamonFactory;
import madgik.exareme.worker.art.remote.RmiRemoteObject;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.UUID;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiExecutionEngine extends RmiRemoteObject<ExecutionEngineProxy>
        implements ExecutionEngine {
    private static String BEAN_NAME = "ExecutionEngine";
    private static Logger log = Logger.getLogger(RmiExecutionEngine.class);
    private String ip = null;
    private PlanSessionManager sessionManager;
    private PlanSessionStatusManager sessionStatusManager = null;
    private PlanSessionReportManager sessionReportManager = null;
    private PlanSessionStatisticsManager sessionStatisticsManager = null;
    private ClockTickManager clockTickManager = null;
    private ArtRegistryProxy artRegistryProxy = null;
    private ExecutionEngineStatus engineStatus = null;
    private EntityName regEntityName = null;
    private RegistryUpdateDeamon registryUpdateDeamon = null;

    public RmiExecutionEngine(PlanSessionManagerInterface sessionManagerInterface,
                              PlanSessionStatusManagerInterface statusManagerInterface,
                              PlanSessionReportManagerInterface reportManagerInterface,
                              PlanSessionStatisticsManagerInterface statisticsManagerInterface,
                              ClockTickManagerInterface clockTickManager, EntityName regEntityName,
                              ExecutionEngineStatus engineStatus) throws RemoteException {
        super(NetUtil.getIPv4() + "_execution_" + UUID.randomUUID().toString());

        this.ip = NetUtil.getIPv4();
        this.regEntityName = regEntityName;
        this.artRegistryProxy = ArtRegistryLocator.getArtRegistryProxy();
        this.engineStatus = engineStatus;

        this.sessionStatusManager =
                new RmiPlanSessionStatusManager(statusManagerInterface, regEntityName);

        this.sessionReportManager =
                new RmiPlanSessionReportManager(reportManagerInterface, regEntityName);

        this.sessionStatisticsManager =
                new RmiPlanSessionStatisticsManager(statisticsManagerInterface, regEntityName);

        this.sessionManager =
                new RmiPlanSessionManager(sessionManagerInterface, regEntityName, artRegistryProxy);

        this.clockTickManager =
                new RmiClockTickManager(clockTickManager, regEntityName, artRegistryProxy);

        ExecutionEngineManagement engineManager = new ExecutionEngineManagement(this);
        ManagementUtil.registerMBean(engineManager, "ExecutionEngine");

        long lifeTime = AdpProperties.getArtProps()
                .getLong("art.executionEngine.rmi.RmiExecutionEngine.lifetime");
        registryUpdateDeamon =
                RegistryUpdateDeamonFactory.createDeamon(this.createProxy(), (long) (0.75 * lifeTime));
        if(lifeTime != 0) {
            registryUpdateDeamon.startDeamon();
        }
    }

    @Override
    public PlanSessionID createNewSession() throws RemoteException {
        return sessionManager.createNewSession();
    }

    @Override
    public PlanSessionID createNewSessionElasticTree() throws RemoteException {
        return sessionManager.createNewSessionElasticTree();
    }

    @Override
    public void destroySession(PlanSessionID sessionID) throws RemoteException {
        sessionManager.destroySession(sessionID);
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
        sessionManager.destroyAllSessions();
    }

    @Override
    public final ExecutionEngineProxy createProxy() throws RemoteException {
        super.register();
        return new RmiExecutionEngineProxy(ip, super.getRegEntryName(), regEntityName);
    }

    @Override
    public ExecutionEngineStatus getStatus() throws RemoteException {
        return engineStatus;
    }

    @Override
    public PlanSessionStatusManagerProxy getPlanSessionStatusManagerProxy(
            PlanSessionID sessionPublicID) throws RemoteException {
        RmiPlanSessionStatusManagerProxy proxy =
                (RmiPlanSessionStatusManagerProxy) sessionStatusManager.createProxy();
        proxy.sessionID = sessionPublicID;
        return proxy;
    }

    @Override
    public PlanSessionReportManagerProxy getPlanSessionReportManagerProxy(
            PlanSessionReportID sessionPrivateID) throws RemoteException {
        RmiPlanSessionReportManagerProxy proxy =
                (RmiPlanSessionReportManagerProxy) sessionReportManager.createProxy();
        proxy.internalSessionID = sessionPrivateID;
        return proxy;
    }

    @Override
    public PlanSessionManagerProxy getPlanSessionManagerProxy(PlanSessionID sessionID)
            throws RemoteException {
        RmiPlanSessionManagerProxy proxy =
                (RmiPlanSessionManagerProxy) sessionManager.createProxy();
        proxy.sessionID = sessionID;
        return proxy;
    }

    @Override
    public PlanSessionStatisticsManagerProxy getPlanSessionStatisticsManagerProxy(
            PlanSessionID sessionID) throws RemoteException {
        RmiPlanSessionStatisticsManagerProxy proxy =
                (RmiPlanSessionStatisticsManagerProxy) sessionStatisticsManager.createProxy();
        proxy.sessionID = sessionID;
        return proxy;
    }

    @Override
    public ClockTickManagerProxy getClockTickManagerProxy() throws RemoteException {
        RmiClockTickManagerProxy proxy = (RmiClockTickManagerProxy) clockTickManager.createProxy();
        return proxy;
    }

    @Override
    public void stopExecutionEngine() throws RemoteException {
        stopExecutionEngine(false);
    }

    @Override
    public void stopExecutionEngine(boolean force) throws RemoteException {
        try {
            sessionManager.destroyAllSessions();
            sessionManager.stopManager(force);
            registryUpdateDeamon.stopDeamon();
            super.unregister();
            ManagementUtil.unregisterMBean(BEAN_NAME);
            log.info("Execution engine stopped!");
        } catch (Exception e) {
            throw new ServerException("Cannot stop execution engine", e);
        }
    }
}
