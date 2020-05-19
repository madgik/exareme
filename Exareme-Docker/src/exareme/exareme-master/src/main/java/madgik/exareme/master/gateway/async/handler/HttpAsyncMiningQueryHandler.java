package madgik.exareme.master.gateway.async.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jcraft.jsch.IO;
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
import madgik.exareme.master.gateway.async.handler.Exceptions.PathologyException;
import madgik.exareme.master.gateway.async.handler.entity.NQueryResultEntity;
import madgik.exareme.master.queryProcessor.composer.AlgorithmProperties;
import madgik.exareme.master.queryProcessor.composer.Algorithms;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.Exceptions.AlgorithmException;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
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
    private static final String error = new String("text/plain+error");
    private static final String user_error = new String("text/plain+user_error");

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
        try {
            handleInternal(request, response, context);
        } catch (Exception e) {
            log.error(e.getMessage());
            String data = e.getMessage();
            String type = user_error;        //type could be error, user_error, warning regarding the error occurred along the process
            String result = defaultOutputFormat(data, type);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        }
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(HttpRequest request, HttpResponse response, HttpContext context)
            throws Exception {

        String datasets;
        String pathology=null;
        String[] userDatasets = null;

        //Check given method
        String algorithmName = preALgoExecutionChecks(request);

        //Get parameters of given algorithm
        HashMap<String, String> inputContent = getAlgoParameters(request);
        if (inputContent != null) {
            if (inputContent.containsKey("dataset")) {
                datasets = inputContent.get("dataset");
                //Get datasets provided by user
                userDatasets = datasets.split(",");
            }
            if (inputContent.containsKey("pathology"))
                pathology = inputContent.get("pathology");
        }

        try {
            //Get datasets available in Consul[Key-Value store] for each Exareme node[master/workers]
            HashMap<String, String[]> nodeDatasets = null;
            List<String> nodesToBeChecked;
            if (pathology != null) {
                try {
                    nodeDatasets = getDatasetsFromConsul(pathology);
                }
                catch (PathologyException e) {
                    log.error(e.getMessage());
                    String data = e.getMessage();
                    String type = user_error;        //type could be error, user_error, warning regarding the error occured along the process
                    String result = defaultOutputFormat(data, type);
                    BasicHttpEntity entity = new BasicHttpEntity();
                    entity.setContent(new ByteArrayInputStream(result.getBytes()));
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setEntity(entity);
                    return;
                }
                catch (Exception e){
                    String data = "An error has occurred.Please inform your system admin.";
                    String type = error;        //type could be error, user_error, warning regarding the error occured along the process
                    String result = defaultOutputFormat(data, type);
                    BasicHttpEntity entity = new BasicHttpEntity();
                    entity.setContent(new ByteArrayInputStream(result.getBytes()));
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setEntity(entity);
		            return;
                }
                if (userDatasets == null)
                    nodesToBeChecked = allNodesIPs();   //LIST_VARIABLES Algorithm
                else
                    nodesToBeChecked = checkDatasets(nodeDatasets, userDatasets, pathology);
            }
            else
                nodesToBeChecked = allNodesIPs();       //LIST_DATASET Algorithm

            //Check that node containers are up and running properly
            log.debug("Checking workers...");

            if (!nodesRunning(nodesToBeChecked,pathology)) return;  //TODO inside nodesRunning getDatasetsFromConsul(pathology) when there are IPs that do not belong in Exareme registry
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
                throw new AlgorithmException(algorithmName, "The algorithm '" + algorithmName + "' does not exist.");

            algorithmProperties.mergeAlgorithmParametersWithInputContent(inputContent);

            DataSerialization ds = DataSerialization.summary;

            // Bypass direct composer call in case of iterative algorithm.
            if (algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.iterative) ||
                    algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.python_iterative)) {

                final IterativeAlgorithmState iterativeAlgorithmState =
                        iterationsHandler.handleNewIterativeAlgorithmRequest(
                                manager, algorithmKey, algorithmProperties, usedContainerProxies);

                log.info("Iterative algorithm " + algorithmKey + " execution started.");

                BasicHttpEntity entity = new NIterativeAlgorithmResultEntity(
                        iterativeAlgorithmState, ds, ExaremeGatewayUtils.RESPONSE_BUFFER_SIZE);

                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(entity);
            } else {
                dfl = Composer.composeDFLScript(algorithmKey, algorithmProperties, numberOfContainers);
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

                log.info("Algorithm " + algorithmKey + " with queryID "
                        + queryStatus.getQueryID() + " execution started. DFL Script: \n " + dfl);

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
            String data = e.getMessage();
            String type = error;        //type could be error, user_error, warning regarding the error occured along the process
            String result = defaultOutputFormat(data, type);
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        } catch (JsonSyntaxException e) {
            log.error("Could not parse the algorithms properly.");
            String data = "Could not parse the algorithms properly.";
            String type = error;        //type could be error, user_error, warning regarding the error occured along the process
            String result = defaultOutputFormat(data, type);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        } catch (PathologyException | DatasetsException | IOException e) {
            log.error(e.getMessage());
            String data = e.getMessage();
            String type = user_error;        //type could be error, user_error, warning regarding the error occured along the process
            String result = defaultOutputFormat(data, type);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        }
        catch (Exception e) {
            log.error(e);
            String data = e.getMessage();
            String type = error;        //type could be error, user_error, warning regarding the error occured along the process
            String result = defaultOutputFormat(data, type);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(result.getBytes()));
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(entity);
        }
    }

    private HashMap<String, String[]> getDatasetsFromConsul(String pathology) throws IOException,PathologyException {
        Gson gson = new Gson();
        HashMap<String, String[]> nodeDatasets = new HashMap<>();
        List<String> pathologyNodes = new ArrayList<String>();

        String masterKey = searchConsul(System.getenv("EXAREME_MASTER_PATH") + "/?keys");
        String[] masterKeysArray = gson.fromJson(masterKey, String[].class);

        String masterName = masterKeysArray[0].replace(System.getenv("EXAREME_MASTER_PATH") + "/", "");
        String masterIP = searchConsul(System.getenv("EXAREME_MASTER_PATH") + "/" + masterName + "?raw");

        String pathologyKey = searchConsul(System.getenv("DATA") + "/" + masterName + "/" + pathology + "?keys");
        String[] pathologyKeyKeysArray = gson.fromJson(pathologyKey, String[].class);

        if (pathologyKeyKeysArray != null) {
            pathologyNodes.add(pathologyKeyKeysArray[0]);                 //Add Master Pathology
        }

        String datasetKey = searchConsul(System.getenv("DATA") + "/" + masterName + "/" + pathology + "?raw");
        String[] datasetKeysArray = gson.fromJson(datasetKey, String[].class);
        if (datasetKeysArray != null)
            nodeDatasets.put(masterIP, datasetKeysArray);                 //Map Master IP-> Matser Datasets

        String workersKey = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/?keys");
        if (workersKey == null) {     //No workers running
            if(pathologyNodes.isEmpty())
                throw new PathologyException("Pathology " + pathology + " not found!");
            return nodeDatasets;         //return master's Datasets only
        }
        String[] workerKeysArray = gson.fromJson(workersKey, String[].class);
        for (String worker : workerKeysArray) {
            String workerName = worker.replace(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/", "");
            String workerIP = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/" + workerName + "?raw");


            pathologyKey = searchConsul(System.getenv("DATA") + "/" + workerName + "/" + pathology + "?keys");
            pathologyKeyKeysArray = gson.fromJson(pathologyKey, String[].class);

            if (pathologyKeyKeysArray != null) {
                pathologyNodes.add(pathologyKeyKeysArray[0]);                 //Add worker Pathology
            }

            datasetKey = searchConsul(System.getenv("DATA") + "/" + workerName + "/" + pathology + "?raw");
            datasetKeysArray = gson.fromJson(datasetKey, String[].class);
            if (datasetKeysArray != null)
                nodeDatasets.put(workerIP, datasetKeysArray);        //Map Worker's IP-> Worker's Datasets
        }

        if (pathologyNodes.isEmpty()) {
            throw new PathologyException("Pathology " + pathology + " not found!");
        }

        return nodeDatasets;
    }

    private HashMap<String, String> getNamesOfActiveNodes() throws Exception {
        Gson gson = new Gson();
        HashMap<String, String> nodeNames = new HashMap<>();
        String masterKey = searchConsul(System.getenv("EXAREME_MASTER_PATH") + "/?keys");
        String[] masterKeysArray = gson.fromJson(masterKey, String[].class);    //Map Master's IP-> Master's Name

        String masterName = masterKeysArray[0].replace(System.getenv("EXAREME_MASTER_PATH") + "/", "");
        String masterIP = searchConsul(System.getenv("EXAREME_MASTER_PATH") + "/" + masterName + "?raw");
        nodeNames.put(masterIP, masterName);

        String workersKey = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/?keys");
        if (workersKey == null)             //No workers running
            return nodeNames;               //return master only
        String[] workerKeysArray = gson.fromJson(workersKey, String[].class);
        for (String worker : workerKeysArray) {
            String workerName = worker.replace(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/", "");
            String workerIP = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/" + workerName + "?raw");
            nodeNames.put(workerIP, workerName);         //Map Worker's IP-> Worker's Datasets
        }
        return nodeNames;
    }

    private List<String> checkDatasets(HashMap<String, String[]> nodeDatasets, String[] userDatasets, String pathology) throws DatasetsException {
        List<String> notFoundDatasets = new ArrayList<>();
        List<String> nodesToBeChecked = new ArrayList<>();
        Boolean flag;

        //for every dataset provided by the user
        for (String data : userDatasets) {
            Iterator<Map.Entry<String, String[]>> entries = nodeDatasets.entrySet().iterator();
            flag = false;
            //for each Exareme node (master/workers)
            while (entries.hasNext()) {
                Map.Entry<String, String[]> entry = entries.next();
                String IP = entry.getKey();
                String[] datasets = entry.getValue();
                //if dataset exist in that Exareme node
                if (Arrays.asList(datasets).contains(data)) {
                    //and Exareme node not already added to list nodesToBeChecked
                    if (!nodesToBeChecked.contains(IP))
                        nodesToBeChecked.add(IP);
                    flag = true;
                    continue;
                }
            }
            //if flag=false then dataset(s) provided by user are not contained in ANY Exareme node
            if (!flag) {
                notFoundDatasets.add(data);
            }
        }
        //if notFoundDatasets list is not empty, there are dataset(s) provided by user not contained in ANY Exareme node
        if (notFoundDatasets.size() != 0) {
            StringBuilder notFound = new StringBuilder();
            for (String ds : notFoundDatasets)
                notFound.append(ds).append(", ");
            String notFoundSring = notFound.toString();
            notFoundSring = notFoundSring.substring(0, notFoundSring.length() - 2);
            //Show appropriate error message to user
            throw new DatasetsException("Dataset(s) " + notFoundSring + " not found for pathology " +pathology + "!");
        }
        return nodesToBeChecked;
    }

    private String preALgoExecutionChecks(HttpRequest request) throws UnsupportedHttpVersionException {
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

    private HashMap<String, String> getAlgoParameters(HttpRequest request) throws IOException {

        log.debug("Parsing content ...");
        HashMap<String, String> inputContent = new HashMap<>();
        List<Map> parameters = new ArrayList();
        String content;

        if (request instanceof HttpEntityEnclosingRequest) {
            log.debug("Streaming ...");
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            content = EntityUtils.toString(entity);
            if (content != null && !content.isEmpty()) {
                parameters = new Gson().fromJson(content, List.class);
            }
        }
        if (!parameters.isEmpty()) {
            log.debug("All of the parameters: " + parameters);
            for (Map k : parameters) {
                String name = (String) k.get("name");
                String value = (String) k.get("value");
                if (name == null || name.isEmpty() || value == null || value.isEmpty()) continue;

                log.debug("Parameter in the json: ");
                log.debug(name + " = " + value);

                value = value.replaceAll("[^A-Za-z0-9,._~*+><=&|(){}:\\-\\\"\\[\\]]", "");    // ><=&| we no more need those for filtering
                value = value.replaceAll("\\s+", "");

                log.debug("Parameter after format: ");
                log.debug(name + " = " + value);

                inputContent.put(name, value);
            }
            return inputContent;
        }
        return null;
    }

    private boolean nodesRunning(List<String> nodesToBeChecked, String pathology) throws Exception {
        Gson gson = new Gson();
        //Check if IP's gotten from Consul[Key-Value store] exist in Exareme's Registry
        List<String> notContainerProxy = new ArrayList<>();
        ContainerProxy[] containerProxy = ArtRegistryLocator.getArtRegistryProxy().getContainers();     //get IP's from Exareme's Registry
        for (String IP : nodesToBeChecked) {
            log.debug("Node to be checked: "+IP);
            boolean flag = false;
            for (ContainerProxy containers : containerProxy) {
                if (containers.getEntityName().getIP().contains(IP)) {                      //If IP exists in Exareme's Registry
                    log.debug("Container in registry: " + containers.getEntityName().getIP());
                    flag = true;
                    break;
                }
            }
            if (!flag) {                                                                    //IP is not in Exareme's Registry
                notContainerProxy.add(IP);
            }
        }
        HashMap<String, String> names = getNamesOfActiveNodes();        //Get Node IP-> Node Name from Consul[Key-Value store]
        if (notContainerProxy.size() != 0) {                            //If there are IPs that are not in Exareme's registry
            String existingDatasetsSring;
            String nodesNotFound;
            List<String> datasetsFound = new ArrayList<>();
            StringBuilder nodes = new StringBuilder();
            StringBuilder datasets = new StringBuilder();
            for (String ip : notContainerProxy) {       //maybe more than one Exareme nodes
                String name = names.get(ip);
                log.info("It seems that node[" + name + "," + ip + "] you are trying to check is not part of Exareme's registry. Deleting it from Consul....");

                //Delete pathologies and IP of the node
                String pathologyKey = searchConsul(System.getenv("DATA") + "/" + name + "?keys");
                String[] pathologyKeyArray = gson.fromJson(pathologyKey, String[].class);
                for (String p : pathologyKeyArray) {
                    deleteFromConsul(p);            //Delete every pathology for node with name $name
                }
                //Delete IP of active_worker with name $name
                deleteFromConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/" + name);

                //Get datasets exist in other nodes for showing appropriate message to user
                HashMap<String, String[]> nodeDatasets;
                try {
                    nodeDatasets = getDatasetsFromConsul(pathology);
                }
                catch (PathologyException e) {
                    throw new PathologyException(e.getMessage());
                }
                catch (Exception e) {
                    throw new Exception("An error has occurred.Please inform your system admin.");
                }
                for (Map.Entry<String, String[]> entry : nodeDatasets.entrySet()) {
                    String[] getDatasets = entry.getValue();
                    for (String data : getDatasets) {
                        if (!datasetsFound.contains(data)) {
                            datasets.append(data).append(", ");         //proper error message showing which datasets the user could use [no duplicates]
                            datasetsFound.add(data);
                        }
                    }
                }
                nodes.append(name).append(", ");                        //proper error message showing which nodes(names) had issues
            }
            existingDatasetsSring = datasets.substring(0, datasets.length() - 2);
            nodesNotFound = nodes.substring(0, nodes.length() - 2);
            throw new Exception("Node/(S) with name/(s) " + nodesNotFound + " is/(are) not responding." +
                    " Until the issue is fixed, you can re-run your Experiment using one or more dataset(s):" +
                    existingDatasetsSring);
        }

        //If IPs are in Exareme's registry
        for (String IP : nodesToBeChecked) {
            log.debug("Will check container with IP: " + IP + " in order to see if a connection can be established..");       //check that a connection with the Exareme node can be established
            for (ContainerProxy containers : containerProxy) {
                if (containers.getEntityName().getIP().equals(IP)) {
                    try {
                        ContainerProxy tmpContainerProxy = ArtRegistryLocator.getArtRegistryProxy().lookupContainer(containers.getEntityName());
                        break;
                    } catch (RemoteException e) {
                        //TODO we need to catch the RemoteException here. The fix in NQueryResultEntity.java is temporal
                    }
                }
            }
        }
        return true;
    }

    //Get all IP's from Exareme's registry
    private List<String> allNodesIPs() throws RemoteException {
        List<String> allIPs = new ArrayList<>();
        for (ContainerProxy containerProxy : ArtRegistryLocator.getArtRegistryProxy().getContainers()) {
            allIPs.add(containerProxy.getEntityName().getIP());
        }
        return allIPs;
    }

    //Consul[Key-Value store] consists of information like IP's of Exareme nodes, Datasets existing at each node.
    private String searchConsul(String query) throws IOException {
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String consulURL = System.getenv("CONSULURL");
        if (consulURL == null) throw new IOException("Consul url not set");
        if (!consulURL.startsWith("http://")) {
            consulURL = "http://" + consulURL;
        }

        HttpGet httpGet;
        httpGet = new HttpGet(consulURL + "/v1/kv/" + query);
        log.debug("Running: " + httpGet.getURI());
        CloseableHttpResponse response = null;
        if (httpGet.toString().contains(System.getenv("EXAREME_MASTER_PATH") + "/") || httpGet.toString().contains(System.getenv("DATA") + "/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/master* or http://exareme-keystore:8500/v1/kv/datasets*
            try {   //then throw exception
                response = httpclient.execute(httpGet);
            } catch (Exception e) {
		        response.close();
            }
	        result = EntityUtils.toString(response.getEntity());
        }
        if (httpGet.toString().contains(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/active_workers*
               //then maybe there are no workers running
            try {
                response = httpclient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() != 200) {
                    if (httpGet.toString().contains("?keys"))
                        log.debug("No workers running. Continue with master");
                    } else {
                        result = EntityUtils.toString(response.getEntity());
                    }
                }
            catch (Exception e){
                response.close();
            }
        }
        return result;
    }

    //Some times infos regarding Exareme nodes exist in Consul-Key-Value store], but the nodes are not part of Exareme's registry. We delete the infos from Consul[Key-Value store]
    private void deleteFromConsul(String query) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String consulURL = System.getenv("CONSULURL");
        if (consulURL == null) throw new IOException("Consul url not set");
        if (!consulURL.startsWith("http://")) {
            consulURL = "http://" + consulURL;
        }
        HttpDelete httpDelete;
        httpDelete = new HttpDelete(consulURL + "/v1/kv/" + query);

        //curl -X DELETE $CONSULURL/v1/kv/$DATASETS/$NODE_NAME
        //curl -X DELETE $CONSULURL/v1/kv/$1/$NODE_NAME

        log.debug("Running: " + httpDelete.getURI());

        CloseableHttpResponse response = null;
        if (httpDelete.toString().contains(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/") || httpDelete.toString().contains(System.getenv("DATA") + "/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/master* or http://exareme-keystore:8500/v1/kv/datasets*
            try {   //then throw exception
                response = httpclient.execute(httpDelete);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new ServerException("Cannot contact consul", new Exception(EntityUtils.toString(response.getEntity())));
                }
            } finally {
                response.close();
            }
        }
    }

    private String defaultOutputFormat(String data, String type) {
        return "{\"result\" : [{\"data\":" + "\"" + data + "\",\"type\":" + "\"" + type + "\"}]}";
    }
}
