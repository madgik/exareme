/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.lru;

import madgik.exareme.master.engine.remoteQuery.impl.cache.CachedDataInfo;

/**
 * @author christos
 */
public class ListNode {

    public String query;
    public CachedDataInfo cacheInfo;

    public ListNode(String query, CachedDataInfo cacheInfo) {

        this.query = query;
        this.cacheInfo = cacheInfo;
    }
}
