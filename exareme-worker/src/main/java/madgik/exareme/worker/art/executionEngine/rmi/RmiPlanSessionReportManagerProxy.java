/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.rmi;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManager;
import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManagerProxy;
import madgik.exareme.worker.art.executionEngine.session.PlanSessionReportID;
import madgik.exareme.worker.art.remote.RmiObjectProxy;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * @author Herald Kllapi <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RmiPlanSessionReportManagerProxy extends RmiObjectProxy<PlanSessionReportManager>
    implements PlanSessionReportManagerProxy {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(RmiPlanSessionReportManagerProxy.class);
    public PlanSessionReportID internalSessionID = null;

    public RmiPlanSessionReportManagerProxy(String regEntryName, EntityName regEntityName) {
        super(regEntryName, regEntityName);
    }

    @Override public void planStart(Date time, ContainerID containerID) throws RemoteException {
        super.getRemoteObject().planStart(time, containerID, internalSessionID);
    }

    @Override public void planInstantiationException(RemoteException exception, Date time,
        ContainerID containerID) throws RemoteException {
        super.getRemoteObject()
            .planInstantiationException(exception, time, containerID, internalSessionID);
    }

    @Override public void operatorSuccess(ConcreteOperatorID operatorID, int exidCode,
        Serializable exitMessage, Date time, ContainerID containerID, boolean terminateGroup)
        throws RemoteException {
        //    System.out.println("OperatorSuccess: " + operatorID.operatorName + " " + " " + exidCode);
        //    System.out.println("RPSMP before get");
        log.debug("Reporting operatorSuccess: " + operatorID.operatorName);
        boolean success = false;
        int max_retries = 1000;
        // System.out.println("RPSMP after get");
        while (!success && max_retries > 0) {
            try {
                max_retries--;
                PlanSessionReportManager rmo = super.getRemoteObject();
                rmo.operatorSuccess(operatorID, exidCode, exitMessage, time, containerID,
                    internalSessionID, terminateGroup);
                success = true;
                log.debug("Successfully reported, operatorSuccess: " + operatorID.operatorName);
                //      System.out.println("RPSMP after success");
                //      System.out.println("DoneOperatorSuccess: " + operatorID.operatorName + " " + " " + exidCode);
            } catch (Exception e) {
                System.out.println("RPSMP ERROR: " + e);
                //this.operatorSuccess(operatorID, exidCode, exitMessage, time, containerID);
            }
        }

    }

    @Override
    public void operatorError(ConcreteOperatorID operatorID, RemoteException exception, Date time,
        ContainerID containerID) throws RemoteException {
        super.getRemoteObject()
            .operatorError(operatorID, exception, time, containerID, internalSessionID);
    }
}
