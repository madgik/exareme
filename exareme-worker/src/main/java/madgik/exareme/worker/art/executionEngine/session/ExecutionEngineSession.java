/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.session;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.executionEngine.ExecutionEngine;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ExecutionEngineSession implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ExecutionEngineSession.class);
    private ExecutionEngine engine = null;
    private List<ExecutionEngineSessionPlan> planList = null;

    public ExecutionEngineSession(ExecutionEngine engine) {
        this.engine = engine;
        this.planList = Collections.synchronizedList(new LinkedList<ExecutionEngineSessionPlan>());
    }

    public ExecutionEngineSessionPlan startSession() throws RemoteException {
        PlanSessionID sessionID = engine.createNewSession();
        ExecutionEngineSessionPlan sessionPlan = new ExecutionEngineSessionPlan(sessionID, engine);
        planList.add(sessionPlan);
        return sessionPlan;
    }

    public ExecutionEngineSessionPlan startSessionElasticTree() throws RemoteException {
        PlanSessionID sessionID = engine.createNewSessionElasticTree();
        ExecutionEngineSessionPlan sessionPlan = new ExecutionEngineSessionPlan(sessionID, engine);
        planList.add(sessionPlan);
        return sessionPlan;
    }


    public List<ExecutionEngineSessionPlan> listPlans() throws RemoteException {
        return Collections.unmodifiableList(planList);
    }

    public void close() throws RemoteException {
        for (ExecutionEngineSessionPlan sessionPlan : planList) {
            if (sessionPlan.isClosed() == false) {
                sessionPlan.close();
            }
        }
    }
}
