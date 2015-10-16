/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.connector;

import com.google.gson.Gson;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.common.schema.Partition;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.master.engine.parser.SemanticException;
import madgik.exareme.master.engine.util.SchemaUtil;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.embedded.db.DBUtils;
import madgik.exareme.utils.embedded.db.SQLDatabase;
import madgik.exareme.utils.properties.AdpDBProperties;
import madgik.exareme.utils.stream.StreamUtil;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.container.buffer.SocketBuffer;
import madgik.exareme.worker.art.container.buffer.tcp.TcpSocketBuffer;
import madgik.exareme.worker.art.container.netMgr.NetSession;
import madgik.exareme.worker.art.container.netMgr.session.NetSessionSimple;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineLocator;
import madgik.exareme.worker.art.executionEngine.ExecutionEngineProxy;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSessionPlan;
import madgik.exareme.worker.art.executionEngine.statusMgr.PlanSessionStatusManagerProxy;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanParser;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.sql.ResultSet;
import java.util.*;

/**
 * @author heraldkllapi
 */
public class AdpDBConnectorUtil {

    private static Logger log = Logger.getLogger(AdpDBConnectorUtil.class);

    public static void readRemoteTablePart(Registry registry, PhysicalTable table, Partition p,
        Map<String, Object> includeProps, OutputStream out) throws RemoteException {
        log.info("Remote Table Part: " + p.getTable() + "." + p.getpNum() + " ...");
        ExecutionEngineProxy engine = ExecutionEngineLocator.getExecutionEngineProxy();
        ContainerProxy[] containerProxies =
            ArtRegistryLocator.getArtRegistryProxy().getContainers();
        BitSet filter = new BitSet(containerProxies.length);
        SchemaUtil.getLocationsOfPartitions(containerProxies, table, p.getpNum(), filter);
        int[] locations = new int[filter.cardinality()];
        if (locations.length == 0) {
            throw new SemanticException("Partition not found: " + table.getName() + "/" + p);
        }
        int l = 0;
        for (int i = filter.nextSetBit(0); i >= 0; i = filter.nextSetBit(i + 1)) {
            locations[l++] = i;
        }
        log.debug(
            "Table '" + table.getName() + "/" + p + "' located at " + Arrays.toString(locations));

        SocketBuffer socketBuffer = new TcpSocketBuffer();
        EntityName name = socketBuffer.getNetEntityName();

        ContainerProxy proxy = containerProxies[locations[0]];
        boolean sendHeader = p.getpNum() == 0;

        //        String artPlan = "container c('" + proxy.getEntityName().getName() + "', 1000); \n"
        //            + "operator op c('AdpDBNetReaderOperator', " + "database='" + registry.getDatabase()
        //            + "', " + "table='" + table.getName() + "', " + "part='" + p.getpNum() + "', "
        //            + "sendHeader='" + sendHeader + "', " + "ip='" + name.getIP() + "', " + "port='" + name
        //            .getPort() + "');";

        String artPlan = "{\n" +
            "  \"containers\": [\n" +
            "    {\n" +
            "      \"name\": \"c\",\n" +
            "      \"IP\":" + "\"" + proxy.getEntityName().getName() + "\"" + ",\n" +
            "      \"port\": \"1000\",\n" +
            "      \"data_transfer_port\": \"1000\"\n" +
            "    }],\n" +
            "  \"operators\": [\n" +
            "    {\n" +
            "      \"name\": \"op\",\n" +
            "      \"container\": \"c\",\n" +
            "      \"operator\": \"madgik.exareme.master.engine.executor.remote.operator.admin.AdpDBNetReaderOperator\",\n"
            +
            "       \"parameters\": [\n" +
            "        [\n" +
            "          \"database\",\n" +
            "          \"" + new File(registry.getDatabase()).getParent() + "\"\n" +
            "        ],\n" +
            "        [\n" +
            "          \"table\",\n" +
            "          \"" + table.getName() + "\"\n" +
            "        ],\n" +
            "        [\n" +
            "          \"part\",\n" +
            "          \"" + p.getpNum() + "\"\n" +
            "        ],\n" +
            "        [\n" +
            "          \"sendHeader\",\n" +
            "          \"" + sendHeader + "\"\n" +
            "        ],\n" +
            "        [\n" +
            "          \"ip\",\n" +
            "          \"" + name.getIP() + "\"\n" +
            "        ],\n" +
            "        [\n" +
            "          \"port\",\n" +
            "          \"" + name.getPort() + "\"\n" +
            "        ]\n" +
            "       ]\n" +
            "    }]\n" +
            "}";
        log.debug("Executing... \n" + artPlan);
        ExecutionPlan plan = null;
        try {
            ExecutionPlanParser parser = new ExecutionPlanParser();
            plan = parser.parse(artPlan.toString());
        } catch (Exception e) {
            throw new ServerException("Cannot parse generated plan.", e);
        }
        ExecutionEngineSession session = engine.createSession();
        ExecutionEngineSessionPlan sessionPlan = session.startSession();
        sessionPlan.submitPlan(plan);
        PlanSessionStatusManagerProxy sessionManager =
            sessionPlan.getPlanSessionStatusManagerProxy();
        int waifForMs =
            1000 * AdpDBProperties.getAdpDBProps().getInt("db.client.statisticsUpdate_sec");
        try {
            NetSession net = new NetSessionSimple();
            InputStream inputStream = net.openInputStream(socketBuffer);
            StreamUtil.copyStreams(inputStream, out);
            inputStream.close();
            socketBuffer.close();
        } catch (IOException e) {
            throw new RemoteException("Cannot read table", e);
        }
        while (sessionManager.hasFinished() == false && sessionManager.hasError() == false) {
            try {
                Thread.sleep(waifForMs);
            } catch (Exception e) {
            }
        }
        sessionPlan.close();
        session.close();
    }

    public static void readLocalTablePart(String tabName, int part, String database,
        Map<String, Object> alsoIncludeProps, OutputStream out) throws RemoteException {
        try {
            log.info(
                "Local Table Part: " + tabName + "." + part + " ..." + alsoIncludeProps == null);
            Gson g = new Gson();
            SQLDatabase db =
                DBUtils.createEmbeddedSqliteDB(database + "/" + tabName + "." + part + ".db");
            ResultSet rs = db.executeAndGetResults("select * from " + tabName + ";");
            int cols = rs.getMetaData().getColumnCount();
            if (alsoIncludeProps != null) {
                Map<String, Object> schema = new HashMap<String, Object>();
                schema.putAll(alsoIncludeProps);
                ArrayList<String[]> names = new ArrayList<String[]>();
                schema.put("schema", names);
                for (int c = 0; c < cols; ++c) {
                    names.add(new String[] {rs.getMetaData().getColumnName(c + 1),
                        rs.getMetaData().getColumnTypeName(c + 1)});
                }
                out.write((g.toJson(schema) + "\n").getBytes());
            }
            ArrayList<Object> row = new ArrayList<Object>();
            while (rs.next()) {
                for (int c = 0; c < cols; ++c) {
                    row.add(rs.getObject(c + 1));
                }
                out.write((g.toJson(row) + "\n").getBytes());
                row.clear();
            }
            rs.close();
            db.close();
        } catch (Exception e) {
            throw new RemoteException("Cannot get results", e);
        }
    }
}
