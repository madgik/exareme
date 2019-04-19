/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
//package madgik.exareme.db.arm.compute.cloud.openNebula.util;
//
//import demo.Eolus;
//import demo.StringArray;
//import java.util.Arrays;
//
///**
// *
// * @author herald
// */
//public class CreateWorkerNode extends Thread {
//
//    public Eolus eolus = null;
//    public Exception exception = null;
//    public String name = null;
//
//    public CreateWorkerNode(Eolus eolus, String name) {
//        this.eolus = eolus;
//        this.name = name;
//    }
//
//    @Override
//    public void run() {
//        try {
//            String template = "gcube-2.2.4";
//            String[] nets = {"public"};
//            StringArray vnets = new StringArray();
//            vnets.getItem().addAll(Arrays.asList(nets));
//            eolus.createVM(template, name, 2, 512, vnets);
//
////            while(true) {
////
////                try {
////                    String status = eolus.getVMStatus(name);
////                    if(status.equalsIgnoreCase("RUNNING")) {
////                        break;
////                    }
////
////                    if(status.equals("STAGING") == false) {
////                        log.debug(name + ":" + status);
////                        break;
////                    }
////                } catch (Exception e) {
////                    System.err.println(e.getMessage());
////                }
////
////                /* Sleep 5 seconds */
////                Thread.sleep(5000);
////            }
//
//            log.debug(name + " is running...");
//
//        } catch (Exception e) {
//            this.exception = e;
//        }
//    }
//}
