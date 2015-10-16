/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.streamClient.rmi;

import com.google.gson.Gson;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBExecutor;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.gateway.OptiqueStreamQueryMetadata.StreamRegisterQuery;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.master.streamClient.AdpStreamDBClient;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionPlan.EditableExecutionPlanImpl;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;
import madgik.exareme.worker.art.executionPlan.parser.expression.Operator;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.*;

/**
 * @author Christoforos Svingos
 */
public class RmiOptiqueStreamAdpDBClient implements AdpStreamDBClient {
    private static Logger log = Logger.getLogger(RmiOptiqueStreamAdpDBClient.class);
    private static int containerNumber = 0;

    // Remote
    private final AdpDBExecutor executor;

    // Local
    private Registry registry;

    // Properties
    private AdpDBClientProperties properties;

    public RmiOptiqueStreamAdpDBClient(AdpDBManager manager, AdpDBClientProperties properties)
        throws RemoteException {
        this.executor = manager.getAdpDBExecutor();
        this.properties = properties;
        this.registry = Registry.getInstance(properties.getDatabase());
    }

    @Override public String explain(String queryScript) throws RemoteException {
        // Nothing
        return null;
    }

    @Override public StreamRegisterQuery.QueryInfo query(String queryID, String queryScript)
        throws RemoteException {

        EditableExecutionPlanImpl plan = new EditableExecutionPlanImpl();
        List<String> containers = new ArrayList<String>();

        ContainerProxy container[] = ArtRegistryLocator.getArtRegistryProxy().getContainers();
        containerNumber = (containerNumber + 1) % container.length;
        log.info("ConatainerNumber: " + containerNumber);

        String containerIp = container[containerNumber].getEntityName().getIP();
        StreamRegisterQuery.QueryInfo info =
            new StreamRegisterQuery.QueryInfo(queryScript, containerIp,
                StreamRegisterQuery.getInstance().getUnusedPort(containerIp));

        plan.addContainer(new Container("c" + containerNumber, //name
            container[containerNumber].getEntityName().getName(), //IP
            container[containerNumber].getEntityName().getPort(),
            container[containerNumber].getEntityName().getDataTransferPort())); //[JV] TOC: port
        String c = "c" + containerNumber;
        containers.add(c);

        LinkedList<Parameter> parameters = new LinkedList<>();
        parameters.add(new Parameter("time", "1"));
        parameters.add(new Parameter("memoryPercentage", "1"));
        parameters.add(new Parameter("port", String.valueOf(info.port)));
        parameters.add(new Parameter("database", "database"));
        Map<String, LinkedList<Parameter>> links = new HashMap<>();
        plan.addOperator(new Operator("sample1",
            "madgik.exareme.master.engine.executor.remote.operator.OptiqueStreamQueryEndPoint.ExecuteStreamQueryOperator",
            parameters, queryScript, String.format("c%1d", containerNumber), links));

        try {
            Gson gson = new Gson();
            log.info("PLAN: " + gson.toJson(plan));

            ExecutionEngineProxy engineProxy = ExecutionEngineLocator.getExecutionEngineProxy();
            ExecutionEngineSession engineSession = engineProxy.createSession();
            final ExecutionEngineSessionPlan sessionPlan = engineSession.startSession();
            sessionPlan.submitPlan(plan);
            log.info("Submitted !");
        } catch (Exception e) {
            log.error(e);
        }

        return info;
    }
}
