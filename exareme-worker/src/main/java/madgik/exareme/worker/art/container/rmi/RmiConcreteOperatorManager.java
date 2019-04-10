/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.rmi;

import madgik.exareme.common.art.ConcreteOperatorStatistics;
import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterface;
import madgik.exareme.worker.art.container.job.*;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManager;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerInterface;
import madgik.exareme.worker.art.container.operatorMgr.ConcreteOperatorManagerStatus;

import java.rmi.RemoteException;

/**
 * University of Athens / Department of Informatics and Telecommunications.
 *
 * @author Herald Kllapi <br>
 * @author John Chronis<br>
 * @author Vaggelis Nikolopoulos<br>
 * @since 1.0
 */
public class RmiConcreteOperatorManager implements ConcreteOperatorManager {

    private ConcreteOperatorManagerInterface concreteOperatorManagerInterface = null;
    private EntityName regEntityName = null;
    private DataTransferMgrInterface dataTransferManagerDTP = null;

    public RmiConcreteOperatorManager(
            ConcreteOperatorManagerInterface concreteOperatorManagerInterface, EntityName regEntityName,
            DataTransferMgrInterface dataTransferManagerDTP) throws RemoteException {
        this.concreteOperatorManagerInterface = concreteOperatorManagerInterface;
        this.regEntityName = regEntityName;
        this.dataTransferManagerDTP = dataTransferManagerDTP;

        //JC na parei to container
    }

    @Override
    public void stopManager() throws RemoteException {
    }

    @Override
    public ContainerJobResult prepareJob(ContainerJob job, ContainerSessionID containerSessionID,
                                         PlanSessionID sessionID) throws RemoteException {
        switch (job.getType()) {
            //      case dataTransferRegister:{
            //        return create((CreateOperatorJob) job, containerSessionID, sessionID);
            //      }
            case createOperator: {
                CreateOperatorJobResult cpjr =
                        create((CreateOperatorJob) job, containerSessionID, sessionID);
                if (((CreateOperatorJob) job).type == OperatorType.dataTransfer) {
                    //           //concreteOperatorManagerInterface.
                    //             dataTransferManagerDTP.AddTodataTransferSuccess.put(
                    //               ((CreateOperatorJob) job).operatorName ,
                    //               concreteOperatorManagerInterface.getAbstractOperator(((CreateOperatorJob) job).operatorName))     ;
                    //
                    //          dataTransferManagerDTP.addDataTransfer(//TODO @vagos
                    //            ((CreateOperatorJob) job).operatorName,
                    //            concreteOperatorManagerInterface.getAbstractOperator(
                    //              ((CreateOperatorJob) job).operatorName));
                }
                return cpjr;
            }
            case startOperator: {
        /*concreteOperatorManagerInterface.start(
         ((StartOperatorJob) job).opID,
         containerSessionID,
         sessionID);*/
                return new StartOperatorJobResult();
            }
            case stopOperator: {
                concreteOperatorManagerInterface
                        .stop(((StopOperatorJob) job).opID, containerSessionID, sessionID);
                return new StopOperatorJobResult();
            }
            case destroyOperator: {
                concreteOperatorManagerInterface
                        .destroyInstance(((DestroyOperatorJob) job).opID, containerSessionID,
                                sessionID);
                return new DestroyOperatorJobResult();
            }
        }
        throw new RemoteException("Job type not supported: " + job.getType());
    }

    @Override
    public boolean hasExec(ContainerJob job) {
        switch (job.getType()) {
            //case dataTransferRegister:
            case destroyOperator:
            case stopOperator:
            case createOperator: {
                return false;
            }
            case startOperator: {
                return true;
            }
        }
        return false;
    }

    @Override
    public void execJob(ContainerJob job, ContainerSessionID containerSessionID,
                        PlanSessionID sessionID) throws RemoteException {
        switch (job.getType()) {
            case createOperator: {
                return;
            }
            case startOperator: {
                concreteOperatorManagerInterface
                        .start(((StartOperatorJob) job).opID, ((StartOperatorJob) job).contSessionID,
                                sessionID);
                return;
            }
            case stopOperator: {
                return;
            }
            case destroyOperator: {
                return;
            }
        }
        throw new RemoteException("Job type not supported: " + job.getType());
    }

    private CreateOperatorJobResult create(CreateOperatorJob job,
                                           ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {

        return new CreateOperatorJobResult(concreteOperatorManagerInterface
                .instantiate(job.operatorName, job.category, job.type, job.operator, job.parameters,
                        job.linkMapParameters, job.queryString, job.sessionReportID, containerSessionID,
                        sessionID));
    }

    @Override
    public ConcreteOperatorManagerStatus getStatus() throws RemoteException {
        return concreteOperatorManagerInterface.getStatus();
    }

    @Override
    public void destroyContainerSession(ContainerSessionID containerSessionID,
                                        PlanSessionID sessionID) throws RemoteException {
        concreteOperatorManagerInterface.destroyContainerSession(containerSessionID, sessionID);
    }

    @Override
    public void destroySessions(PlanSessionID sessionID) throws RemoteException {
        concreteOperatorManagerInterface.destroySessions(sessionID);
    }

    @Override
    public void destroyAllSessions() throws RemoteException {
        concreteOperatorManagerInterface.destroyAllSessions();
    }

    @Override
    public ConcreteOperatorStatistics getOperatorStatistics(ConcreteOperatorID opID,
                                                            ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
        return concreteOperatorManagerInterface
                .getOperatorStatistics(opID, containerSessionID, sessionID);
    }

}
