package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;

import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.commons.io.Charsets;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.*;
import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.connector.DataSerialization;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;
import madgik.exareme.master.engine.iterations.handler.IterationsHandler;
import madgik.exareme.master.engine.iterations.handler.NIterativeAlgorithmResultEntity;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.gateway.ExaremeGatewayUtils;
import madgik.exareme.master.gateway.async.handler.entity.NQueryResultEntity;
import madgik.exareme.master.queryProcessor.composer.AlgorithmsProperties;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.ComposerConstants;
import madgik.exareme.master.queryProcessor.composer.ComposerException;
import madgik.exareme.utils.encoding.Base64Util;

import static madgik.exareme.master.gateway.GatewayConstants.COOKIE_ALGORITHM_EXECUTION_ID;

/**
 * Mining  Handler
 *
 * @author alex
 * @since 0.1
 */
public class HttpAsyncMiningQueryHandler implements HttpAsyncRequestHandler<HttpRequest> {

    private static final Logger log = Logger.getLogger(HttpAsyncMiningQueryHandler.class);
    private static final String msg =
        "{ " + "\"schema\":[[\"error\",\"text\"]], " + "\"errors\":[[null]] " + "}\n";

    private static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";
    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();
    private static final Composer composer = Composer.getInstance();
    private static final IterationsHandler iterationsHandler = IterationsHandler.getInstance();

    public HttpAsyncMiningQueryHandler() {
    }

