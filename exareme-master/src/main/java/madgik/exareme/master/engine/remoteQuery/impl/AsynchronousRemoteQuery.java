/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl;

import madgik.exareme.master.engine.remoteQuery.RemoteQuery;
import madgik.exareme.master.engine.remoteQuery.RemoteQueryListener;
import madgik.exareme.master.engine.remoteQuery.ServerInfo;
import madgik.exareme.master.engine.remoteQuery.impl.bootstrapping.Bootstrap;
import madgik.exareme.master.engine.remoteQuery.impl.bootstrapping.RemoteQueryBootstrap;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheAlgorithm;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.QueryRequests;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.QueryCache;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation.FederatedCache;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation.QueryInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.lru.LRUCache;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.unlimited.UnlimitedCache;
import madgik.exareme.master.engine.remoteQuery.impl.metadata.Metadata;
import madgik.exareme.master.engine.remoteQuery.impl.metadata.RemoteQueryMetadata;
import madgik.exareme.master.engine.remoteQuery.impl.queryExecution.QueryExecution;
import madgik.exareme.master.engine.remoteQuery.impl.queryExecution.QueryExecutionImplementation;
import madgik.exareme.master.engine.remoteQuery.impl.utility.Date;
import madgik.exareme.master.engine.remoteQuery.impl.utility.Files;
import madgik.exareme.master.engine.remoteQuery.impl.utility.QueryParser;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.association.Triple;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public final class AsynchronousRemoteQuery implements RemoteQuery, RemoteQueryInternal {

    //simulation data
    private int cacheHit = 0;
    private int cacheMiss = 0;

    //Variables' declaration
    private String cacheDir = "/home/christos/cacheDirectory";
    private String storageDir = "/home/christos/storageDirectory";
    private boolean inOperation = true;
    private long tableID;
    private final File storageDirectory;
    private final Metadata metadata;
    private QueryCache cache;
    //Indexes' declaration
    private final HashSet<ServerInfo> serverUsage;
    private final HashMap<ServerInfo, LinkedList<Triple<String, File, ProcessManager>>>
        serversOnDemand;
    private final HashMap<String, LinkedList<RemoteQueryListener>> listenersPerQuery;
    private final HashMap<String, String> DBNameDemands;

    //Log Declaration
    private static final Logger log = Logger.getLogger(AsynchronousRemoteQuery.class);
    private final Object lock = new Object();

    @Override public void printData() {
        synchronized (lock) {
            System.out.println("cache hit " + cacheHit);
            System.out.println("cache miss " + cacheMiss);
        }
    }

    public AsynchronousRemoteQuery(File metadataPath, File storagePath, File cachePath)
        throws Exception {

        serverUsage = new HashSet<>();
        serversOnDemand = new HashMap<>();
        listenersPerQuery = new HashMap<>();
        DBNameDemands = new HashMap<>();

        if (cachePath != null) {
            cacheDir = cachePath.getAbsolutePath();
        }

        String file = null;
        if (metadataPath != null) {
            file = metadataPath.getAbsolutePath();
        }

        if (storagePath != null) {
            storageDirectory = storagePath;
        } else {
            storageDirectory = Files.createDir(storageDir);
        }

        //Creation of the metadata module
        metadata = new RemoteQueryMetadata(file);

        //creation of the cache module
        cache = new LRUCache(cacheDir, 100000, metadata);

        Bootstrap bootstrap = new RemoteQueryBootstrap();
        boot(bootstrap);
    }

    public AsynchronousRemoteQuery(File metadataPath, File storagePath, File cachePath,
        CacheAlgorithm algorithm) throws Exception {

        serverUsage = new HashSet<>();
        serversOnDemand = new HashMap<>();
        listenersPerQuery = new HashMap<>();
        DBNameDemands = new HashMap<>();

        if (cachePath != null) {
            cacheDir = cachePath.getAbsolutePath();
        }

        String file = null;
        if (metadataPath != null) {
            file = metadataPath.getAbsolutePath();
        }

        if (storagePath != null) {
            storageDirectory = storagePath;
        } else {
            storageDirectory = Files.createDir(storageDir);
        }

        //Creation of the metadata module
        metadata = new RemoteQueryMetadata(file);

        //creation of the cache module
        switch (algorithm) {

            case unlimited:
                try {
                    cache = new UnlimitedCache(cacheDir, metadata);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
                break;
            case lru:
                try {
                    cache = new LRUCache(cacheDir, 100000, metadata);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
                break;
            case federated:
                try {
                    cache = new FederatedCache((cacheDir), 100000, metadata);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
                break;
            default:
                break;
        }

        Bootstrap bootstrap = new RemoteQueryBootstrap();
        boot(bootstrap);
    }

    public AsynchronousRemoteQuery(File metadataPath, File storagePath, File cachePath,
        CacheAlgorithm algorithm, int storageSize) throws Exception {

        serverUsage = new HashSet<>();
        serversOnDemand = new HashMap<>();
        listenersPerQuery = new HashMap<>();
        DBNameDemands = new HashMap<>();

        if (cachePath != null) {
            cacheDir = cachePath.getAbsolutePath();
        }

        String file = null;
        if (metadataPath != null) {
            file = metadataPath.getAbsolutePath();
        }

        if (storagePath != null) {
            storageDirectory = storagePath;
        } else {
            storageDirectory = Files.createDir(storageDir);
        }

        //Creation of the metadata module
        metadata = new RemoteQueryMetadata(file);

        //creation of the cache module
        switch (algorithm) {

            case unlimited:
                try {
                    cache = new UnlimitedCache(cacheDir, metadata);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
                break;
            case lru:
                try {
                    cache = new LRUCache(cacheDir, 100000, metadata);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
                break;
            case federated:
                try {
                    cache = new FederatedCache((cacheDir), 100000, metadata);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
                break;
            default:
                break;
        }

        Bootstrap bootstrap = new RemoteQueryBootstrap();
        boot(bootstrap);
    }

    @Override public void setReplacementAlgorithm(CacheAlgorithm algorithm, int storageSize) {

        switch (algorithm) {

            case unlimited:
                try {
                    cache = new UnlimitedCache(cacheDir, metadata);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
                break;
            case lru:
                try {
                    cache = new LRUCache(cacheDir, storageSize, metadata);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
                break;
            case federated:
                try {
                    cache = new FederatedCache((cacheDir), storageSize, metadata);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
                break;
            default:
                break;
        }
    }


    /*
     * Function which makes a query request
     */
    @Override public void schedule(ServerInfo server, String query, RemoteQueryListener listener,
        ProcessManager procManager, String DBName) throws RemoteException, IOException {

        try {
            scheduleQuery(server, query, listener, procManager, DBName, null);
        } catch (ParseException | SQLException | InterruptedException ex) {
            java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.
                getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Function which makes a query request along with a stale limit
     * which is measured in hours
     */
    @Override public void schedule(ServerInfo server, String query, RemoteQueryListener listener,
        ProcessManager procManager, String DBName, double staleLimit)
        throws RemoteException, IOException {

        try {
            scheduleQuery(server, query, listener, procManager, DBName, staleLimit);
        } catch (ParseException | SQLException | InterruptedException ex) {
            java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.
                getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Function which is called by the listener and informs RQI that it
     * no longer needs the results of the query
     */
    @Override public void finished(String query, CachedDataInfo cacheInfo) throws RemoteException {

        synchronized (lock) {

            query = QueryParser.parsing(query);

      /*System.out.println("Paw gia finish!!!");*/
            cache.unpinQueryResults(query, cacheInfo);
            if (!cache.isPinned(query, cacheInfo)) {
        /*System.out.println("Eina unpin to " + query);*/
                if (!inOperation && serverUsage.isEmpty() && cache.isEmpty()) {
                    shutDown();
                }
            } else {
        /*System.out.println("Pleon einai pinned");*/
            }
        }
    }

    @Override public void close() {
        inOperation = false;
    }

    /*
     * Function which saves the result of a query
     */
    public void scheduleQuery(ServerInfo server, String query, RemoteQueryListener listener,
        ProcessManager processManager, String DBName, Double staleLimit)
        throws RemoteException, ParseException, SQLException, IOException, InterruptedException {

        //    log.debug("This is the start of the schedule");
        String madisMainDB = null, outputTable = null;
        Boolean firstQueryRequest;
        Boolean readFromCache = false, inCache = false;
        CachedDataInfo locationOfCachedResults;
        Triple<String, File, ProcessManager> queryInfo;
        QueryExecution queryExecutor;

        synchronized (lock) {

            if (!inOperation) {
                throw new RemoteException("The RQI is out of order");
            }

            String current_datetime = Date.getCurrentDateTime();

            query = QueryParser.parsing(query);

            locationOfCachedResults = cache.getCacheInfo(query);
            //      log.trace("cache return " + locationOfCachedResults);
            System.out.println("cache return " + locationOfCachedResults);

            if (locationOfCachedResults != null) {
                inCache = true;
                if (staleLimit == null
                    || Date.getDifferenceInSec(locationOfCachedResults.storageTime) < staleLimit) {
                    readFromCache = true;
                }
            }

            //The query results are in cache and it not stale
            if (readFromCache) {

                String command = "ln -s " + locationOfCachedResults.cacheInfo.directory + "/"
                    + locationOfCachedResults.database + " " + storageDirectory + "/" + DBName;

                if (!DBName.equals(locationOfCachedResults.database)) {
                    queryExecutor = new QueryExecutionImplementation(this);
                    queryExecutor.executeQuery(new File(cache.getInfo().directory),
                        locationOfCachedResults.database, DBName, processManager);
                }

                System.out.println("command " + command);

                Process process = Runtime.getRuntime().exec(command);

                cacheHit++;
                System.out.println("In cache");
                //        log.debug("The results of the query " + query + " are already "
                //                + "cached in the file "
                //                + locationOfCachedResults.cacheInfo.directory
                //                + "/" + locationOfCachedResults.database
                //                + " at the table " + locationOfCachedResults.table);
        /*System.out.println("read from cache");*/

                System.out.println(
                    "The results of the query " + query + " are already " + "cached in the file "
                        + locationOfCachedResults.cacheInfo.directory + "/"
                        + locationOfCachedResults.database + " at the table "
                        + locationOfCachedResults.table);
                cache.updateCache(query);

                listener.finished(locationOfCachedResults, null);

                QueryInfo info;
                try {
                    info = cache.getQueryInfo(query);
                    if (info != null) {
                        metadata.updateCacheRecord(query, info.benefit, info.requestInfo);
                    } else {
                        metadata.updateCacheRecord(query, 0, null);
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                        .log(Level.SEVERE, null, ex);
                }
            } else {

                cacheMiss++;

                firstQueryRequest = false;
                if (!listenersPerQuery.containsKey(query)) {
                    if (!inCache) {
                        //            log.trace("arxikopoiisi gia to " + query);
                        System.out.println("arxikopoiisi gia to " + query);
                    } else {
                        //            log.trace("New version for " + query);
                        System.out.println("New version for " + query);
                    }
                    listenersPerQuery.put(query, new LinkedList<RemoteQueryListener>());
                    firstQueryRequest = true;
                }
                listenersPerQuery.get(query).add(listener);

                //First request for execution of the query and the server is
                //available
                if (!serverUsage.contains(server) && firstQueryRequest == true) {
                    //          log.trace("mpika sto execute gia to " + query);
                    System.out.println("mpika sto execute gia to " + query);
                    //serverUsage.add(server); //edw

                    if (DBName != null) {
                        madisMainDB = DBName;
                    } else {
                        madisMainDB = getDBName();
                    }
                    //          outputTable = getTableName();
                    outputTable = madisMainDB.replaceAll("\\.0\\.db", "");
                    System.out.println("outputTable " + outputTable);


                    queryExecutor = new QueryExecutionImplementation(this);
                    //                    queryExecutor.executeQuery(server, query, storageDirectory,
                    //                            madisMainDB, outputTable, processManager);
                    queryExecutor.executeQuery(server, query, new File(cache.getInfo().directory),
                        madisMainDB, outputTable, storageDirectory.getAbsolutePath(),
                        processManager);
                } //First request for execution of the query but the server is not
                //available
                else if (firstQueryRequest == true) {

                    //          log.trace("mpika sto anamoni gia query apo ton srv");
                    System.out.println("mpika sto anamoni gia query apo ton srv");
                    if (!serversOnDemand.containsKey(server)) {
                        serversOnDemand
                            .put(server, new LinkedList<Triple<String, File, ProcessManager>>());
                    }
                    queryInfo = new Triple(query, storageDirectory, processManager);
                    serversOnDemand.get(server).add(queryInfo);
                    if (DBName != null) {
                        DBNameDemands.put(query, DBName);
                    }
                } //The query is already in process state
                else {
                    //          log.trace("apla perimenw na enimerwthw");
                    System.out.println("apla perimenw na enimerwthw");
                    cacheMiss--;
                    cacheHit++;
                }
            }
        }
    }

    /*
     * Function which shut down the RMI
     */
    private void shutDown() {
        try {
            metadata.close();
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.
                getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Function which saves the results of a query in cache
     */
    @Override public void queryCompletion(ServerInfo server, String query,
        String directoryOfStorage, String cacheDatabase, String cacheTable, int responseTime,
        int Qsize) throws IOException, SQLException {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName())
                .log(Level.SEVERE, null, ex);
        }

        double size;
        CachedDataInfo cacheInfo;
        QueryExecutionImplementation queryExecutor;
        LinkedList<Triple<String, File, ProcessManager>> serverDemands;
        String outputTable = null, madisMainDB = null;

        synchronized (lock) {

            //            File f = new File(directoryOfStorage + "/" + cacheDatabase);
            //            size = f.length();
            //
            //            //Create a link from cache directory to the file that the results
            //            //exist
            //            String command = "ln -s " + directoryOfStorage + "/" + cacheDatabase
            //                    + " " + cache.getInfo().directory + "/"
            //                    + cacheDatabase;

            File f = new File(cache.getInfo().directory + "/" + cacheDatabase);
            size = f.length();

            String command =
                "ln -s " + new File(cache.getInfo().directory) + "/" + cacheDatabase + " "
                    + directoryOfStorage + "/" + cacheDatabase;

            System.out.println("command " + command);

            Process process = Runtime.getRuntime().exec(command);
            //      size = Qsize;

            String currentTime = Date.getCurrentDateTime();
            QueryRequests request = new QueryRequests(responseTime);
            //query's results saved on the cacheFile
            cacheInfo = new CachedDataInfo(cacheDatabase, cacheTable, query,
                directoryOfStorage + "/" + cacheDatabase, currentTime, cache.getInfo(), currentTime,
                size);
            cacheInfo.setQueryRequests(request);
            cache.setCacheInfo(cacheInfo);

            //      log.debug("The results of the query have beedn saved to the"
            //              + " " + cacheInfo.cacheInfo.directory + "/" + cacheInfo.database
            //              + " at the table " + cacheInfo.table);

      /*System.out.println("The results of the query have beedn saved to the"
       + " " + cacheInfo.cacheInfo.directory + "/" + cacheInfo.database
       + " at the table " + cacheInfo.table);*/
            QueryInfo info = cache.getQueryInfo(query);
            if (info != null) {
                metadata.addNewCacheRecord(cacheInfo.database, cacheInfo.table, cacheInfo.query,
                    cacheInfo.size, info.benefit, info.requestInfo);
            } else {
                metadata.addNewCacheRecord(cacheInfo.database, cacheInfo.table, cacheInfo.query,
                    cacheInfo.size, 0, null);
            }

            //      log.debug("The metadata records have been saved");
            if (serverUsage
                .contains(server)) { //Just for normal bootstrapping if server had gone down
                //serverUsage.remove(server); //edw
            }

            //inform listeners which were waiting the results of the executed query
            if (listenersPerQuery
                .containsKey(query)) {  //Just for normal bootstrapping if server had gone down
                /*if (listenersPerQuery.get(query) == null) {
         System.out.println("Einai nul!!!!!!");
         }*/
                callListeners(query, cacheInfo, new LinkedList<>(listenersPerQuery.get(query)));
            }

            if (!serversOnDemand
                .containsKey(server)) {  //Just for normal bootstrapping if server had gone down
                return;
            }

            //if another query waits that server, then execute it
            serverDemands = serversOnDemand.get(server);
            if (serverDemands != null) {
                Triple<String, File, ProcessManager> queryOnDemand = serverDemands.get(0);

                if (queryOnDemand != null) {

                    serversOnDemand.get(server).remove(0);
                    if (serversOnDemand.get(server).isEmpty()) {
                        serversOnDemand.remove(server);
                    }

                    if (DBNameDemands.containsKey(queryOnDemand.a)) {
                        madisMainDB = DBNameDemands.get(queryOnDemand.a);
                    } else {
                        madisMainDB = getDBName();
                    }
                    outputTable = getTableName();

                    //serverUsage.add(server); //edw
                    try {
                        queryExecutor = new QueryExecutionImplementation(this);
                        //                        queryExecutor.executeQuery(server, queryOnDemand.a,
                        //                                queryOnDemand.b, madisMainDB, outputTable,
                        //                                queryOnDemand.c);
                        queryExecutor.executeQuery(server, queryOnDemand.a,
                            new File(cache.getInfo().directory), madisMainDB, outputTable,
                            queryOnDemand.b.getAbsolutePath(), queryOnDemand.c);
                    } catch (RemoteException ex) {
                        java.util.logging.Logger.getLogger(AsynchronousRemoteQuery.class.getName()).
                            log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                serversOnDemand.remove(server);
            }
            //      log.trace("telos me to query" + query);
        }
    }

    private void callListeners(String query, CachedDataInfo cacheInfo,
        LinkedList<RemoteQueryListener> listeners) {

        //    log.trace("mpika sto calllistner");
    /*System.out.println("mpika sto calllistner");*/
        int i = 1;
        if (listeners != null) {
            //      log.trace("arxi listener");
      /*System.out.println("arxi listener");*/

            boolean firstRequestOfBatch = true;

            for (RemoteQueryListener listener : listeners) {
                //        log.trace("listener " + Integer.toString(i));
        /*System.out.println("listener " + Integer.toString(i));*/
                i++;

                listenersPerQuery.get(query).remove(listener);
                if (listenersPerQuery.get(query).isEmpty()) {
                    listenersPerQuery.remove(query);
                }

                cache.pinQuery(query, firstRequestOfBatch);
                if (firstRequestOfBatch) {
                    firstRequestOfBatch = false;
                }
            }

            for (RemoteQueryListener listener : listeners) {
                listener.finished(cacheInfo, null);
            }
        }
    }

    /*
     * Bootstrapping of RQI
     */
    public void boot(Bootstrap bootstrap) throws SQLException {

        synchronized (lock) {
            Pair<String, Long> pair = bootstrap.getStorageDirectory();

            if (pair.a == null) {
                tableID = 0;
                metadata.addStorageDirectory(storageDirectory.toString());
            } else if (!pair.a.equals(storageDirectory.toString())) {
                tableID = pair.b;
                metadata.updateStorageDirectory(storageDirectory.toString());
            } else {
                tableID = pair.b;
            }

            System.out.println("prin");
            cache.boot(bootstrap, storageDirectory.toString());
            System.out.println("meta");

            bootstrap.close();
            bootstrap = null;
        }
    }

    /*
     * Function which returns the id of the new database
     */
    private String getDBName() throws SQLException {

        String madisMainDB = "DB" + tableID;
        tableID++;
        //System.out.println("enhmerwnw me to " + tableID);
        metadata.updateDBID(tableID);
        return madisMainDB;
    }

    /*
     * Function which returns the table name of the results of a query
     */
    private String getTableName() {

        String tableName = "storageTable";
        return tableName;
    }
}

//TIPS
//MadisMainDB einai to file/Database, opou tha apothikeutei to results
//tableName einai to table sto MadisMainDB opou tha apothikeutei to result
//Me to pou ftasei sti Cache, ta listenersPerQuery metatrepontai se pinsPerQuery
//kai opoiodipote neo request pou kaluptetai apo ti cache tha ginetai pinsPerQ.
//Epomenws an einai stale, gia na doume an exei ginei request, elegxoume mono
//an to listenersPerQuery einai adeio i oxi
