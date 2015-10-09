/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote;

import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBExecutor;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.AdpDBStatusManager;
import madgik.exareme.master.engine.executor.remote.operator.data.*;
import madgik.exareme.utils.properties.AdpDBProperties;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.ExecEngineConstants;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
import madgik.exareme.worker.art.executionPlan.JsonBuilder;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;
import madgik.exareme.worker.art.executionPlan.parser.expression.PlanExpression;
import madgik.exareme.worker.art.executionPlan.parser.expression.Pragma;
import madgik.exareme.worker.art.manager.ArtManager;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author herald
 * @author John Chronis
 * @author Vaggelis Nikolopoulos
 */
public class AdpDBExecutorRemote implements AdpDBExecutor {
    private final static Logger log = Logger.getLogger(AdpDBExecutorRemote.class);

    private static final String execMethod =
        AdpDBProperties.getAdpDBProps().getString("db.execution.method");
    private final Map<AdpDBQueryID, AdpDBArtJobMonitor> monitors;
    private ArtManager manager = null;
    private ExecutionEngineProxy engine = null;
    private AdpDBStatusManager statusManager = null;
    private ExecutorService executor = null;
    private ArrayList<AdpDBStatus> statusArray = null;

    public AdpDBExecutorRemote(AdpDBStatusManager statusMgr, ArtManager mgr)
        throws RemoteException {
        this.manager = mgr;
        this.statusManager = statusMgr;
        this.executor = Executors.newFixedThreadPool(100);
        this.statusArray = new ArrayList<AdpDBStatus>();
        this.monitors = new HashMap<AdpDBQueryID, AdpDBArtJobMonitor>();
        this.engine = ExecutionEngineLocator.getExecutionEngineProxy();
    }


    @Override
    public AdpDBStatus executeScript(AdpDBQueryExecutionPlan execPlan, AdpDBClientProperties props)
        throws RemoteException {
        PlanExpression planExpression = new PlanExpression();
        if (execMethod.equals("optimized")) {
            // Add pragmas
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_MATERIALIZED_BUFFER_READER,
                    MaterializedReader.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_MATERIALIZED_BUFFER_WRITER,
                    MaterializedWriter.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_MEDIATOR_FROM,
                    InterContainerMediatorFrom.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_MEDIATOR_TO,
                    InterContainerMediatorTo.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_DATA_TRANSFER,
                    DataTransferRegister.class.getName()));

