///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.master.app.engine.scheduler;
//
//
//import madgik.exareme.master.app.engine.scheduler.kFifo.AdpDBSchedulerKFiFo;
//import madgik.exareme.master.app.engine.scheduler.sync.AdpDBSchedulerSelfManageableSynchronized;
//
//import java.rmi.RemoteException;
//
///**
// *
// * @author herald
// */
//public class AdpDBSchedulerFactory {
//
//  public static AdpDBSchedulerSelfManageable createAdpDBKFiFoScheduler(
//          int k, String server) throws RemoteException {
//    return new AdpDBSchedulerSelfManageableSynchronized(
//            new AdpDBSchedulerKFiFo(k, server));
//  }
//
//  private AdpDBSchedulerFactory() {
//  }
//}
