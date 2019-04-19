/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.streamClient;

import madgik.exareme.master.gateway.OptiqueStreamQueryMetadata.StreamRegisterQuery;

import java.rmi.RemoteException;


/**
 * @author Christoforos Svingos
 */
public interface AdpStreamDBClient {

    String explain(String queryScript) throws RemoteException;

    StreamRegisterQuery.QueryInfo query(String queryID, String queryScript) throws RemoteException;

}
