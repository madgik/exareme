/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.gateway;

import com.google.gson.Gson;

import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.properties.GenericProperties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

/**
 * @author heraldkllapi
 */
public class ExaremeGatewayUtils {
  // gateway
  public static final String GW_MODE;
  public static final String GW_LOG_LEVEL;
  // api
  public static final String GW_API_QUERY;
  public static final String GW_API_DROP_TABLE;
  public static final String GW_API_HISTORY;
  public static final String GW_API_FILE;
  public static final String GW_API_TABLE;
  public static final String GW_API_DECOMPOSER;
  public static final String GW_API_EXPLAINQUERY;
  public static final String GW_API_TABLESMETADATA;
  public static final String GW_API_STREAMQUERY_RESULT;
  public static final String GW_API_STREAMQUERY_DELETE;
  public static final String GW_API_STREAMQUERY;
  public static final String GW_API_HISTORICALSTREAMQUERY;
  public static final String GW_API_STREAMQUERY_INFO;
  public static final String GW_API_MINING_ALGORITHMS;
  public static final String GW_API_MINING_QUERY;


  // server
  public static final int GW_PORT;
  public static final int GW_BUFFERSIZE_KB;
  public static final boolean GW_NODELAY;
  public static final int GW_WAIT_TERM_SEC;
  public static final int GW_THREADS;
  public static final String GW_REGISTRY_PATH;
  // request params
  public static final String REQUEST_USER;
  public static final String REQUEST_DATABASE;
  public static final String REQUEST_EXPLAINMODE;
  public static final String REQUEST_QUERY;
  public static final String REQUEST_TABLE;
  public static final String REQUEST_NO_TABLE;
  public static final String REQUEST_USETREE;
  public static final String REQUEST_USEHISTORY;
  public static final String REQUEST_MAXCONTAINERS;
  public static final String REQUEST_STREAMQUERY;
  // response params
  public static final String RESPONSE_SCHEMA;
  public static final String RESPONSE_TIME;
  public static final String RESPONSE_ERRORS;
  // context
  public static final String CONTEXT_DB_CLIENT;
  public static final String CONTEXT_DB_CONNECTOR;
  private static final Logger log = Logger.getLogger(ExaremeGatewayUtils.class);
  public static final String REQUEST_TIMEOUT;

