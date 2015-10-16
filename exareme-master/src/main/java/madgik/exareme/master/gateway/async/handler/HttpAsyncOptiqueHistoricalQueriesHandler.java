package madgik.exareme.master.gateway.async.handler;

import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.async.handler.entity.NQueryStatusEntity;
import org.apache.http.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Query Handler.
 *
 * @author alex
 * @author Christoforos Svingos
 * @since 0.1
 */
public class HttpAsyncOptiqueHistoricalQueriesHandler
    implements HttpAsyncRequestHandler<HttpRequest> {
    private static final Logger log =
        Logger.getLogger(HttpAsyncOptiqueHistoricalQueriesHandler.class);
    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();

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
            query = inputContent.get(ExaremeGatewayUtils.REQUEST_STREAMQUERY);
            query = getDflQuery(query, queryName);
            log.info("Database : " + dbname);
            log.info("Query : " + query);
        } catch (Exception ex) {
            log.error(ex);
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new NStringEntity("ERROR"));
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
            response.setEntity(new NStringEntity(queryStatus.getError()));
        }

    }

    private String getDflQuery(String madisQuery, String queryName) {
        StringBuilder dflQuery = new StringBuilder();
        List<String> script = new ArrayList<>();
        List<String> windowTablesName = new ArrayList<>();
        Pattern madisCreateStmtPattern = Pattern
            .compile("(?i)\\s*create\\s+(temp|temporary)\\s+(view|table)\\s+(\\w+)\\s+as\\s+(.*)");
        for (String stream : madisQuery.split(";\\s+")) {
            String query = stream.trim().toLowerCase();
            if (!query.isEmpty()) {
                Matcher createStmtMatcher = madisCreateStmtPattern.matcher(query);
                if (createStmtMatcher.find()) {
                    if (query.contains("timeslidingwindow")) {
                        String tableName = createStmtMatcher.group(3);
                        dflQuery.append("distributed create temporary table ").append(tableName)
                            .append(" to 4 on wid as direct\n").append(createStmtMatcher.group(4))
                            .append(";\n\n");

                        script.add("create index " + tableName + "_index on " + tableName
                            + " (wid, abox)");
                        windowTablesName.add(tableName);
                    } else {
                        script.add(query);
                    }
                } else if (query.startsWith("select")) {
                    script.add(query);
                    break;
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

        dflQuery.append("distributed create temporary table ").append(queryName)
            .append(" to 1 as directscript\n");
        int i;
        for (i = 0; i < script.size() - 1; ++i) {
            String query = script.get(i).replaceAll("'", "''").replaceAll("\\s+", " ");
            dflQuery.append("select '").append(query).append("' as query\nunion all\n");
        }
        dflQuery.append("select '").append(script.get(i)).append("' as query;");

        return dflQuery.toString();
    }
}
