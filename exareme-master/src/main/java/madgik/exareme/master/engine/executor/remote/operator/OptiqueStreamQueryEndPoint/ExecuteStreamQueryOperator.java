package madgik.exareme.master.engine.executor.remote.operator.OptiqueStreamQueryEndPoint;

import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.utils.association.SimplePair;
import madgik.exareme.utils.http.HttpUtils;
import madgik.exareme.worker.art.concreteOperator.AbstractNiNo;
import org.apache.http.*;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ExecuteStreamQueryOperator extends AbstractNiNo {
    private static Logger log = Logger.getLogger(ExecuteStreamQueryOperator.class);

    @Override
    public void run() throws Exception {
        log.trace("Parsing parameters ...");
        String queryString = super.getParameterManager().getQueryString();
        int port =
                Integer.valueOf(super.getParameterManager().getParameter("port").get(0).getValue());

        StreamQueryExecutorThread queryExecutor =
                new StreamQueryExecutorThread(queryString, 15 * 60);
        queryExecutor.start();

        SocketConfig socketConfig =
                SocketConfig.custom().setSoTimeout(15000).setTcpNoDelay(true).build();

        final HttpServer server =
                ServerBootstrap.bootstrap().setListenerPort(port).setServerInfo("Test/1.1")
                        .setSocketConfig(socketConfig).setExceptionLogger(new StdErrorExceptionLogger())
                        .registerHandler(ExaremeGatewayUtils.GW_API_STREAMQUERY_RESULT,
                                new HttpGetStreamResultHandler(queryExecutor))
                        .registerHandler(ExaremeGatewayUtils.GW_API_STREAMQUERY_DELETE,
                                new HttpDeleteStreamHandler(queryExecutor)).create();

        server.start();
        //        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        while (StreamQueryExecutorThread.State.DESTROY != queryExecutor.state()) {
            Thread.sleep(1000);
        }

        log.info("Server Shotdown ...");
        server.shutdown(1, TimeUnit.MILLISECONDS);

        //        Runtime.getRuntime().addShutdownHook(new Thread() {
        //            @Override
        //            public void run() {
        //                server.shutdown(5, TimeUnit.SECONDS);
        //            }
        //        });

        exit(0);
    }

    static class StdErrorExceptionLogger implements ExceptionLogger {

        @Override
        public void log(final Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                System.err.println("Connection timed out");
            } else if (ex instanceof ConnectionClosedException) {
                System.err.println(ex.getMessage());
            } else {
                ex.printStackTrace();
            }
        }

    }


    static class HttpGetStreamResultHandler implements HttpRequestHandler {

        private static SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        private final StreamQueryExecutorThread queryExecutor;

        public HttpGetStreamResultHandler(final StreamQueryExecutorThread queryExecutor) {
            super();
            this.queryExecutor = queryExecutor;
        }

        public void handle(final HttpRequest request, final HttpResponse response,
                           final HttpContext context) throws HttpException, IOException {

            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
            if (!method.equals("GET") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            String target = request.getRequestLine().getUri();

            Map<String, String> dict = new HashMap<String, String>();
            //            if (request instanceof HttpEntityEnclosingRequest) {
            //                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            //                byte[] entityContent = EntityUtils.toByteArray(entity);
            //                System.out.println("Incoming entity content (bytes): " + entityContent.length);
            //            } else
            int semicolonIndx = target.indexOf('?');
            log.info(target.substring(semicolonIndx + 1));
            if (semicolonIndx > 0) {
                HttpUtils.getValues(target.substring(semicolonIndx + 1), dict);
            } else {
                HttpResponses.badRequest(response, "Check your URI: " + target);
            }

            try {
                long last;
                long endTimestamp;
                long startTimestamp;

                if (dict.containsKey("last")) {
                    last = Long.valueOf(dict.get("last"));
                    log.info(last);
                    endTimestamp = (long) ((double) System.currentTimeMillis() / (double) 1000.0);
                    startTimestamp = endTimestamp - last;
                } else if (dict.containsKey("startTimestamp") && dict.containsKey("endTimestamp")) {
                    startTimestamp = dateParser.parse(dict.get("startTimestamp")).getTime() / 1000;
                    endTimestamp = dateParser.parse(dict.get("endTimestamp")).getTime() / 1000;
                } else if (dict.containsKey("startTimestamp")) {
                    startTimestamp = dateParser.parse(dict.get("startTimestamp")).getTime() / 1000;
                    endTimestamp = (long) ((double) System.currentTimeMillis() / (double) 1000.0);
                } else {
                    HttpResponses.badRequest(response, "Check your URI: " + target);
                    return;
                }

                SimplePair<List<String[]>, ArrayDeque<Object[]>> buffer;
                try {
                    log.debug(
                            "Starttimestamp: " + startTimestamp + ", EndTimestamp: " + endTimestamp);
                    buffer = queryExecutor.getBuffer(startTimestamp, endTimestamp);
                } catch (IllegalThreadStateException ex) {
                    HttpResponses.serviceUnavailable(response, ex.getMessage());
                    return;
                }

                Header acceptHeader = request.getFirstHeader("Accept");
                Output output = acceptHeaderMining(acceptHeader);

                EntityTemplate body = new EntityTemplate(new StreamDataStreamer(buffer, output));
                if (output == Output.MIXED) {
                    body.setContentType("text/json");
                } else {
                    body.setContentType(acceptHeader.getValue());
                }
                response.setEntity(body);
            } catch (ParseException ex) {
                log.error("Response error: " + ex.getMessage(), ex);
                HttpResponses.badRequest(response, ex.getMessage());
            }
        }

        private Output acceptHeaderMining(Header header) {
            if (header == null)
                return Output.MIXED;

            if (header.getValue().toLowerCase().equals("application/json")) {
                return Output.JSON;
            } else if (header.getValue().toLowerCase().equals("text/csv")) {
                return Output.CSV;
            } else {
                return Output.MIXED;
            }

        }
    }


    static class HttpDeleteStreamHandler implements HttpRequestHandler {

        private final StreamQueryExecutorThread queryExecutor;

        public HttpDeleteStreamHandler(final StreamQueryExecutorThread queryExecutor) {
            super();
            this.queryExecutor = queryExecutor;
        }

        public void handle(final HttpRequest request, final HttpResponse response,
                           final HttpContext context) throws HttpException, IOException {

            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
            if (!method.equals("DELETE")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            queryExecutor.destroyGenerator();

            HttpResponses.quickResponse(response, "Stream Deleted !");
        }
    }
}

