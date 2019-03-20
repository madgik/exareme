package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;
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
import madgik.exareme.master.queryProcessor.composer.Algorithms;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.ComposerException;
import madgik.exareme.utils.net.NetUtil;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.commons.io.Charsets;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.*;

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

    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request,
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

    // TODO Refactor as much as possible
    private void handleInternal(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {

        log.debug("Validate method ...");
        RequestLine requestLine = request.getRequestLine();
        String uri = requestLine.getUri();
        String method = requestLine.getMethod().toUpperCase(Locale.ENGLISH);
        if (!"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }

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
            parameters = new Gson().fromJson(content, List.class);
        }

        String algorithmName = uri.substring(uri.lastIndexOf('/') + 1);

        boolean format = false;
        log.debug("Posting " + algorithmName + " ...\n");
        String[] usedDatasets = null;

        log.debug("All of the parameters: " + parameters);
        for (Map k : parameters) {
            String name = (String) k.get("name");
            String value = (String) k.get("value");
            if (name == null || name.isEmpty() || value == null || value.isEmpty()) continue;

            log.debug("Parameter in the json: ");
            log.debug(name + " = " + value);

            if (name.equals("filter"))
                value = value.replaceAll("[^A-Za-z0-9,._*+><=&|(){}:\"\\[\\]]", "");
            else
                value = value.replaceAll("[^A-Za-z0-9,._*+():\\-{}\\\"\\[\\]]", "");    // ><=&| we no more need those for filtering
            value = value.replaceAll("\\s+", "");

            if ("dataset".equals(name)) {
                usedDatasets = value.split(",");
            }

            log.debug("Parameter after format: ");
            log.debug(name + " = " + value);

            inputContent.put(name, value);
        }

        Set<String> usedContainersIPs = getUsedContainers(usedDatasets, response);
        if (usedContainersIPs == null) return;
        ContainerProxy[] usedContainerProxies;
        log.debug("Checking workers...");
        if (!allWorkersRunning(response, usedContainersIPs)) return;

        //Find container proxy of used containers (from IPs)
        List<ContainerProxy> usedContainerProxiesList = new ArrayList<>();
        for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
            if (usedContainersIPs.contains(containerProxy.getEntityName().getIP())) {
                usedContainerProxiesList.add(containerProxy);
            }
        }
        usedContainerProxies = usedContainerProxiesList.toArray(new ContainerProxy[usedContainerProxiesList.size()]);

        int numberOfContainers = usedContainerProxies.length;
        log.debug("Containers: " + numberOfContainers);
        log.debug("Containers: " + new Gson().toJson(usedContainersIPs));
        String algorithmKey = algorithmName + "_" + System.currentTimeMillis();

        try {
            String dfl;
            AdpDBClientQueryStatus queryStatus;

            Algorithms.AlgorithmProperties algorithmProperties =
                    Algorithms.AlgorithmProperties.createAlgorithmProperties(algorithmName, inputContent);

            DataSerialization ds = DataSerialization.summary;

            if (algorithmProperties.getResponseContentType() != null) {
                response.setHeader("Content-Type", algorithmProperties.getResponseContentType());
            }

            // Bypass direct composer call in case of iterative algorithm.
            if (algorithmProperties.getType() ==
                    Algorithms.AlgorithmProperties.AlgorithmType.iterative) {

                final IterativeAlgorithmState iterativeAlgorithmState =
                        iterationsHandler.handleNewIterativeAlgorithmRequest(
                                manager, algorithmKey, algorithmProperties);

                BasicHttpEntity entity = new NIterativeAlgorithmResultEntity(
                        iterativeAlgorithmState, ds, ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);

                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            } else {
                dfl = composer.composeVirtual(algorithmKey, algorithmProperties, null, numberOfContainers);
                log.debug(dfl);
                try {
                    Composer.persistDFLScriptToAlgorithmsDemoDirectory(
                            HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey
                                    + "/" + algorithmKey,
                            dfl, null);
                } catch (ComposerException e) {
                    // Ignoring error if failed to persist DFL Scripts - it's not something fatal.
                }
                AdpDBClientProperties clientProperties =
                        new AdpDBClientProperties(
                                HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey,
                                "", "", false, false,
                                -1, 10);
                clientProperties.setContainerProxies(usedContainerProxies);
                AdpDBClient dbClient =
                        AdpDBClientFactory.createDBClient(manager, clientProperties);
                queryStatus = dbClient.query(algorithmKey, dfl);
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

    private boolean allWorkersRunning(HttpResponse response, Set<String> usedIPs) throws IOException {
        BasicHttpEntity entity = new BasicHttpEntity();

        String workersPath = AdpProperties.getGatewayProperties().getString("workers.path");
        log.debug("Workers Path : " + workersPath);
        try (BufferedReader br = new BufferedReader(new FileReader(workersPath))) {
            String containerIP, containerNAME, line;
            String[] container;
            while ((line = br.readLine()) != null) {
                container = line.split(" ");
                containerIP = container[0];
                containerNAME = container[1];

                log.debug("Will check container with IP: " + containerIP + " and NAME: " + containerNAME);
                if (usedIPs != null && !usedIPs.contains(containerIP)) {
                    log.debug("Container not used, skipping");
                    continue;
                }
                boolean containerResponded = false;
                for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
                    if (containerProxy.getEntityName().getIP().equals(containerIP)) {
                        try {
                            ContainerProxy tmpContainerProxy = ArtRegistryLocator.getArtRegistryProxy()
                                    .lookupContainer(containerProxy.getEntityName());
                            containerResponded = true;
                            break;
                        } catch (Exception e) {
                            log.error("Container connection error: " + e);
                        }
                    }
                }
                log.debug("Container responded: " + containerResponded);
                if (!containerResponded) {
                    String result = "{\"Error\":\"Container with IP " + containerIP + " and NAME " + containerNAME + " is not responding. Please inform your system administrator\"}";
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
        } catch (FileNotFoundException e) {
            log.warn("Workers file not found at: " + workersPath + ". Will continue without checking that all containers are running");
        }
        return true;
    }

    //Return null if dataset not found
    private Set<String> getUsedContainers(String[] usedDatasets, HttpResponse response) throws RemoteException {
        try {
            //if datasets are not defined run to all datasets
            if (usedDatasets == null || usedDatasets.length == 0) {
                return allContainersIPs();
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
            List<String> notFoundDatasets = new ArrayList<>();
            for (String dataset : usedDatasets) {
                if (!datasetToNodes.containsKey(dataset)) {
                    notFoundDatasets.add(dataset);
                } else {
                    usedContainersIPs.addAll(datasetToNodes.get(dataset));
                }
            }
            if (!notFoundDatasets.isEmpty()) {
                returnNotFoundError(response, notFoundDatasets);
                return null;
            }

            return usedContainersIPs;

        } catch (IOException e) {
            log.error("Exception while contacting consul, running with all containers. " + e.getMessage());
            return allContainersIPs();
        }
    }

    private void returnNotFoundError(HttpResponse response, List<String> notFoundDatasets) throws UnsupportedEncodingException {
        StringBuilder notFound = new StringBuilder();
        for (String ds : notFoundDatasets) {
            notFound.append(ds);
            notFound.append(", ");
        }
        String notFoundSring = notFound.toString();
        notFoundSring = notFoundSring.substring(0, notFoundSring.length() - 2);
        log.debug("Dataset(s) " + notFoundSring + " not found!");
        String result = "{\"Error\":\"Dataset(s) " + notFoundSring + " not found!\"}";
        byte[] contentBytes = result.getBytes(Charsets.UTF_8.name());
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(contentBytes));
        entity.setContentLength(contentBytes.length);
        entity.setContentEncoding(Charsets.UTF_8.name());
        entity.setChunked(false);
        response.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
        response.setEntity(entity);
    }

    private Set<String> allContainersIPs() throws RemoteException {
        Set<String> allIPs = new HashSet<>();
        for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
            allIPs.add(containerProxy.getEntityName().getIP());
        }
        return allIPs;
    }

    private String getNodeIP(String name) throws IOException {
        try {
            return searchConsul("active_workers/" + name + "?raw");
        } catch (IOException e) { //Worker not found
            try {
                return searchConsul("master/" + name + "?raw");
            } catch (IOException em) {
                throw new IOException("Worker with name " + name + " not found");
            }
        }
    }

    private String searchConsul(String query) throws IOException {
        String result;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String consulURL = System.getenv("CONSULURL");
        if (consulURL == null) throw new IOException("Consul url not set");
        if (!consulURL.startsWith("http://")) {
            consulURL = "http://" + consulURL;
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

