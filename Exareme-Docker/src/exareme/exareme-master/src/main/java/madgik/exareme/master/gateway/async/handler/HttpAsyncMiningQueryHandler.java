package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
import madgik.exareme.master.gateway.async.handler.Exceptions.DatasetsException;
import madgik.exareme.master.gateway.async.handler.entity.NQueryResultEntity;
import madgik.exareme.master.queryProcessor.composer.AlgorithmProperties;
import madgik.exareme.master.queryProcessor.composer.Algorithms;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.Exceptions.AlgorithmException;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.*;

import static madgik.exareme.master.gateway.GatewayConstants.COOKIE_ALGORITHM_EXECUTION_ID;

public class HttpAsyncMiningQueryHandler implements HttpAsyncRequestHandler<HttpRequest> {

    private static final Logger log = Logger.getLogger(HttpAsyncMiningQueryHandler.class);
    private static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";
    private static final AdpDBManager manager = AdpDBManagerLocator.getDBManager();
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

    private HashMap <String,String[]> getDatasetsFromConsul() throws IOException {
        Gson gson = new Gson();
        HashMap <String,String[]> map = new HashMap<>();
        String masterKey = searchConsul("master/?keys");
        String[] masterKeysArray = gson.fromJson(masterKey, String[].class);

        String masterName = masterKeysArray[0].replace("master/", "");
        String masterIP = searchConsul("master/"+masterName+"?raw");
        String datasetKey = searchConsul("datasets/"+masterName+"?raw");
        String[] datasetKeysArray = gson.fromJson(datasetKey, String[].class);
        map.put(masterIP,datasetKeysArray);

        String workersKey = searchConsul("active_workers/?keys");
        String[] workerKeysArray = gson.fromJson(workersKey, String[].class);
        for(String worker: workerKeysArray){
            String workerName = worker.replace("active_workers/", "");
            String workerIP = searchConsul("active_workers/"+workerName+"?raw");
            datasetKey = searchConsul("datasets/"+workerName+"?raw");
            datasetKeysArray = gson.fromJson(datasetKey, String[].class);
            map.put(workerIP,datasetKeysArray);
        }
        return map;
    }

