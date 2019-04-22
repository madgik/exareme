/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.lru;

import madgik.exareme.master.engine.remoteQuery.impl.bootstrapping.Bootstrap;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CacheInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.QueryCache;
import madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation.QueryInfo;
import madgik.exareme.master.engine.remoteQuery.impl.doublyLinkedList.DoublyLinkedList;
import madgik.exareme.master.engine.remoteQuery.impl.doublyLinkedList.Node;
import madgik.exareme.master.engine.remoteQuery.impl.metadata.Metadata;
import madgik.exareme.master.engine.remoteQuery.impl.utility.Files;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */

/*
 * Cache which implements LRU as the replacement algorithmF
 */
public class LRUCache implements QueryCache {

    private final Metadata metadata;
    private final CacheInfo cacheInfo;
    private final String directory;
    private final DoublyLinkedList<ListNode> lruSequence;
    //private final HashMap<String, Node> cacheIndex;
    private final HashMap<String, LinkedList<Node>> cacheIndex;
    private static final Logger log = Logger.getLogger(LRUCache.class);

    public LRUCache(String dir, double totalSize, Metadata metadata) throws Exception {

        boolean created;

        directory = dir;

        cacheIndex = new HashMap<String, LinkedList<Node>>();
        lruSequence = new DoublyLinkedList<ListNode>();

        //Creation of cache directory
        File file = Files.createDir(directory);

        cacheInfo = new CacheInfo(directory, 0);
        cacheInfo.totalSize = totalSize;

        this.metadata = metadata;
    }

    @Override
    public void boot(Bootstrap bootstrap, String storagePath) {

        CacheInfo info = null;

        System.out.println("arxi boot lru");

        try {
            info = bootstrap.getCacheInfo();
            if (info != null) {
                cacheInfo.currentSize = info.currentSize;
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

        ListNode listNode;
        for (CachedDataInfo cachedDataInfo : list) {

            listNode = new ListNode(cachedDataInfo.query, cachedDataInfo);
            lruSequence.add(listNode);

            cacheIndex.put(cachedDataInfo.query, new LinkedList<Node>());
            cacheIndex.get(cachedDataInfo.query).add(lruSequence.getLast());
        }
    }

    @Override
    public CacheInfo getInfo() {
        return cacheInfo;
    }

    @Override
    public double getTotalCacheSize() {
        return cacheInfo.totalSize;
    }

    @Override
    public void setTotalCacheSize(double size) {
        cacheInfo.totalSize = size;
    }

    @Override
    public CachedDataInfo getCacheInfo(String query) {

        Node node = null;

        if (query != null) {
            if (cacheIndex.containsKey(query)) {
                node = cacheIndex.get(query).getLast();
            } else {
                return null;
            }
        }

        if (node == null) {
            return null;
        }

        return ((ListNode) node.value).cacheInfo;
    }

    @Override
    public void updateCache(String query) {

        pinQuery(query, true);
        lruSequence.lruUpdate(cacheIndex.get(query).getLast());
    }

    @Override
    public void setCacheInfo(CachedDataInfo info) {

        try {

            ListNode listNode = new ListNode(info.query, info);

            lruSequence.add(listNode);
            if (!cacheIndex.containsKey(info.query)) {
                cacheIndex.put(info.query, new LinkedList<Node>());
            }
            cacheIndex.get(info.query).add(lruSequence.getLast());
            cacheInfo.currentSize += info.size;

            //Remove old unused query's instances
            removeOldVersions(cacheIndex.get(info.query), info.query);

            //      log.trace("Prin apo pithano delete");
            /*System.out.println("Prin apo pithano delete");*/
            /*lruSequence.printAll();*/
            /*System.out.println("Telos me print");*/
            //      log.trace("Telos me print");
            Node lruNode;
            while (cacheInfo.currentSize > cacheInfo.totalSize) {
                try {
                    lruNode = lruSequence.getFirstUnpinned();
                    if (lruNode == null) {
                        break;
                    } else if (lruNode == lruSequence.getLast()) {
                        break;
                    }

                    String query = ((ListNode) lruNode.value).query;

                    removeDB(lruNode);

                    /*System.out.println("Mpika gia to query " + query);*/
                    List<Node> list = cacheIndex.get(query);
                    boolean queryInstance = true;
                    String db = null, currentQuery;

                    for (Node node : list) {
                        currentQuery = ((ListNode) node.value).cacheInfo.database;

                        if (queryInstance) {
                            db = currentQuery;
                            queryInstance = false;
                        }
                        if (currentQuery.equals(db)) {
                            /*System.out.println("Diagrafi query " + query);*/
                            cacheIndex.get(query).remove(node);
                        }
                    }
                    if (cacheIndex.get(query).isEmpty()) {
                        /*System.out.println("Telika delete to query " + query);*/
                        cacheIndex.remove(query);
                    }

                    lruSequence.remove(lruNode);

                    //metadata.deleteCacheRecord(((ListNode) lruNode.value).cacheInfo.database);
                    //          log.trace("Meta apo delete");
                    /*System.out.println("Meta apo delete");*/
                    /*lruSequence.printAll();*/
                    //          log.trace("Telos me print");
                    /*System.out.println("Telos me print");*/
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(LRUCache.class.getName())
                            .log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LRUCache.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }

            try {
                metadata.updateCacheSize(cacheInfo.currentSize);
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(LRUCache.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(LRUCache.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(LRUCache.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void pinQuery(String query, boolean firstRequestOfTheBatch) {

        lruSequence.pin(cacheIndex.get(query).getLast());
    }

    @Override
    public boolean isPinned(String query, CachedDataInfo info) {

        LinkedList<Node> listNode = cacheIndex.get(query);

        for (Node node : listNode) {

            if (((ListNode) node.value).cacheInfo.database.equals(info.database)) {
                return lruSequence.isPinned(node);
            }
        }

        return false;
    }

    @Override
    public void unpinQueryResults(String query, CachedDataInfo info) {

        LinkedList<Node> listNode = cacheIndex.get(query);

        for (Node node : listNode) {

            if (((ListNode) node.value).cacheInfo.database.equals(info.database)) {
                lruSequence.unpin(node);
                //remove query instance if there is newer version
                listNode = new LinkedList<Node>();
                listNode.add(node);
                try {
                    removeOldVersions(listNode, query);
                    metadata.updateCacheSize(cacheInfo.currentSize);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(LRUCache.class.
                            getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LRUCache.class.
                            getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return cacheIndex.isEmpty();
    }

    private void removeDB(Node lruNode) throws IOException, SQLException {

        /*System.out.println("prin apo delete to db size einai " + cacheInfo.currentSize);*/
        cacheInfo.currentSize -= ((ListNode) lruNode.value).cacheInfo.size;
        /*System.out.println("meta apo delete to db size einai " + cacheInfo.currentSize);*/
        Files.deleteDB(lruNode, metadata);
    }

    private void removeOldVersions(List<Node> list, String query) throws IOException, SQLException {

        for (Node node : list) {
            if (node != cacheIndex.get(query).getLast() && !lruSequence.isPinned(node)) {

                removeDB(node);
                cacheIndex.get(query).remove(node);
                lruSequence.remove(node);
            }
        }
    }

    @Override
    public QueryInfo getQueryInfo(String query) {
        return null;
    }
}
