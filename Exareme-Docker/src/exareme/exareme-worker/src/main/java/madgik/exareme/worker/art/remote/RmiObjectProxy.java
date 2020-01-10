/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.remote;

import com.google.gson.Gson;
import madgik.exareme.common.art.entity.EntityName;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;

/**
 * University of Athens /
 * Department of Informatics and Telecommunications.
 *
 * @param <T>
 * @since 1.0
 */
public abstract class RmiObjectProxy<T> implements ObjectProxy<T> {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(RmiObjectProxy.class);
    private EntityName regEntityName = null;
    private String regEntryName = null;
    private transient T remoteObject = null;
    private transient boolean isConnected = false;

    public RmiObjectProxy(String regEntryName, EntityName regEntityName) {
        this.regEntryName = regEntryName;
        this.regEntityName = regEntityName;
    }

    @Override
    public synchronized T connect() throws RemoteException {
        int tries = 0;
        while (true) {
            try {
                log.trace("Connecting to (" + tries + ") " +
                        regEntityName.getIP() + ":" + regEntityName.getPort() + " ...");
                tries++;
                Registry registry = RmiRegistryCache.getRegistry(regEntityName);
                remoteObject = (T) registry.lookup(regEntryName);
                isConnected = true;
                return remoteObject;
            } catch (Exception e) {
                log.error("Cannot connect to " +
                        regEntityName.getIP() + ":" + regEntityName.getPort() + " ...", e);
                if (!getRetryPolicy().getRetryTimesPolicy().retry(e, tries)) {
                    break;
                }
                try {
                    Thread.sleep(getRetryPolicy().getRetryTimeInterval().getTime(tries));
                } catch (Exception ee) {
                    throw new AccessException("Cannot connect", ee);
                }
            }
        }
        throw new RemoteException(
                "Cannot connect to " + regEntityName.getIP() + ":" + regEntityName.getPort());
    }

    @Override
    public T getRemoteObject() throws RemoteException {
        String name = null;
        Iterator<Map.Entry<String, String>> entries;
        Gson gson = new Gson();
        Semaphore semaphore = new Semaphore(1);

        if (isConnected == false) {
            try {
                connect();      //try to connect to remote object. If the connection is failing, maybe java is not running
            } catch (RemoteException exception) {

                //Get the Exareme's node name that is not responding
                HashMap<String,String> names = null;
                try {
		        semaphore.acquire();
                    names = getNamesOfActiveNodes();
                    for (Map.Entry<String, String> entry : names.entrySet()) {
                        log.debug("ActiveNodes from Consul key-value store: " + entry.getKey() + " = " + entry.getValue());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                if(names != null) {
                    entries = names.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry<String, String> entry = entries.next();
                        if (Objects.equals(entry.getKey(), regEntityName.getIP())) {
                            name = entry.getValue();
                            log.info("Found node with name: "+name+" that seems to be down..");

                            try {
                                String pathologyKey = searchConsul(System.getenv("DATA") + "/" + name + "?keys");
                                String[] pathologyKeyArray = gson.fromJson(pathologyKey, String[].class);
                                for( String p: pathologyKeyArray) {
                                    deleteFromConsul(p);            //Delete every pathology for node with name $name
                                }
                                deleteFromConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH") + "/" + name);
                                log.info("Worker node:[" + name + "," + regEntityName.getIP() + "]" + " removed from Consul key-value store");
                            } catch (IOException E) {
                                throw new RemoteException("Can not contact Consul Key value Store");
                            }
                            break;
                        }
                    }
                }
                semaphore.release();
                throw new RemoteException("There was an error with worker "+ "["+ regEntityName.getIP() + "].");
            }
        }
        return remoteObject;
    }

    private HashMap <String,String> getNamesOfActiveNodes() throws IOException {
        Gson gson = new Gson();
        HashMap <String,String> map = new HashMap<>();
        String masterKey = searchConsul(System.getenv("EXAREME_MASTER_PATH")+"/?keys");
        String[] masterKeysArray = gson.fromJson(masterKey, String[].class);

        String masterName = masterKeysArray[0].replace(System.getenv("EXAREME_MASTER_PATH")+"/", "");
        String masterIP = searchConsul(System.getenv("EXAREME_MASTER_PATH")+"/"+masterName+"?raw");
        map.put(masterIP,masterName);

        String workersKey = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH")+"/?keys");
        if (workersKey == null)   //No workers running
            return map;             //return master only
        String[] workerKeysArray = gson.fromJson(workersKey, String[].class);
        for(String worker: workerKeysArray){
            String workerName = worker.replace(System.getenv("EXAREME_ACTIVE_WORKERS_PATH")+"/", "");
            String workerIP = searchConsul(System.getenv("EXAREME_ACTIVE_WORKERS_PATH")+"/"+workerName+"?raw");
            map.put(workerIP,workerName);
        }
        return map;
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
            if (httpGet.toString().contains(System.getenv("EXAREME_MASTER_PATH")+"/") || httpGet.toString().contains(System.getenv("DATA")+"/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/master* or http://exareme-keystore:8500/v1/kv/datasets*
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
            if (httpGet.toString().contains(System.getenv("EXAREME_ACTIVE_WORKERS_PATH")+"/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/active_workers*
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

    private String deleteFromConsul(String query) throws IOException {
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String consulURL = System.getenv("CONSULURL");
        if (consulURL == null) throw new IOException("Consul url not set");
        if (!consulURL.startsWith("http://")) {
            consulURL = "http://" + consulURL;
        }
        try {
            HttpDelete httpDelete;
            httpDelete = new HttpDelete(consulURL + "/v1/kv/" + query);

            //curl -X DELETE $CONSULURL/v1/kv/$DATASETS/$NODE_NAME
            //curl -X DELETE $CONSULURL/v1/kv/$1/$NODE_NAME
            log.debug("Running: " + httpDelete.getURI());
            CloseableHttpResponse response = null;
            if (httpDelete.toString().contains(System.getenv("EXAREME_ACTIVE_WORKERS_PATH")+"/") || httpDelete.toString().contains(System.getenv("DATA")+"/")) {    //if we can not contact : http://exareme-keystore:8500/v1/kv/master* or http://exareme-keystore:8500/v1/kv/datasets*
                try {   //then throw exception
                    response = httpclient.execute(httpDelete);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        throw new ServerException("Cannot contact consul", new Exception(EntityUtils.toString(response.getEntity())));
                    } else {
                        result = EntityUtils.toString(response.getEntity());
                    }
                } finally {
                    response.close();
                }
            }
        }finally {
            return result;
        }
    }

    @Override
    public RetryPolicy getRetryPolicy() throws RemoteException {
        return RetryPolicyFactory.defaultRetryPolicy();
    }
}

