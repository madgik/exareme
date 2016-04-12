/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.client.rmi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import madgik.exareme.common.app.engine.AdpDBQueryID;
import madgik.exareme.common.app.engine.AdpDBQueryListener;
import madgik.exareme.common.app.engine.AdpDBStatus;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.AdpDBConnector;
import madgik.exareme.master.connector.AdpDBConnectorFactory;
import madgik.exareme.master.engine.AdpDBExecutor;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBOptimizer;
import madgik.exareme.master.engine.AdpDBQueryExecutionPlan;
import madgik.exareme.master.engine.dflSegment.Segment;
import madgik.exareme.master.engine.executor.remote.AdpDBArtJobMonitor;
import madgik.exareme.master.engine.historicalData.AdpDBHistoricalQueryData;
import madgik.exareme.master.engine.parser.AdpDBParser;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.ExportToDotty;
import madgik.exareme.master.queryProcessor.graph.Link;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionPlan.EditableExecutionPlanImpl;
import madgik.exareme.worker.art.executionPlan.parser.expression.*;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

/**
 * Exareme Database Client.
 * + Thread-safe
 * <p/>
 * TODOs
 * - check parser, optimizer, executor for thread-safety (looks ok)
 * - make history as a service (thread-safe)
 * - merge history and registry
 * - impl readTable
 */
public class RmiAdpDBClient implements AdpDBClient {
    private static Logger log = Logger.getLogger(RmiAdpDBClient.class);

    // Remote
    private final AdpDBOptimizer optimizer;
    private final AdpDBExecutor executor;

    // Local
    private AdpDBParser parser;
    private Registry registry;

    // Properties
    private AdpDBClientProperties properties;

    public RmiAdpDBClient(AdpDBManager manager, AdpDBClientProperties properties)
        throws RemoteException {
        this.optimizer = manager == null ? null : manager.getAdpDBOptimizer();
        this.executor = manager == null ? null : manager.getAdpDBExecutor();
        this.properties = properties;
        this.registry = Registry.getInstance(properties.getDatabase());
        this.parser = new AdpDBParser(properties);
    }

    @Override public String explain(String queryScript, String exportMode) throws RemoteException {
        log.trace("Explain...");
        if (exportMode == null) {
            return explainJSON(queryScript);
        }
        if (exportMode.equalsIgnoreCase("dotty")) {
            return explainDotty(queryScript);
        }
        if (exportMode.equals("JSON")) {
            return explainJSON(queryScript);
        }
        if (exportMode.equals("viz")) {
            return explainViz(queryScript);
        }

        return explainJSON(queryScript);
    }