    @Override public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request,
        HttpContext context) throws HttpException, IOException {

        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpExchange, HttpContext context)
        throws HttpException, IOException {

        HttpResponse response = httpExchange.getResponse();
        response.setHeader("Content-Type", String.valueOf(ContentType.APPLICATION_JSON));

        // When under testing the Set-Cookie header has been used with the "algorithm execution id"
        // parameter for differentiating between concurrent executions of algorithms.
        if (request.containsHeader(SET_COOKIE_HEADER_NAME)) {
            HeaderIterator it = request.headerIterator(SET_COOKIE_HEADER_NAME);

            // Parse "algorithm execution id" cookie
            StringBuilder echoCookieContent = new StringBuilder();
            while (it.hasNext()) {
                echoCookieContent.append(it.next());
            }

            String cookieContentStr = echoCookieContent.toString();
            if (!cookieContentStr.isEmpty() &&
                    cookieContentStr.contains(COOKIE_ALGORITHM_EXECUTION_ID)) {

                String algorithmExecIdStr =
                        cookieContentStr.substring(
                                cookieContentStr.indexOf(" "),
                                cookieContentStr.length())
                                .split("=")[1];

                response.addHeader(
                        SET_COOKIE_HEADER_NAME,
                        COOKIE_ALGORITHM_EXECUTION_ID + "=" + algorithmExecIdStr);
            }
        }
        handleInternal(request, response, context);
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(HttpRequest request, HttpResponse response, HttpContext context)
        throws HttpException, IOException {

        log.debug("Checking workers...");
        if (!allWorkersRunning(response)) return;

        log.debug("Validate method ...");
        RequestLine requestLine = request.getRequestLine();
        String uri = requestLine.getUri();
        String method = requestLine.getMethod().toUpperCase(Locale.ENGLISH);

        log.debug("Parsing content ...");
        String content = "";
        if (request instanceof HttpEntityEnclosingRequest) {

            log.debug("Streaming ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            content = EntityUtils.toString(entity);
        }
        HashMap<String, String> inputContent = new HashMap<String, String>();
        List<Map> parameters = new ArrayList();
        if (content != null && !content.isEmpty()) {
//            ExaremeGatewayUtils.getValues(content, inputContent);
            parameters = new Gson().fromJson(content, List.class);
        }
        if (!"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }
        String query = null;
        String algorithm = uri.substring(uri.lastIndexOf('/')+1);



        boolean format = false;
        log.debug("Posting " + algorithm + " ...\n");
        String[] usedDatasets = null;
        for (Map k : parameters) {
            String name = (String) k.get("name");
            String value = (String) k.get("value");
            if(name == null || name.isEmpty() || value == null || value.isEmpty()) continue;
            log.debug(name + " = " + value);
            value = value.replaceAll("[^A-Za-z0-9,_*+><=&|()]", "");
            value = value.replaceAll("\\s+", "");
            if("local_pfa".equals(name)) {
                Map map = new Gson().fromJson(value, Map.class);
                query = (String) ((Map) ((Map)((Map) map.get("cells")).get("query")).get("init")).get("sql");
                value = Base64Util.simpleEncodeBase64(value);
            } else if("format".equals(name)){
                format = Boolean.parseBoolean(value);
            } else if ("dataset".equals(name)){
                usedDatasets = value.split(",");
            }
            inputContent.put(name, value);
            log.debug(name + " = " + value);
        }

        ContainerProxy[] usedContainerProxies = getUsedContainers(usedDatasets, response);
        if (usedContainerProxies==null) return;
        int numberOfContainers = usedContainerProxies.length;
        log.debug("Containers: " + numberOfContainers);

        String qKey = "query_" + algorithm + "_" +String.valueOf(System.currentTimeMillis());

        try {
            String dfl;
            AdpDBClientQueryStatus queryStatus;

            inputContent.put(ComposerConstants.outputGlobalTblKey, "output_" + qKey);
            inputContent.put(ComposerConstants.algorithmKey, algorithm);
            AlgorithmsProperties.AlgorithmProperties algorithmProperties =
                    AlgorithmsProperties.AlgorithmProperties.createAlgorithmProperties(inputContent);

            // Was initialized to "DataSerialization.ldjson", and that was followed with
            // if(format) ds = DataSerialization.summary; but was commented-out.
            DataSerialization ds = DataSerialization.summary;

            if (algorithmProperties.getResponseContentType()!=null){
                response.setHeader("Content-Type", algorithmProperties.getResponseContentType());
            }

            // Bypass direct composer call in case of iterative algorithm.
            if (algorithmProperties.getType() ==
                    AlgorithmsProperties.AlgorithmProperties.AlgorithmType.iterative) {

                final IterativeAlgorithmState iterativeAlgorithmState =
                        iterationsHandler.handleNewIterativeAlgorithmRequest(
                                manager, algorithmProperties);

                BasicHttpEntity entity = new NIterativeAlgorithmResultEntity(
                        iterativeAlgorithmState, ds, ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            } else {
                dfl = composer.composeVirtual(qKey, algorithmProperties, query, null, numberOfContainers);
                log.debug(dfl);
                try {
                    Composer.persistDFLScriptToAlgorithmsDemoDirectory(
                            HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + qKey
                                    + "/" + qKey,
                            dfl, null);
                } catch (ComposerException e) {
                    // Ignoring error if failed to persist DFL Scripts - it's not something fatal.
                }
                AdpDBClientProperties clientProperties =
                        new AdpDBClientProperties(
                                HBPConstants.DEMO_DB_WORKING_DIRECTORY + qKey,
                                "", "", false, false,
                                -1, 10);
                clientProperties.setContainerProxies(usedContainerProxies);
                AdpDBClient dbClient =
                        AdpDBClientFactory.createDBClient(manager, clientProperties);
                queryStatus = dbClient.query(qKey, dfl);
                BasicHttpEntity entity = new NQueryResultEntity(queryStatus, ds,
                        ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            }

        } catch (ComposerException e) {
            log.error(e);
        } catch (IterationsFatalException e) {
            if (e.getErroneousAlgorithmKey() != null)
                iterationsHandler.removeIterativeAlgorithmStateInstanceFromISM(
                        e.getErroneousAlgorithmKey());
            log.error(e);
        } catch (Exception e) {
            log.error(e);
            throw new IOException(e.getMessage(), e);
        }
    }

    private boolean allWorkersRunning(HttpResponse response) throws IOException {
        BasicHttpEntity entity = new BasicHttpEntity();

        String workersPath = AdpProperties.getGatewayProperties().getString("workers.path");
        log.debug("Workers Path : " + workersPath);
        try (BufferedReader br = new BufferedReader(new FileReader(workersPath))) {
            String containerIP,containerNAME,line;
            String[] container;
            while ((line = br.readLine()) != null) {
                container = line.split(" ");
                containerIP = container[0];
                containerNAME = container[1];
                log.debug("Will check container with IP: "+containerIP+ " and NAME: "+containerNAME);
                boolean containerResponded = false;
                for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
                    if (containerProxy.getEntityName().getIP().equals(containerIP))
                        try {
                            ContainerProxy tmpContainerProxy = ArtRegistryLocator.getArtRegistryProxy()
                                    .lookupContainer(containerProxy.getEntityName());
                            containerResponded = true;
                            break;
                        } catch (Exception e) {
                            log.error("Container connection error: " + e);
                        }
                }
                log.debug("Container responded: " + containerResponded);
                if (!containerResponded){
                    String result = "{\"Error\":\"Container with IP "+containerIP+" and NAME "+containerNAME+" is not responding. Please inform your system administrator\"}";
                    byte[] contentBytes = result.getBytes(Charsets.UTF_8.name());

                    entity.setContent(new ByteArrayInputStream(contentBytes));
                    entity.setContentLength(contentBytes.length);
                    entity.setContentEncoding(Charsets.UTF_8.name());
                    entity.setChunked(false);
                    response.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
                    response.setEntity(entity);
                    return false;
                }

            }
        } catch (FileNotFoundException e){
            log.warn("Workers file not found at: " + workersPath +". Will continue without checking that all containers are running");
        }
        return true;
    }

    //Return null if dataset not found
    private ContainerProxy[] getUsedContainers(String[] usedDatasets, HttpResponse response) throws RemoteException {
        try {
            //if datasets are not defined run to all datasets
            if (usedDatasets == null || usedDatasets.length == 0) {
                return ArtRegistryLocator.getArtRegistryProxy().getContainers();
            }

            //Generate dataset -> List<node IP> hashmap
            HashMap<String, List<String>> datasetToNodes = new HashMap<>();
            //Find nodes with datasets defined at consul
            String nodesKeys = searchConsul("datasets/?keys");
            log.debug(nodesKeys);
            Gson gson = new Gson();
            String[] nodesKeysArray = gson.fromJson(nodesKeys, String[].class);
            //For every node (with dataset...)
            for (String nodeKey : nodesKeysArray) {
                log.debug(searchConsul(nodeKey + "?raw"));
                String[] nodeDatasets = gson.fromJson(searchConsul(nodeKey + "?raw"), String[].class);
                String nodeName = nodeKey.replace("datasets/", "");
                //For every dataset of this node, add to hashmap
                for (String nodeDataset : nodeDatasets) {
                    if (!datasetToNodes.containsKey(nodeDataset)) {
                        datasetToNodes.put(nodeDataset, new ArrayList<String>());
                    }
                    datasetToNodes.get(nodeDataset).add(getNodeIP(nodeName));
                }
            }

            //Find IPs of used containers
            Set<String> usedContainersIPs = new HashSet<>();
            usedContainersIPs.add(NetUtil.getIPv4()); //Always use master!
            for (String dataset : usedDatasets) {
                if (!datasetToNodes.containsKey(dataset)){
                    log.debug("Dataset " + dataset + " not found!");
                    String result = "{\"Error\":\"Dataset "+dataset+" not found!\"}";
                    byte[] contentBytes = result.getBytes(Charsets.UTF_8.name());
                    BasicHttpEntity entity = new BasicHttpEntity();
                    entity.setContent(new ByteArrayInputStream(contentBytes));
                    entity.setContentLength(contentBytes.length);
                    entity.setContentEncoding(Charsets.UTF_8.name());
                    entity.setChunked(false);
                    response.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
                    response.setEntity(entity);
                    return null;
                }
                usedContainersIPs.addAll(datasetToNodes.get(dataset));
            }

            //Find container proxy of used containers (from IPs)
            List<ContainerProxy> usedContainerProxies = new ArrayList<>();
            for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
                if (usedContainersIPs.contains(containerProxy.getEntityName().getIP())) {
                    usedContainerProxies.add(containerProxy);
                }
            }

            log.debug("Used containers: " + usedContainerProxies);

            return usedContainerProxies.toArray(new ContainerProxy[usedContainerProxies.size()]);
        } catch (IOException e) {
            log.error("Exception while contacting consul, running with all containers. " + e.getMessage() );
            return ArtRegistryLocator.getArtRegistryProxy().getContainers();
        }
    }

    private String getNodeIP(String name) throws IOException {
        try {
            return searchConsul("active_workers/" + name + "?raw");
        } catch (IOException e) { //Worker not found
            try {
                return searchConsul("master/" + name + "?raw");
            } catch (IOException em){
                throw new IOException("Worker with name " + name + " not found");
            }
        }
    }

    private String searchConsul(String query) throws IOException {
        String result;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String consulURL = System.getenv("CONSULURL");
        if (consulURL == null ) throw new IOException("Consul url not set");
        if (!consulURL.startsWith("http://")){
            consulURL= "http://" + consulURL;
        }

        HttpGet httpGet = null;
        try {
            httpGet = new HttpGet(consulURL + "/v1/kv/" + query);
            log.debug("Running: " + httpGet.getURI());
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new ServerException("Cannot contact consul", new Exception(EntityUtils.toString(response.getEntity())));
                } else {
                    result = EntityUtils.toString(response.getEntity());
                }
            } finally {
                response.close();
            }
        } finally {
            httpGet.releaseConnection();
            httpclient.close();
        }
        return result;
    }
}

