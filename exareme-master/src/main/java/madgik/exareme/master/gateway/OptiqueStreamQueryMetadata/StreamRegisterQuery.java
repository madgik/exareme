package madgik.exareme.master.gateway.OptiqueStreamQueryMetadata;

import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.streamClient.AdpStreamDBClient;
import madgik.exareme.master.streamClient.AdpStreamDBClientFactory;
import madgik.exareme.utils.association.SimplePair;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * @author Christoforos Svingos
 */
public class StreamRegisterQuery {

    public static class QueryInfo {
        public String sqlQuery;
        public String registerDate;
        public int port;
        public String ip;

        private static final SimpleDateFormat dateParser;

        static {
            dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            dateParser.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public QueryInfo(String sqlQuery, String ip, int port) {
            this.sqlQuery = sqlQuery;
            this.registerDate = dateParser.format(new Date());
            this.ip = ip;
            this.port = port;
        }
    }


    private static final HashMap<String, QueryInfo> streamQueryMap;
    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();

    private static StreamRegisterQuery instance = null;
    private static final Logger log = Logger.getLogger(StreamRegisterQuery.class);

    static {
        RegistryDB.createSchema();
        streamQueryMap = new HashMap<String, QueryInfo>();
        for (SimplePair<String, String> pair : RegistryDB.getRegisterQueries()) {
            try {
                StreamRegisterQuery.add(pair.first, pair.second);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected StreamRegisterQuery() {
        // Exists only to defeat instantiation.
    }

    public static StreamRegisterQuery getInstance() {
        if (instance == null) {
            instance = new StreamRegisterQuery();
        }

        return instance;
    }

    public static synchronized boolean add(String streamName, String sqlQuery)
        throws RemoteException {
        if (streamQueryMap.containsKey(streamName)) {
            throw new RemoteException("Query with name: " + streamName + " is already registered");
        }

        log.info("The stream register quesry is: " + sqlQuery);

        String dbname = "/tmp/random-db";
        AdpStreamDBClient dbClient = AdpStreamDBClientFactory
            .createOptiqueStreamDBClient(manager, new AdpDBClientProperties(dbname));
        QueryInfo info = dbClient.query("noid", sqlQuery);
        streamQueryMap.put(streamName, info);

        RegistryDB.addToRegistry(streamName, sqlQuery);

        return true;
    }

    public static synchronized QueryInfo get(String streamName) {
        return streamQueryMap.get(streamName);
    }

    // TODO: Delete Request at the host machine
    public static synchronized boolean remove(String streamName) {
        QueryInfo info = streamQueryMap.remove(streamName);
        RegistryDB.removeFromRegistry(streamName);

        return true;
    }

    public static synchronized int size() {
        return streamQueryMap.size();
    }

    public static synchronized int clear() {
        for (String key : streamQueryMap.keySet()) {
            remove(key);
        }

        return streamQueryMap.size();
    }

    public static synchronized Map<String, Map<String, String>> getQueriesInfo() {
        Map<String, Map<String, String>> streams = new HashMap<String, Map<String, String>>();
        for (String key : streamQueryMap.keySet()) {
            QueryInfo info = streamQueryMap.get(key);
            Map<String, String> dict = new HashMap<String, String>();
            dict.put("Query", info.sqlQuery);
            dict.put("Register Date", info.registerDate);
            streams.put(key, dict);
        }

        return streams;
    }

    // TODO(XRS): Limited Ports
    public static synchronized int getUnusedPort(String ip) {
        boolean find = true;
        Random rand = new Random();
        int port;
        for (port = (rand.nextInt(50534) + 10001); port > 10000; --port) {
            find = true;
            for (QueryInfo info : streamQueryMap.values()) {
                if (info.ip.equals(ip) && info.port == port) {
                    find = false;
                    break;
                }
            }

            if (find) {
                return port;
            }
        }

        return -1;
    }
}