    private String explainViz(String queryScript) throws RemoteException {
        AdpDBQueryID queryId = createNewQueryID();
        QueryScript script = parser.parse(queryScript, registry);
        log.trace("QueryScript parsed.");

        // optimize
        AdpDBHistoricalQueryData queryData = null;
        AdpDBQueryExecutionPlan plan = optimizer
            .optimize(script, registry, null, queryData, queryId, properties, true  /* schedule */,
                true  /* validate */);

        PlanExpression execPlan = executor.getExecPlan(plan, properties);

        Integer countrepart = 0;



        for (ConcreteOperator op : plan.getGraph().getOperators()) {
            boolean isrep = true;
            if (plan.getGraph().getOutputLinks(op.opID).size() == 4) {
                for (Link link : plan.getGraph().getOutputLinks(op.opID)) {
                    if (plan.getGraph().getInputLinks(link.to.opID).size() < 4) {
                        isrep = false;
                    }
                }
                if (isrep) {
                    countrepart++;
                }

            }
        }
        countrepart = countrepart / 4;
        StringBuilder Json = new StringBuilder();
        Json.append("<html>\n" +
            "<head>\n" +
            "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/vis/4.8.2/vis.min.js\"></script>\n"
            +
            "    <link href=\"https://cdnjs.cloudflare.com/ajax/libs/vis/4.8.2/vis.min.css\" rel=\"stylesheet\" type=\"text/css\" />\n"
            +
            "\n" +
            "    <style type=\"text/css\">\n" +
            "        #mynetwork {\n" +
            "            width: 1000;\n" +
            "            height: 800px;\n" +
            "            border: 1px solid lightgray;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" + countrepart.toString() +

            "<div id=\"mynetwork\"></div>\n" +
            "\n" +
            "<script type=\"text/javascript\">");



        StringBuilder nodes = new StringBuilder();
        StringBuilder edges = new StringBuilder();
        nodes.append(" var nodes = new vis.DataSet([\n");
        edges.append(" var edges = new vis.DataSet([\n");
        HashMap<String, Color> contToColor = new HashMap<>();
        int countcont = execPlan.getContainerList().size();

        for (madgik.exareme.worker.art.executionPlan.parser.expression.Container cont : execPlan
            .getContainerList()) {
            Random rand;
            float r, g, b;
            Color randomColor;
            rand = new Random();
            r = rand.nextFloat();
            g = rand.nextFloat();
            b = rand.nextFloat();
            randomColor = new Color(r, g, b);
            contToColor.put(cont.name, randomColor);
        }
        Color randomColor;
        int id = 1;
        Map<String, Integer> opidToOp = new HashMap<>();
        for (Operator op : execPlan.getOperatorList()) {
            if (id > 1) {
                nodes.append(",");
            }
            randomColor = contToColor.get(op.containerName);
            nodes.append("\n{id: ").append(id).append(", label: \'").append(op.operatorName)
                .append("\'").append(",color: ").append(
                "\'rgb(" + randomColor.getRed() + "," + randomColor.getGreen() + "," + randomColor
                    .getBlue() + ")\'").append("}");

            opidToOp.put(op.operatorName, id);
            id++;
        }



        int count = 0;
        for (ConcreteOperator op : plan.getGraph().getOperators()) {
            if (plan.getGraph().getOutputLinks(op.opID).size() == 4) {
                countrepart++;
            }
            for (Link link : plan.getGraph().getOutputLinks(op.opID)) {

                // for(Link link :links){
                if (count > 0) {
                    edges.append(",");
                }
                String to = plan.getGraph().getOperator(link.to.opID).getName();
                String from = plan.getGraph().getOperator(link.from.opID).getName();
                ;
                edges.append("\n{from: ").append(opidToOp.get(from)).append(", to: ")
                    .append(opidToOp.get(to)).append("}");
                count++;
                //  }

            }
        }

        nodes.append("]);\n");
        edges.append(" ]);\n");

        Json.append(nodes.toString()).append(edges.toString());

        Json.append("var container = document.getElementById(\'mynetwork\');")
            .append("var data = {\n" +
                "        nodes: nodes,\n" +
                "        edges: edges\n" +
                "    };\n" +
                "    var options = {\n" +
                " layout: { " +
                "hierarchical: {" +
                "   sortMethod: \"directed\" " +
                "}" +
                "      }," +
                "        edges:{\n" +
                "        arrows: {\n" +
                "          to:     {enabled: true, scaleFactor:1},\n" +
                "        }\n" +
                "    }\n" +
                "    };\n").append("var network = new vis.Network(container, data, options);\n");

        Json.append("</script>\n" +
            "   \n" +
            "</body>\n" +
            "</html>");
        return Json.toString();

    }

    private String explainJSON(String queryScript) throws RemoteException {
        log.trace("JSON...");
        // parse
        AdpDBQueryID queryId = createNewQueryID();
        QueryScript script = parser.parse(queryScript, registry);
        log.trace("QueryScript parsed.");

        // optimize
        AdpDBHistoricalQueryData queryData = null;
        AdpDBQueryExecutionPlan plan = optimizer
            .optimize(script, registry, null, queryData, queryId, properties, true  /* schedule */,
                true  /* validate */);
        log.trace("Optimized.");



        return executor.getJSONPlan(plan, properties);

    }

