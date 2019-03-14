/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.operatorMgr;

import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.JobExecutor;
import madgik.exareme.worker.art.container.SessionBased;
import madgik.exareme.worker.art.container.Stoppable;

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
public interface ConcreteOperatorManager extends SessionBased, JobExecutor, Stoppable {

    ConcreteOperatorStatistics getOperatorStatistics(ConcreteOperatorID opID,
                                                     ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException;

    ConcreteOperatorManagerStatus getStatus() throws RemoteException;


}
