/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine;

import madgik.exareme.worker.art.executionEngine.session.ExecutionEngineSession;
import madgik.exareme.worker.art.registry.Registerable;
import madgik.exareme.worker.art.remote.ObjectProxy;

import java.rmi.RemoteException;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface ExecutionEngineProxy extends ObjectProxy<ExecutionEngine>, Registerable {

    /**
     * Create a new session.
     *
     * @return a new execution engine session
     * @throws RemoteException if the session cannot be created.
     */
    ExecutionEngineSession createSession() throws RemoteException;
}
