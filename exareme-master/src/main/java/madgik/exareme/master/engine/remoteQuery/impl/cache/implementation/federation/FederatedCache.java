/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation;

import madgik.exareme.master.engine.remoteQuery.impl.bootstrapping.Bootstrap;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.QueryRequests;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.QueryCache;
import madgik.exareme.master.engine.remoteQuery.impl.metadata.Metadata;
import madgik.exareme.master.engine.remoteQuery.impl.utility.Files;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation.Benefit.computeBenefit;
import static madgik.exareme.master.engine.remoteQuery.impl.utility.Sets.powerset;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public class FederatedCache implements QueryCache {

    private final Metadata metadata;
    private final CacheInfo cacheInfo;
    private final String directory;
    private double a = 1.0;
    private double b = 0.0;
    private final HashMap<String, QueryRequests> requestsMap;
    private final TreeMap<Double, LinkedList<String>> benefitMap;
    private final HashMap<String, LinkedList<Node>> pinMap;

    public FederatedCache(String directory, double cacheSize, Metadata metadata) throws Exception {

        this.metadata = metadata;
        this.directory = directory;

        File file = Files.createDir(directory);

        cacheInfo = new CacheInfo(directory, cacheSize, 0);
        cacheInfo.totalSize = cacheSize;

        requestsMap = new HashMap<String, QueryRequests>();
        benefitMap = new TreeMap<Double, LinkedList<String>>();
        pinMap = new HashMap<String, LinkedList<Node>>();
    }

    public void setParameters(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public void setParameterA(double a) {
        this.a = a;
    }

    public void setParameterB(double b) {
        this.b = b;
    }

    public double getParameterA() {
        return a;
    }

    public double getParameterB() {
        return b;
    }

    public boolean fitInCache(List<String> queries, String excludedQuery) {

        double requiredSize = 0, excludedSize;
        for (String query : queries) {
            requiredSize += pinMap.get(query).getLast().info.size;
        }

        if (excludedQuery == null) {
            excludedSize = 0;
        } else {
            excludedSize = pinMap.get(excludedQuery).getLast().info.size;
        }

        return requiredSize <= cacheInfo.totalSize - excludedSize;
    }

    @Override public QueryInfo getQueryInfo(String query) {

        QueryInfo info;
        info = new QueryInfo(requestsMap.get(query), pinMap.get(query).getLast().info.benefit);

        return info;

    }

    @Override public void boot(Bootstrap bootstrap, String storagePath) {

        CacheInfo info = null;

        System.out.println("arxi boot federated");

        try {
            info = bootstrap.getCacheInfo();
            if (info != null) {
                cacheInfo.currentSize = info.currentSize;
                System.out.println("cache size " + cacheInfo.currentSize);
                if (!directory.equals(info.directory)) {
                    metadata.updateCacheDirectory(directory);
                }
                info = null;
            } else {
                this.metadata.initializeCacheInfo(directory);
            }
        } catch (SQLException ex) {
            return;
        }

        List<CachedDataInfo> list = bootstrap.getCacheIndexList(cacheInfo, storagePath);

        QueryRequests requestsInfo;
        LinkedList<Node> listNode;

        for (CachedDataInfo cachedDataInfo : list) {

            System.out.println("infosss");

            requestsInfo = cachedDataInfo.requests;
            requestsMap.put(cachedDataInfo.query, requestsInfo);

            listNode = new LinkedList<Node>();
            listNode.add(new Node(cachedDataInfo));
            pinMap.put(cachedDataInfo.query, listNode);

            LinkedList<String> benefitQueries;
            if (benefitMap.containsKey(cachedDataInfo.benefit)) {
                benefitQueries = benefitMap.get(cachedDataInfo.benefit);
            } else {
                benefitQueries = new LinkedList<String>();
                benefitMap.put(cachedDataInfo.benefit, benefitQueries);
            }
            benefitQueries.add(cachedDataInfo.query);
        }

        System.out.println("telos boot");
    }

    @Override public CacheInfo getInfo() {
        return cacheInfo;
    }

    @Override public double getTotalCacheSize() {
        return cacheInfo.totalSize;
    }

    @Override public void setTotalCacheSize(double size) {
        cacheInfo.totalSize = size;
    }

    @Override public CachedDataInfo getCacheInfo(String query) {

        if (!pinMap.containsKey(query)) {
            return null;
        }
        return pinMap.get(query).getLast().info;
    }

    @Override public void setCacheInfo(CachedDataInfo info) {

        int queryResponseTime = info.requests.queryResponseTime;
        boolean newVersion = false;

        try {
            LinkedList<Node> listNodes;
            Node node = new Node(info);

            double benefit;
            QueryRequests queryRequests;

            if (pinMap.containsKey(info.query)) {
                listNodes = pinMap.get(info.query);
                queryRequests = requestsMap.get(info.query);
                queryRequests.updateVersion(queryResponseTime);
                newVersion = true;
            } else {
                listNodes = new LinkedList<Node>();
                pinMap.put(info.query, listNodes);
                queryRequests = new QueryRequests(queryResponseTime);
                requestsMap.put(info.query, queryRequests);
            }
            listNodes.add(node);
            updateTotalRequests();

            cacheInfo.currentSize += info.size;
            removeOldVersions(listNodes, info.query);

            benefit = computeBenefit(a, queryRequests);

            LinkedList<String> queries;
            if (benefitMap.containsKey(benefit)) {
                queries = benefitMap.get(benefit);
            } else {
                queries = new LinkedList<String>();
                benefitMap.put(benefit, queries);
            }
            queries.add(info.query);

            if (newVersion) {
                updateBenefit(info.query, benefit);
            } else {
                info.benefit = benefit;
            }

            if (cacheInfo.currentSize > cacheInfo.totalSize && requestsMap.size() > 1) {
                replace(info.query);
            }

            metadata.updateCacheSize(cacheInfo.currentSize);

        } catch (IOException ex) {
            Logger.getLogger(FederatedCache.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(FederatedCache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override public void updateCache(String query) {

        try {
            double previousBenefit;

            QueryRequests queryRequests = requestsMap.get(query);
            queryRequests.updateRequests();
            updateTotalRequests();

            Node node = pinMap.get(query).getLast();
            node.numberOfPins++;
            previousBenefit = node.info.benefit;

            node.info.benefit = computeBenefit(a, queryRequests);

            benefitMap.get(previousBenefit).remove(query);
            if (benefitMap.get(previousBenefit).isEmpty()) {
                benefitMap.remove(previousBenefit);
            }

            LinkedList<String> queries;
            if (benefitMap.containsKey(node.info.benefit)) {
                queries = benefitMap.get(node.info.benefit);
            } else {
                queries = new LinkedList<String>();
                benefitMap.put(node.info.benefit, queries);
            }
            queries.add(query);
        } catch (SQLException ex) {
            Logger.getLogger(FederatedCache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override public void pinQuery(String query, boolean firstRequestOfTheBatch) {
        Node node = pinMap.get(query).getLast();
        node.numberOfPins++;
        if (!firstRequestOfTheBatch) {
            try {
                //System.out.println("mpikaaaaaaaaaaaaaaaaaaaa!");
                QueryRequests request = requestsMap.get(query);
                request.updateRequests();
                updateTotalRequests();
                double benefit = computeBenefit(a, request);
                //System.out.println("benefit: " + benefit);
                pinMap.get(query).getLast().info.benefit = benefit;
                metadata.updateCacheRecord(query, benefit, request);
            } catch (SQLException ex) {
                Logger.getLogger(FederatedCache.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override public void unpinQueryResults(String query, CachedDataInfo info) {

        List<Node> nodes = pinMap.get(query);

        for (Node node : nodes) {
            if (node.info.database.equals(info.database)) {
                node.numberOfPins--;

                List<Node> singleNode = new LinkedList<Node>();
                singleNode.add(node);
                try {
                    removeOldVersions(singleNode, query);
                    metadata.updateCacheSize(cacheInfo.currentSize);
                } catch (IOException ex) {
                    Logger.getLogger(FederatedCache.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(FederatedCache.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
    }

    @Override public boolean isPinned(String query, CachedDataInfo info) {

        LinkedList<Node> queryNodes = pinMap.get(query);
        for (Node node : queryNodes) {
            if (node.info.database.equals(info.database)) {
                return node.numberOfPins != 0;
            }
        }
        return false;
    }

    @Override public boolean isEmpty() {
        return requestsMap.isEmpty();
    }

    private void removeOldVersions(List<Node> list, String query) throws IOException, SQLException {

        for (Node node : list) {
            if (node != pinMap.get(query).getLast() && node.numberOfPins == 0) {
                //System.out.println("remove to version apo to query " + node.info.query);
                removeDB(node);
                pinMap.get(query).remove(node);
            }
        }
    }

    private void removeDB(Node node) throws IOException, SQLException {

        cacheInfo.currentSize -= node.info.size;
        Files.deleteDB(node.info, metadata);
    }

    private void updateBenefit(String query, double benefit) {

        if (pinMap.get(query).size() > 1) {

            double previousBenefit = pinMap.get(query).get(0).info.benefit;
            //      System.out.println("benefit for remove " + previousBenefit + " apo to query "
            //              + query);

            LinkedList<String> queries = benefitMap.get(previousBenefit);
            queries.remove(query);
            if (queries.isEmpty()) {
                benefitMap.remove(previousBenefit);
            }
        }

        List<Node> queryNodes = pinMap.get(query);
        for (Node node : queryNodes) {
            node.info.benefit = benefit;
        }
    }

    private void updateTotalRequests() throws SQLException {

        int numberOfAdditionalRequests = requestsMap.size();

        for (String query : requestsMap.keySet()) {
            requestsMap.get(query).updateTotalRequests();
        }
        metadata.updateNumberTotalRequests();
    }

    private void replace(String newQuery) throws IOException, SQLException {

        //    System.out.println("ReplaceMent!!!!!");

        HashMap<String, Double> hashBenefitMap = new HashMap<String, Double>();
        Collection<String> queryList = new LinkedList<String>();

        for (Double benefit : benefitMap.keySet()) {
            for (String query : benefitMap.get(benefit)) {
                if (pinMap.get(query).getLast().numberOfPins == 0 && !query.equals(newQuery)) {
                    hashBenefitMap.put(query, benefit);
                    queryList.add(query);
                }
            }
        }

        List<List<String>> querySets = powerset(queryList);

        Benefit.setBenefitMap(hashBenefitMap);
        List<String> removedQueries = Benefit.maximizeBenefit(querySets, this, newQuery);

        Node node;
        LinkedList<String> remainedQueries;
        double benefit;

        for (String query : removedQueries) {

            node = pinMap.get(query).getLast();
            removeDB(node);

            benefit = pinMap.get(query).getLast().info.benefit;

            remainedQueries = benefitMap.get(benefit);
            remainedQueries.remove(query);
            if (remainedQueries.isEmpty()) {
                benefitMap.remove(benefit);
            }

            List<Node> nodes = pinMap.get(query);
            nodes.remove(node);
            if (nodes.isEmpty()) {
                pinMap.remove(query);
            }
            requestsMap.remove(query);
        }
    }
}
