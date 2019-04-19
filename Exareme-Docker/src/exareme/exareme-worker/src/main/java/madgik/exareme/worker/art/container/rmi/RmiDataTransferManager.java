///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package madgik.exareme.db.art.container.rmi;
//
//import java.rmi.RemoteException;
//import java.util.HashMap;
//import madgik.exareme.db.art.container.ContainerJob;
//import madgik.exareme.db.art.container.ContainerJobResult;
//import madgik.exareme.db.art.container.ContainerSessionID;
//import madgik.exareme.db.art.container.dataTransferMgr.DataTransferManager;
//import madgik.exareme.db.art.container.dataTransferMgr.DataTransferManagerInterface;
//import madgik.exareme.db.art.container.job.CreateOperatorJob;
//import madgik.exareme.db.art.container.job.DataTransferRegisterJob;
//import madgik.exareme.db.art.executionEngine.session.PlanSessionID;
//import madgik.exareme.common.entity.EntityName;
//
///**
// *
// * @author John Chronis
// */
//public class RmiDataTransferManager implements DataTransferManager {
//
//  private DataTransferManagerInterface dataTransferManagerInterface = null;
//  private EntityName regEntityName = null;
//  private HashMap<String,Integer > dtToDest;
//  
//
//  public RmiDataTransferManager(
//    DataTransferManagerInterface dataTransferManagerInterface,
//    EntityName regEntityName)
//    throws RemoteException {
//    this.dataTransferManagerInterface = dataTransferManagerInterface;
//    this.regEntityName = regEntityName;
//    this.dtToDest = new HashMap<>();
//  }
//
//  @Override
//  public void destroyContainerSession(ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
//    dataTransferManagerInterface.destroyContainerSession(containerSessionID, sessionID);
//  }
//
//  @Override
//  public void destroySessions(PlanSessionID sessionID) throws RemoteException {
//    dataTransferManagerInterface.destroySessions(sessionID);
//  }
//
//  @Override
//  public void destroyAllSessions() throws RemoteException {
//    dataTransferManagerInterface.destroyAllSessions();
//  }
//
//  @Override
//  public ContainerJobResult prepareJob(ContainerJob job, ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
//  // idToDtOperator.put(((CreateOperatorJob) job).operatorName, (CreateOperatorJob) job);
//          DataTransferRegisterJob dtjob = new DataTransferRegisterJob(
//            ((CreateOperatorJob) job).operatorName,
//            ((CreateOperatorJob) job).linkMapParameters,
//            ((CreateOperatorJob) job).parameters,
//            ((CreateOperatorJob) job).sessionReportID);
//    return null;          
//  }
//
//  @Override
//  public void execJob(ContainerJob job, ContainerSessionID containerSessionID, PlanSessionID sessionID) throws RemoteException {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//
//  @Override
//  public boolean hasExec(ContainerJob job) {
//    return true;
//  }
//
//  @Override
//  public void stopManager() throws RemoteException {
//  
//  }
//
//}