  static {
    GenericProperties properties = AdpProperties.getGatewayProperties();
    GW_LOG_LEVEL = properties.getString("gateway.logLevel");
    log.setLevel(Level.toLevel(GW_LOG_LEVEL));
    log.trace("Gateway configuration :");

    GW_MODE = properties.getString("gateway.mode");
    GW_PORT = properties.getInt("gateway.port");
    GW_BUFFERSIZE_KB = properties.getInt("gateway.bufferSizeKB");
    GW_NODELAY = properties.getBoolean("gateway.noDelay");
    GW_WAIT_TERM_SEC = properties.getInt("gateway.waitForTerminationSec");
    GW_THREADS = properties.getInt("gateway.threads");
    GW_REGISTRY_PATH = properties.getString("gateway.registry.path");

    GW_API_QUERY = properties.getString("gateway.api.query");
    GW_API_HISTORY = properties.getString("gateway.api.history");
    GW_API_FILE = properties.getString("gateway.api.file");
    GW_API_TABLE = properties.getString("gateway.api.table");
    GW_API_DROP_TABLE = properties.getString("gateway.api.dropTable");
    GW_API_DECOMPOSER = properties.getString("gateway.api.decomposer");
    GW_API_EXPLAINQUERY = properties.getString("gateway.api.explainQuery");
    GW_API_STREAMQUERY = properties.getString("gateway.api.streamquery");
    GW_API_HISTORICALSTREAMQUERY = properties.getString("gateway.api.historicalstreamquery");
    GW_API_STREAMQUERY_RESULT = properties.getString("gateway.api.streamquery.result");
    GW_API_STREAMQUERY_DELETE = properties.getString("gateway.api.streamquery.delete");
    GW_API_STREAMQUERY_INFO = properties.getString("gateway.api.streamquery.info");
    GW_API_TABLESMETADATA = properties.getString("gateway.api.tablesmetadata");
    GW_API_MINING_ALGORITHMS = properties.getString("gateway.api.mining.algorithms");
    GW_API_MINING_QUERY= properties.getString("gateway.api.mining.query");


    CONTEXT_DB_CLIENT = properties.getString("gateway.context.db.client");
    CONTEXT_DB_CONNECTOR = properties.getString("gateway.context.db.connector");

    REQUEST_USER = properties.getString("gateway.request.user");
    REQUEST_DATABASE = properties.getString("gateway.request.database");
    REQUEST_EXPLAINMODE = properties.getString("gateway.request.explainMode");
    REQUEST_QUERY = properties.getString("gateway.request.query");
    REQUEST_TABLE = properties.getString("gateway.request.table");
    REQUEST_NO_TABLE = properties.getString("gateway.request.notable");
    REQUEST_USETREE = properties.getString("gateway.request.usetree");
    REQUEST_USEHISTORY = properties.getString("gateway.request.usehistory");
    REQUEST_MAXCONTAINERS = properties.getString("gateway.request.maxcontainers");
    REQUEST_STREAMQUERY = properties.getString("gateway.request.streamquery");

    RESPONSE_SCHEMA = properties.getString("gateway.response.schema");
    RESPONSE_TIME = properties.getString("gateway.response.time");
    RESPONSE_ERRORS = properties.getString("gateway.response.errors");
    
    REQUEST_TIMEOUT = properties.getString("gateway.request.timeout");

    log.trace("Gateway mode         :" + GW_MODE);
    log.trace("Listening port       :" + String.valueOf(GW_PORT));
    log.trace("Size of Buffer in KB :" + String.valueOf(GW_BUFFERSIZE_KB));
    log.trace("TCP no delay         :" + String.valueOf(GW_NODELAY));
    log.trace("Shutdown wait in secs:" + String.valueOf(GW_WAIT_TERM_SEC));
    log.trace("API Query            :" + GW_API_QUERY);
    log.trace("Request database     :" + REQUEST_DATABASE);
    log.trace("Request query        :" + REQUEST_QUERY);
    log.trace("Request user         :" + REQUEST_USER);
    log.trace("Response schema      :" + RESPONSE_SCHEMA);
    log.trace("Response time        :" + RESPONSE_TIME);

    log.trace("Default properties successfully initialized.");
  }


  public static void getValues(String content, Map<String, String> dict)
      throws UnsupportedEncodingException {
    if (!content.isEmpty()) {
      try {
        getValuesFromJDBC(content, dict);
      } catch (Exception e) {
          getValuesFromWeb(content, dict);
      }
    }
  }

  private static void getValuesFromJDBC(String content, Map<String, String> dict)
      throws UnsupportedEncodingException {
    Gson g = new Gson();
    Map<String, String> values = g.fromJson(content, Map.class);
    dict.putAll(values);
  }

  private static void getValuesFromWeb(String content, Map<String, String> dict)
      throws UnsupportedEncodingException {
    String[] parts = content.split("&");
    for (String p : parts) {
      int split = p.indexOf("=");
      if (split == -1) return;
      String key = p.substring(0, split);
      String value = p.substring(split + 1, p.length());
      dict.put(key, normalize(value));
    }
  }
  public static void getValuesFromJson(String content, Map<String, String> dict) throws UnsupportedEncodingException {
    Gson g = new Gson();
    List<Map> parameters = new Gson().fromJson(content, List.class);
    for (Map parameter : parameters) {
      dict.put((String)parameter.get("name"), (String)parameter.get("value"));
    }
  }

  private static String normalize(String in) throws UnsupportedEncodingException {
    return URLDecoder.decode(in, "UTF-8");
  }
}
