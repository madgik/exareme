package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.async.handler.entity.NQueryStatusEntity;
import org.apache.http.*;
//import org.apache.http.ParseException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.text.*;
import java.text.ParseException;
import java.util.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;

/**
 * Query Handler.
 *
 * @author alex
 * @author Christoforos Svingos
 */

public class HttpAsyncOptiqueHistoricalQueriesHandler
    implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log =
        Logger.getLogger(HttpAsyncOptiqueHistoricalQueriesHandler.class);
    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();
    private static int numberOfMachines;
    
    static {
        try {
            numberOfMachines = ArtRegistryLocator.getArtRegistryProxy().getContainers().length;
            log.info("Number of machines: " + numberOfMachines);
        } catch (RemoteException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public HttpAsyncOptiqueHistoricalQueriesHandler() {
    }

    @Override public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request,
        HttpContext context) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpExchange, HttpContext context)
        throws HttpException, IOException {
        HttpResponse response = httpExchange.getResponse();
        handleInternal(request, response, context);
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(HttpRequest request, HttpResponse response, HttpContext context)
        throws UnsupportedHttpVersionException, IOException {
        String dbname = null;
        String query = null;
        String inputQuery = null;
        String queryName = null;
        try {
            log.info("Validating request ..");
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);

            if (!"POST".equals(method)) {
                throw new UnsupportedHttpVersionException(method + "not supported.");
            }

            String target = request.getRequestLine().getUri();
            queryName = target.substring(target.lastIndexOf('/') + 1);

            // parse content
            String content = "";
            if (request instanceof HttpEntityEnclosingRequest) {
                log.info("Streamming request ...");
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                content = EntityUtils.toString(entity);
            }

            HashMap<String, String> inputContent = new HashMap<>();
            ExaremeGatewayUtils.getValues(content, inputContent);

            dbname = inputContent.get(ExaremeGatewayUtils.REQUEST_DATABASE);
            inputQuery = inputContent.get(ExaremeGatewayUtils.REQUEST_STREAMQUERY);
            log.info("Query : " + inputQuery);
            query = getDflQuery(inputQuery, queryName);
            log.info("Database : " + dbname);
            log.info("Distributed Query : " + query);
        } catch (Exception ex) {
            log.error(ex);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("query", inputQuery);
            errorMap.put("error", ex.getMessage());
            Gson gson = new Gson();
            response.setEntity(new NStringEntity(gson.toJson(errorMap)));
            return;
        }

        AdpDBClientQueryStatus queryStatus = null;
        try {
            AdpDBClientProperties properties =
                new AdpDBClientProperties(dbname, "", "", false, true, -1, 10);
            AdpDBClient dbClient = AdpDBClientFactory.createDBClient(manager, properties);
            queryStatus = dbClient.query("noid", query);
            BasicHttpEntity entity = new NQueryStatusEntity(queryStatus);
            response.setStatusCode(HttpStatus.SC_OK);
            response.setEntity(entity);
        } catch (Exception ex) {
            log.error(ex);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("query", inputQuery);
            try {
                errorMap.put("error", queryStatus.getError());
            } catch (Exception e) {
                errorMap.put("error", "null");
            }

            Gson gson = new Gson();
            response.setEntity(new NStringEntity(gson.toJson(errorMap)));
        }

    }

    private String getDflQuery(String madisQuery, String queryName) throws ParseException {
        madisQuery = madisQuery.replaceAll("(?i)wcache", " ");
        StringBuilder dflQuery = new StringBuilder();
        List<String> script = new ArrayList<>();
        List<String> windowTablesName = new ArrayList<>();
        Pattern madisCreateStmtPattern = Pattern
            .compile("(?i)\\s*create\\s+(temp|temporary)\\s+(view|table)\\s+(\\w+)\\s+as\\s+([\\s\\S]*)");
        for (String stream : madisQuery.split(";\\s+")) {
            String query = stream.trim();
            if (!query.isEmpty()) {
                Matcher createStmtMatcher = madisCreateStmtPattern.matcher(query);
                if (createStmtMatcher.find()) {
                    if (query.toLowerCase().contains("timeslidingwindow")) {
                        String tableName = createStmtMatcher.group(3);
                        String windowQuery = createStmtMatcher.group(4);
                        dflQuery.append("distributed create temporary table ").append(tableName)
                            .append("_unordered")
                            .append(" to " + numberOfMachines + " on part as direct\n")
                            .append(getWindowRangePartitionQuery(windowQuery)).append(";\n\n");

                        script.add(
                            "create index " + tableName + "_index on " + tableName
                                + "_unordered (wid, abox, timestamp)");

                        script.add(
                            "create temp view " + tableName + " as select * from " + tableName
                                + "_unordered order by wid, abox, timestamp");
                        windowTablesName.add(tableName + "_unordered");
                    } else {
                        script.add(query);
                    }
                } else if (query.toLowerCase().startsWith("select")) {
                    script.add(query);
                    break;
                } else if (!query.matches("(?i)create\\s+index.*$")) {
                    script.add(query);
                }
            }
        }

        if (!windowTablesName.isEmpty()) {
            dflQuery.append("using ");
            int i;
            for (i = 0; i < windowTablesName.size() - 1; ++i) {
                dflQuery.append(windowTablesName.get(i) + ",");
            }
            dflQuery.append(windowTablesName.get(i) + " ");
        }

        dflQuery.append("distributed create table ").append(queryName)
            .append(" as directscript\n");
        int i;
        for (i = 0; i < script.size() - 1; ++i) {
            String query = script.get(i).replaceAll("'", "''").replaceAll("\\s+", " ");
            dflQuery.append("select '").append(query).append("' as query\nunion all\n");
        }
        dflQuery.append("select '").append(script.get(i)).append("' as query;");

        return dflQuery.toString();
    }

    private String getWindowRangePartitionQuery(String query) throws ParseException {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        String startTimestamp = null;
        String endTimestamp = null;

        Pattern startTimestampPattern1 = Pattern.compile("(?i)\\s+timestamp\\s+(>|>=)\\s+'(\\S+)'");
        Pattern startTimestampPattern2 = Pattern.compile("(?i)\\s+'(\\S+)'\\s+(<|<=)\\s+timestamp");
        Pattern endTimestampPattern1 = Pattern.compile("(?i)\\s+timestamp\\s+(<|<=)\\s+'(\\S+)'");
        Pattern endTimestampPattern2 = Pattern.compile("(?i)\\s+'(\\S+)'\\s+(>|>=)\\s+timestamp");
        Pattern betweenPattern =
            Pattern.compile("(?i)between\\s+timestamp\\s+'(\\S+)'\\s+and\\s+'(\\S+)'");

        Matcher betweenMatcher = betweenPattern.matcher(query);
        if (betweenMatcher.find()) {
            startTimestamp = betweenMatcher.group(1);
            endTimestamp = betweenMatcher.group(2);
        } else {
            Matcher startTimestampMatcher = startTimestampPattern1.matcher(query);
            if (startTimestampMatcher.find()) {
                startTimestamp = startTimestampMatcher.group(2);
            } else {
                startTimestampMatcher = startTimestampPattern2.matcher(query);
                if (startTimestampMatcher.find()) {
                    startTimestamp = startTimestampMatcher.group(1);
                }
            }

            Matcher endTimestampMatcher = endTimestampPattern1.matcher(query);
            if (endTimestampMatcher.find()) {
                endTimestamp = endTimestampMatcher.group(2);
            } else {
                endTimestampMatcher = endTimestampPattern2.matcher(query);
                if (endTimestampMatcher.find()) {
                    endTimestamp = endTimestampMatcher.group(1);
                }
            }
        }

        if (startTimestamp == null || endTimestamp == null) {
            throw new ParseException("Timestamp limits may define ...", 10);
        }

        Long startEpochTime;
        Long endEpochTime;
        try {
            startEpochTime = dateParser.parse(startTimestamp).getTime() / 1000;
            endEpochTime = dateParser.parse(endTimestamp).getTime() / 1000;
        } catch (java.text.ParseException e) {
            throw new ParseException("Timestamp format are not valid (yyyy-MM-ddTHH:mm:ssX)", 10);
        }

        return query.replace("timeslidingwindow",
            "timeslidingwindow minepochtime:" + startEpochTime + " parts:8 ")
            .replace("timeslidingwindow", "timeslidingwindow maxepochtime:" + endEpochTime + " ");
    }
}