            log.debug("Using optimized execution method ... ");
        } else {
            log.debug("Using simple execution method ... ");
        }

        ContainerProxy[] container = execPlan.getContainerProxies();
        List<String> containers = new ArrayList<String>();

        for (int i = 0; i < container.length; ++i) {

            planExpression.addContainer(new Container("c" + i, //name
                container[i].getEntityName().getName(), //IP
                container[i].getEntityName().getPort(),
                container[i].getEntityName().getDataTransferPort())); //[JV] TOC: port
            String c = "c" + i;
            containers.add(c);
        }

        // Add the any containers
        for (int i = 0; i < execPlan.getGraph().getNumOfOperators(); ++i) {
            planExpression.addContainer(new Container("any" + i, //name
                "any" + i, //IP
                1099, 8088)); //[JV] TOC: port
            containers.add("any" + i);
        }
        HashMap<String, String> categoryMessageMap = new HashMap<>();
        try {
            AdpDBArtPlanGenerator
                .generateJsonPlan(containers, execPlan, categoryMessageMap, planExpression, props);
        } catch (Exception e) {
            throw new ServerException("Cannot generate ART plan!", e);
        }


        JsonBuilder jsonBuilder = new JsonBuilder();
        String jsonEP = jsonBuilder.parse(planExpression);
        log.debug("JSON Plan:" + jsonEP);
        ExecutionPlan plan = null;
        try {
            ExecutionPlanParser parser = new ExecutionPlanParser();
            plan = parser.parse(jsonEP);
        } catch (Exception e) {
            throw new ServerException("Cannot parse generated plan.", e);

        }

        ExecutionEngineSession session = engine.createSession();
        ExecutionEngineSessionPlan sessionPlan = null;

        if (props.isTreeEnabled()) {
            sessionPlan = session.startSessionElasticTree();
            sessionPlan.submitPlanElasticTree(plan, props.getSLA());
        } else {
            //TODO check if the following is needed
            sessionPlan = session.startSession();
            sessionPlan.submitPlan(plan);
        }

        AdpDBStatus status =
            statusManager.createNewStatus(execPlan.getQueryID(), sessionPlan, categoryMessageMap);
        AdpDBArtJobMonitor monitor =
            new AdpDBArtJobMonitor(sessionPlan, status, statusManager, execPlan.getQueryID());
        monitors.put(execPlan.getQueryID(), monitor);

        executor.submit(monitor);
        statusArray.add(status);
        return status;
    }


    @Override public void registerListener(AdpDBQueryListener listener, AdpDBQueryID queryID)
        throws RemoteException {
        AdpDBArtJobMonitor monitor = monitors.get(queryID);
        monitor.registerListener(listener);
    }

    @Override public void stop() throws RemoteException {
        for (AdpDBStatus stat : statusArray) {
            if (stat.hasFinished() == false && stat.hasError() == false) {
                log.debug("Stopping session ... ");
                stat.stopExecution();
            }
        }
    }

    @Override
    public String getJSONPlan(AdpDBQueryExecutionPlan execPlan, AdpDBClientProperties props)
        throws RemoteException {
        PlanExpression planExpression = new PlanExpression();
        if (execMethod.equals("optimized")) {
            // Add pragmas
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_MATERIALIZED_BUFFER_READER,
                    MaterializedReader.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_MATERIALIZED_BUFFER_WRITER,
                    MaterializedWriter.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_MEDIATOR_FROM,
                    InterContainerMediatorFrom.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_MEDIATOR_TO,
                    InterContainerMediatorTo.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_DATA_TRANSFER,
                    DataTransferRegister.class.getName()));

            log.debug("Using optimized execution method ... ");
        } else {
            log.debug("Using simple execution method ... ");
        }

        ContainerProxy[] container = execPlan.getContainerProxies();
        List<String> containers = new ArrayList<String>();

        for (int i = 0; i < container.length; ++i) {

            planExpression.addContainer(new Container("c" + i, //name
                container[i].getEntityName().getName(), //IP
                container[i].getEntityName().getPort(),
                container[i].getEntityName().getDataTransferPort())); //[JV] TOC: port
            String c = "c" + i;
            containers.add(c);
        }

        // Add the any containers
        //    for (int i = 0; i < execPlan.getGraph().getNumOfOperators(); ++i) {
        //      planExpression.addContainer(new Container("any" + i, //name
        //          "any" + i, //IP
        //          1099,
        //          8088)); //[JV] TOC: port
        //      containers.add("any" + i);
        //    }
        HashMap<String, String> categoryMessageMap = new HashMap<>();
        try {
            AdpDBArtPlanGenerator
                .generateJsonPlan(containers, execPlan, categoryMessageMap, planExpression, props);
        } catch (Exception e) {
            throw new ServerException("Cannot generate ART plan!", e);
        }

        JsonBuilder jsonBuilder = new JsonBuilder();
        String jsonEP = jsonBuilder.parse(planExpression);

        return jsonEP;
    }

    @Override
    public PlanExpression getExecPlan(AdpDBQueryExecutionPlan execPlan, AdpDBClientProperties props)
        throws RemoteException {
        PlanExpression planExpression = new PlanExpression();
        if (execMethod.equals("optimized")) {
            // Add pragmas
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_MATERIALIZED_BUFFER_READER,
                    MaterializedReader.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_MATERIALIZED_BUFFER_WRITER,
                    MaterializedWriter.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_MEDIATOR_FROM,
                    InterContainerMediatorFrom.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_MEDIATOR_TO,
                    InterContainerMediatorTo.class.getName()));
            planExpression.addPragma(
                new Pragma(ExecEngineConstants.PRAGMA_INTER_CONTAINER_DATA_TRANSFER,
                    DataTransferRegister.class.getName()));

            log.debug("Using optimized execution method ... ");
        } else {
            log.debug("Using simple execution method ... ");
        }

        ContainerProxy[] container = execPlan.getContainerProxies();
        List<String> containers = new ArrayList<String>();

        for (int i = 0; i < container.length; ++i) {

            planExpression.addContainer(new Container("c" + i, //name
                container[i].getEntityName().getName(), //IP
                container[i].getEntityName().getPort(),
                container[i].getEntityName().getDataTransferPort())); //[JV] TOC: port
            String c = "c" + i;
            containers.add(c);
        }

        // Add the any containers
        //    for (int i = 0; i < execPlan.getGraph().getNumOfOperators(); ++i) {
        //      planExpression.addContainer(new Container("any" + i, //name
        //          "any" + i, //IP
        //          1099,
        //          8088)); //[JV] TOC: port
        //      containers.add("any" + i);
        //    }
        HashMap<String, String> categoryMessageMap = new HashMap<>();
        try {
            AdpDBArtPlanGenerator
                .generateJsonPlan(containers, execPlan, categoryMessageMap, planExpression, props);
        } catch (Exception e) {
            throw new ServerException("Cannot generate ART plan!", e);
        }

        JsonBuilder jsonBuilder = new JsonBuilder();
        String jsonEP = jsonBuilder.parse(planExpression);

        return planExpression;
    }
}
