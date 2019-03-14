/*
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.queryExecution;

import madgik.exareme.master.engine.remoteQuery.ServerInfo;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;

import java.io.File;
import java.rmi.RemoteException;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public interface QueryExecution {

    public void executeQuery(ServerInfo server, String query, File directory, String madisMainDB,
                             String tableName, String lnDirectory, ProcessManager processManager) throws RemoteException;

    public void executeQuery(File directory, String baseTable, String view,
                             ProcessManager processManager) throws RemoteException, InterruptedException;

}