    private  List<String> checkDatasets(HashMap <String,String[]> map, String[] usedDatasets) throws DatasetsException{
        List<String> notFoundDatasets = new ArrayList<>();
        List<String> nodesToBeChecked = new ArrayList<>();
        Boolean flag;
        for (String data: usedDatasets) {
            Iterator entries = map.entrySet().iterator();
            flag = false;
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                String IP = (String)entry.getKey();
                String[] datasets = (String[]) entry.getValue();
                if (Arrays.asList(datasets).contains(data)) {
                    if(!nodesToBeChecked.contains(IP))
                        nodesToBeChecked.add(IP);
                    flag = true;
                    continue;
                }
            }
            if(!flag){
                notFoundDatasets.add(data);
            }
        }
        if(notFoundDatasets.size() !=0 ){
            StringBuilder notFound = new StringBuilder();
            for (String ds : notFoundDatasets)
                notFound.append(ds).append(", ");
            String notFoundSring = notFound.toString();
            notFoundSring = notFoundSring.substring(0, notFoundSring.length() - 2);
            throw new DatasetsException("Dataset(s) " + notFoundSring + " not found!");
        }
        return nodesToBeChecked;
    }

    private String preALgoExecutionChecks (HttpRequest request) throws UnsupportedHttpVersionException {
        log.debug("Validate method ...");
        RequestLine requestLine = request.getRequestLine();
        String uri = requestLine.getUri();
        String algorithmName = uri.substring(uri.lastIndexOf('/') + 1);
        String method = requestLine.getMethod().toUpperCase(Locale.ENGLISH);

        if (!"POST".equals(method)) {
            throw new UnsupportedHttpVersionException(method + "not supported.");
        }
        log.debug("Posting " + algorithmName + " ...\n");
        return algorithmName;
    }

    // TODO Refactor as much as possible
    private void handleInternal(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {

        //Check given method
        String algorithmName = preALgoExecutionChecks(request);

        //Get parameters of given algorithm
        log.debug("Parsing content ...");
        HashMap<String, String> inputContent = new HashMap<String, String>();
        List<Map> parameters = new ArrayList();
        String content;
        String[] usedDatasets = null;

        if (request instanceof HttpEntityEnclosingRequest) {
            log.debug("Streaming ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            content = EntityUtils.toString(entity);
            if (content != null && !content.isEmpty()) {
                parameters = new Gson().fromJson(content, List.class);
            }
        }
        log.debug("All of the parameters: " + parameters);
        for (Map k : parameters) {
            String name = (String) k.get("name");
            String value = (String) k.get("value");
            if (name == null || name.isEmpty() || value == null || value.isEmpty()) continue;

            log.debug("Parameter in the json: ");
            log.debug(name + " = " + value);

            if (name.equals("filter")) value = value.replaceAll("[^A-Za-z0-9,._*+><=&|(){}:\"\\[\\]]", "");
            else value = value.replaceAll("[^A-Za-z0-9,._*+():\\-{}\\\"\\[\\]]", "");    // ><=&| we no more need those for filtering
            value = value.replaceAll("\\s+", "");

            if ("dataset".equals(name)) usedDatasets = value.split(",");

            log.debug("Parameter after format: ");
            log.debug(name + " = " + value);

            inputContent.put(name, value);
        }

        try {

            //Get datasets available in Consul.io for each node
            HashMap <String,String[]> map = getDatasetsFromConsul();

            //Check that datasets provided by user exist and retrieve nodes containing the existing datasets
            List<String> nodesToBeChecked;
            if(usedDatasets == null) nodesToBeChecked = allNodesIPs();
            else nodesToBeChecked = checkDatasets(map,usedDatasets);

            //Check that node containers are up and running properly
            log.debug("Checking workers...");

            if (!allNodesRunning(response,nodesToBeChecked)) return;
            ContainerProxy[] usedContainerProxies;

            //Find container proxy of used containers (from IPs)
            List<ContainerProxy> usedContainerProxiesList = new ArrayList<>();
            for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
                if (nodesToBeChecked.contains(containerProxy.getEntityName().getIP())) {
                    usedContainerProxiesList.add(containerProxy);
                }
            }
            usedContainerProxies = usedContainerProxiesList.toArray(new ContainerProxy[usedContainerProxiesList.size()]);

            int numberOfContainers = usedContainerProxies.length;
            log.debug("Containers: " + numberOfContainers);
            String algorithmKey = algorithmName + "_" + System.currentTimeMillis();
            String dfl;
            AdpDBClientQueryStatus queryStatus;

            AlgorithmProperties algorithmProperties = Algorithms.getInstance().getAlgorithmProperties(algorithmName);
            if (algorithmProperties == null)
                throw new AlgorithmException("The algorithm '" + algorithmName + "' does not exist.");

            algorithmProperties.mergeAlgorithmParametersWithInputContent(inputContent);

            DataSerialization ds = DataSerialization.summary;

            if (algorithmProperties.getResponseContentType() != null) {
                response.setHeader("Content-Type", algorithmProperties.getResponseContentType());
            }

            // Bypass direct composer call in case of iterative algorithm.
            if (algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.iterative) ||
                    algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.python_iterative)) {

                final IterativeAlgorithmState iterativeAlgorithmState =
                        iterationsHandler.handleNewIterativeAlgorithmRequest(
                                manager, algorithmKey, algorithmProperties);

                BasicHttpEntity entity = new NIterativeAlgorithmResultEntity(
                        iterativeAlgorithmState, ds, ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);

                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            } else {
                dfl = Composer.composeDFLScript(algorithmKey, algorithmProperties, numberOfContainers);
                log.debug(dfl);
                try {
                    Composer.persistDFLScriptToAlgorithmsDemoDirectory(
                            HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey
                                    + "/" + algorithmKey,
                            dfl, null);
                } catch (IOException e) {
                    // Ignoring error if failed to persist DFL Scripts - it's not something fatal.
                    log.error(e);
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
        } catch (IterationsFatalException e) {
            log.error(e);
            if (e.getErroneousAlgorithmKey() != null)
                iterationsHandler.removeIterativeAlgorithmStateInstanceFromISM(
                        e.getErroneousAlgorithmKey());
            log.error(e);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(("{\"error\" : \"" + e.getMessage() + "\"}").getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        } catch (JsonSyntaxException e) {
            log.error("Could not parse the algorithms properly.");
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(("{\"error\" : \"Could not parse the algorithms properly.\"}").getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        } catch (Exception e) {
            log.error(e);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(("{\"error\" : \"" + e.getMessage() + "\"}").getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        }
    }

    private void getMessage(HttpResponse response, String message) throws IOException {
        BasicHttpEntity entity = new BasicHttpEntity();
        String result = "{\"Error\":\"" + message + "\"}";
        byte[] contentBytes = result.getBytes(Charsets.UTF_8.name());
        entity.setContent(new ByteArrayInputStream(contentBytes));
        entity.setContentLength(contentBytes.length);
        entity.setContentEncoding(Charsets.UTF_8.name());
        entity.setChunked(false);
        response.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
        response.setEntity(entity);
    }

    private boolean allNodesRunning(HttpResponse response, List<String> usedIPs) throws IOException {
        boolean containerResponded = false;

        for( String IP: usedIPs){
            log.debug("Will check container with IP: " + IP);

            if (IP != null && !usedIPs.contains(IP)) {
                log.debug("Container not used, skipping");
                continue;
            }
            for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
                if (containerProxy.getEntityName().getIP().equals(IP)) {
                    try {
                        ContainerProxy tmpContainerProxy = ArtRegistryLocator.getArtRegistryProxy()
                                .lookupContainer(containerProxy.getEntityName());
                        containerResponded = true;

                        break;
                    } catch (Exception e) {
                        log.error("Container connection error: " + e);
                        ArtRegistryLocator.getArtRegistryProxy()
                                .removeContainer(containerProxy.getEntityName());

                        containerResponded = false;
                    }
                }
            }
            log.debug("Container responded: " + containerResponded);
            if (!containerResponded) {
                getMessage(response, "Container with IP " + IP + " is not responding. Please inform your system administrator");
                return false;
            }
        }
        return true;
    }

    private List<String> allNodesIPs() throws RemoteException {
        List<String> allIPs = new ArrayList<>();
        for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
            allIPs.add(containerProxy.getEntityName().getIP());
        }
        return allIPs;
    }

    private String searchConsul(String query) throws IOException {
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String consulURL = System.getenv("CONSULURL");
        if (consulURL == null) throw new IOException("Consul url not set");
        if (!consulURL.startsWith("http://")) {
            consulURL = "http://" + consulURL;
        }
        try {
            HttpGet httpGet;
            httpGet = new HttpGet(consulURL + "/v1/kv/" + query);
            log.debug("Running: " + httpGet.getURI());
            CloseableHttpResponse response = null;
            if (httpGet.toString().contains("master/") || httpGet.toString().contains("datasets/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/master* or http://exareme-keystore:8500/v1/kv/datasets*
                try {   //then throw exception
                    response = httpclient.execute(httpGet);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        throw new ServerException("Cannot contact consul", new Exception(EntityUtils.toString(response.getEntity())));
                    } else {
                        result = EntityUtils.toString(response.getEntity());
                    }
                } finally {
                    response.close();
                }
            }
            if (httpGet.toString().contains("active_workers/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/active_workers*
                try {   //then maybe there are no workers running
                    response = httpclient.execute(httpGet);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        if (httpGet.toString().contains("?keys"))
                            log.debug("No workers running. Continue with master");
                    } else {
                        result = EntityUtils.toString(response.getEntity());
                    }
                } finally {
                    response.close();
                }
            }
        } finally {
            return result;
        }
    }
}

