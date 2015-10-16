/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache.implementation.federation;

import madgik.exareme.master.engine.remoteQuery.impl.cache.QueryRequests;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public class QueryInfo {

    public QueryRequests requestInfo;
    public double benefit;
    public String storageTime;
    public String lastUpdate;

    public QueryInfo(QueryRequests requestInfo, double benefit) {
        this.requestInfo = requestInfo;
        this.benefit = benefit;
    }

    public QueryInfo(QueryRequests requestInfo, double benefit, String storageTime,
        String lastUpdate) {
        this.requestInfo = requestInfo;
        this.benefit = benefit;
        this.storageTime = storageTime;
        this.lastUpdate = lastUpdate;
    }
}
