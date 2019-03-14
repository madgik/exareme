/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class QueryRequests {

    public int numberOfRequests;
    public int numberOfLastVersionRequests;
    public int numberOfTotalRequests;
    public int queryResponseTime;
    public int numberOfVersions;

    public QueryRequests() {
        numberOfRequests = 0;
        numberOfLastVersionRequests = 0;
        numberOfTotalRequests = 0;
        queryResponseTime = 0;
        numberOfVersions = 0;
    }

    public QueryRequests(int numberOfRequests, int numberOfLastVersionRequests,
                         int numberOfTotalRequests, int queryResponseTime, int numberOfVersions) {

        this.numberOfRequests = numberOfRequests;
        this.numberOfLastVersionRequests = numberOfLastVersionRequests;
        this.numberOfTotalRequests = numberOfTotalRequests;
        this.queryResponseTime = queryResponseTime;
        this.numberOfVersions = numberOfVersions;

    }

    public QueryRequests(int queryResponseTime) {

        this.numberOfRequests = 1;
        this.numberOfTotalRequests = 0;
        this.numberOfLastVersionRequests = 1;
        this.numberOfVersions = 1;
        this.queryResponseTime = queryResponseTime;
    }

    public void updateRequests() {
        this.numberOfLastVersionRequests++;
        this.numberOfRequests++;
    }

    public void updateVersion(int queryResponseTime) {

        this.numberOfLastVersionRequests = 1;
        this.numberOfRequests++;
        this.numberOfVersions++;
        this.queryResponseTime += queryResponseTime;
    }

    public void updateTotalRequests() {
        this.numberOfTotalRequests++;
    }
}