    private String explainDotty(String queryScript) throws RemoteException {
        log.trace("Dotty...");
        // parse
        AdpDBQueryID queryID = createNewQueryID();
        QueryScript qScript = parser.parse(queryScript, registry);

        // optimize
        AdpDBHistoricalQueryData queryData = null;
        AdpDBQueryExecutionPlan plan = optimizer
            .optimize(qScript, registry, null, queryData, queryID, properties, true  /* schedule */,
                true  /* validate */);

        return ExportToDotty.exportToDotty(plan.getGraph());
    }

    @Override public InputStream readTable(String tableName) throws RemoteException {
        log.debug("readTable");
        HashMap<String, Object> additionalProps = new HashMap<String, Object>();
        additionalProps.put("time", -1);
        additionalProps.put("errors", new ArrayList<Object>());
        AdpDBConnector adpDBConnector = AdpDBConnectorFactory.createAdpDBConnector();
        log.debug("new connector");
        return adpDBConnector.readTable(tableName, additionalProps, properties);
    }


    @Override public AdpDBClientQueryStatus query(String queryID, String queryScript)
        throws RemoteException {

        // parse
        AdpDBQueryID queryId = createNewQueryID();
        QueryScript script = parser.parse(queryScript, registry);
        log.trace("QueryScript parsed.");

        // optimize
        AdpDBHistoricalQueryData queryData = null;
        AdpDBQueryExecutionPlan plan = optimizer
            .optimize(script, registry, null, queryData, queryId, properties, true  /* schedule */,
                true  /* validate */);
        log.trace("Optimized.");

        // execute
        AdpDBStatus status = executor.executeScript(plan, properties);
        return new RmiAdpDBClientQueryStatus(queryId, properties, plan, status);
    }

    @Override public AdpDBClientQueryStatus query(String queryID, QueryScript script) throws RemoteException {
        AdpDBQueryExecutionPlan plan;
        AdpDBQueryID queryId = createNewQueryID();

        // optimize
        plan = optimizer.optimize(script, registry, null, null, queryId, properties,
                true  /* schedule */, true  /* validate */);
        log.trace("Optimized" + plan.toString());

        // execute
        AdpDBStatus status = executor.executeScript(plan, properties);
        return new RmiAdpDBClientQueryStatus(queryId, properties, plan, status);

    }

    @Override public AdpDBClientQueryStatus iquery(String queryID, String queryScript) throws RemoteException {
        EditableExecutionPlanImpl editablePlan = new EditableExecutionPlanImpl();
        List<String> containers = new ArrayList<String>();
        ContainerProxy container[] = ArtRegistryLocator.getArtRegistryProxy().getContainers();

        editablePlan.addContainer(new madgik.exareme.worker.art.executionPlan.parser.expression.Container("c" + 0, //name
                container[0].getEntityName().getName(), //IP
                container[0].getEntityName().getPort(),
                container[0].getEntityName().getDataTransferPort()));

        String c = "c" + 0;
        containers.add(c);
        AdpDBClientQueryStatus queryStatus = null;


        // parse
        AdpDBQueryID queryId = createNewQueryID();
        List<Segment> segments = parser.fullParse(queryScript, registry);


        return query(queryID, segments);

    }


    @Override public AdpDBClientQueryStatus query(String queryID, List<Segment> segments) throws RemoteException {
        EditableExecutionPlanImpl editablePlan = new EditableExecutionPlanImpl();
        List<String> containers = new ArrayList<String>();
        ContainerProxy container[] = ArtRegistryLocator.getArtRegistryProxy().getContainers();
        AdpDBQueryID queryId = createNewQueryID();

        editablePlan.addContainer(new Container("c" + 0, //name
                container[0].getEntityName().getName(), //IP
                container[0].getEntityName().getPort(),
                container[0].getEntityName().getDataTransferPort()));

        String c = "c" + 0;
        containers.add(c);
        AdpDBStatus status = null;
        AdpDBQueryExecutionPlan plan = null;

        for(Segment seg : segments) {
            // if script segment then do the same procedure with the simple query
            if (seg.getType().equals("script")){

                // optimize
                AdpDBHistoricalQueryData queryData = null;
                plan = optimizer
                        .optimize(seg.getQueryScript(), registry, null, queryData, queryId, properties,
                                true  /* schedule */,
                                true  /* validate */);
                log.trace("Optimized.");

                // execute
                status = executor.executeScript(plan, properties);
            }
            // whereas on a loop segment you should prepare and emit a doWhile operator which will be responsible in
            // executing its subSegments
            else {
                GsonBuilder gsonBuilder;
                Gson gson;

                gsonBuilder = new GsonBuilder();
                //gsonBuilder.registerTypeAdapter(Segment.class, new SegmentSerialiser());
                gsonBuilder.setPrettyPrinting();
                gson = gsonBuilder.create();
                LinkedList<Parameter> parameters = new LinkedList<>();
                parameters.add(new Parameter("time", "1"));
                parameters.add(new Parameter("memoryPercentage", "1"));
                //TODO how to pass database
                parameters.add(new Parameter("database", properties.getDatabase()));
                //parameters.add(new Parameter("Segment", JsonBuilder.toJson(seg)));
                parameters.add(new Parameter("whileScript", gson.toJson(seg.getQueryScript())));
                parameters.add(new Parameter("SubSegments", gson.toJson(seg.getSubSegments())));


                Map<String, LinkedList<Parameter>> links = new HashMap<>();
                // plan.addOperator(new Operator())
                editablePlan.addOperator(new Operator(
                                "sample1",
                                "madgik.exareme.master.engine.executor.remote.operator.control.DoWhile",
                                parameters,
                                null,
                                String.format("c%1d",0),
                                links
                        )
                );

                Map<AdpDBQueryID, AdpDBArtJobMonitor> monitors = new HashMap<>();
                try {
                    gson = new Gson();
                    log.info("PLAN: " + gson.toJson(editablePlan));

                    ExecutionEngineProxy engineProxy = ExecutionEngineLocator.getExecutionEngineProxy();
                    ExecutionEngineSession engineSession = engineProxy.createSession();
                    final ExecutionEngineSessionPlan sessionPlan = engineSession.startSession();
                    sessionPlan.submitPlan(editablePlan);

                    log.info("Submitted.");
                    while (sessionPlan.getPlanSessionStatusManagerProxy().hasFinished() == false
                            && sessionPlan.getPlanSessionStatusManagerProxy().hasError() == false) {
                        Thread.sleep(100);
                    }
                    log.info("Exited");
                    if (sessionPlan.getPlanSessionStatusManagerProxy().hasError() == true) {
                        log.error(sessionPlan.getPlanSessionStatusManagerProxy().getErrorList().get(0));
                    }

                } catch (Exception e) {
                    log.error(e);
                }

            }
        }

        // TODO this keeps only the result of the last script segment
        return new RmiAdpDBClientQueryStatus(queryId, properties, plan, status);

    }


    @Override public AdpDBClientQueryStatus aquery(String queryID, String queryScript,
        AdpDBQueryListener listener) throws RemoteException {
        AdpDBClientQueryStatus queryStatus = query(queryID, queryScript);
        executor.registerListener(listener, queryStatus.getQueryID());
        return queryStatus;
    }

    private List<EntityName> checkContainers() throws RemoteException {
        // TODO can this check moved to engine ?
        log.trace("Checking containers status...");
        List<EntityName> faultyContainers = new ArrayList<>();
        ContainerProxy containers[] = ArtRegistryLocator.getArtRegistryProxy().getContainers();
        for (int i = 0; i < containers.length; ++i) {
            ContainerProxy container = containers[i];
            try {
                log.trace("Container status: " + (container.connect().execJobs(new ContainerJobs())
                    != null));
            } catch (Exception e) {
                log.error(e);
                log.debug("Removing container: " + container.getEntityName());
                faultyContainers.add(container.getEntityName());
                ArtRegistryLocator.getArtRegistryProxy().removeContainer(container.getEntityName());
            }
        }

        return faultyContainers.isEmpty() ? null : faultyContainers;


    }

    private AdpDBQueryID createNewQueryID() {
        return new AdpDBQueryID(UUID.randomUUID().getLeastSignificantBits());
    }
}
