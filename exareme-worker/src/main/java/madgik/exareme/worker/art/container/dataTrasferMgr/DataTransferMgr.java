/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.art.container.dataTrasferMgr;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.AbstractOperatorImpl;
import madgik.exareme.worker.art.container.dataTransfer.ContainerDataTransferGatewayFactory;
import madgik.exareme.worker.art.container.dataTransfer.DataTransferGateway;
import madgik.exareme.worker.art.container.dataTransfer.rest.DataTransferClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.util.*;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */

public class DataTransferMgr implements DataTransferMgrInterface {
    private static final Logger log = Logger.getLogger(DataTransferMgr.class);
    DataTransferGateway dataTransfer;
    private int nextDtRegId;
    private int nextDtOpId;
    private Map<String, Integer> operatorNameToTransferID;
    private Map<Integer, AbstractOperatorImpl> transferIdToAoimpl = new HashMap<>();
    private Map<Integer, Set<Integer>> transferIdToRegIds;
    private Map<String, Integer> IPtoDTId;
    private Map<Integer, String> RegIDToFilename;
    private Map<Integer, String> RegIDToTableFile;
    private Map<Integer, Integer> RedIdToTransferId;
    private Map<Integer, Integer> transferIDToNumOfDts;
    private PoolingHttpClientConnectionManager cm;
    private CloseableHttpClient httpClient;
    private int port;

    {
        org.apache.log4j.Logger.getLogger("org.apache.http").setLevel(org.apache.log4j.Level.INFO);
    }

    public DataTransferMgr(int port) throws RemoteException {
        this.transferIdToRegIds = new HashMap<>();
        this.port = port;
        log.debug("\t DataTranfer starting on port: " + port + "...");

        dataTransfer =
            ContainerDataTransferGatewayFactory.createDataTransferServer("0.0.0.0", port);
        dataTransfer.start();
        this.nextDtOpId = 0;
        this.nextDtRegId = 0;
        this.operatorNameToTransferID = new HashMap<>();
        this.transferIdToAoimpl = new HashMap<>();
        this.transferIdToRegIds = new HashMap<>();
        this.IPtoDTId = new HashMap<>();
        this.RegIDToFilename = new HashMap<>();
        this.RegIDToTableFile = new HashMap<>();
        this.RedIdToTransferId = new HashMap<>();
        this.transferIDToNumOfDts = new HashMap<>();

        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
        log.debug("Http client created.");

    }

    public void startServer() throws RemoteException {

    }

    @Override public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    @Override public int getPort() {
        return port;
    }

    @Override public int getDTId(String operatorName) {
        if (operatorNameToTransferID.get(operatorName) != null) {
            return operatorNameToTransferID.get(operatorName);
        }
        return -1;
    }

    //returns the dt id to be used to identify all the register and oks TODO(J) not serial ids
    @Override public int addDataTransfer(AbstractOperatorImpl aoimpl, int numberofDT) {
        transferIdToAoimpl.put(nextDtOpId, aoimpl);
        transferIdToRegIds.put(nextDtOpId, new HashSet<Integer>());
        transferIDToNumOfDts.put(nextDtOpId, numberofDT);
        return nextDtOpId++;
    }

    @Override public void stopDataTransferServer() {
        dataTransfer.stop();
    }

    //returns the id to be used for this register
    @Override public int addRegister(Integer DtOpId, String filename, String tableFile, String Fip,
        String Fport, String Tip, String Tport, PlanSessionID pid) throws IOException {
        RedIdToTransferId.put(nextDtRegId, DtOpId);
        transferIdToRegIds.get(DtOpId).add(nextDtRegId);
        RegIDToFilename.put(nextDtRegId, filename);//cleanup when done
        RegIDToTableFile.put(nextDtRegId, tableFile);
       log.trace("Add register " + DtOpId + " " + nextDtRegId + " " + filename);

        DataTransferClient.registerFileByID(Tip, nextDtRegId, filename, Fip, Fport, Tport, pid);


        return nextDtRegId++;
    }

    @Override public void addFailedTransfer(int DtOpId) throws Exception {
        AbstractOperatorImpl aoimp = transferIdToAoimpl.get(DtOpId);
        aoimp.getSessionManager().getOperatorStatistics()
            .setException(new Exception());//JV exception set
        aoimp.error(new Exception());
    }

    @Override public void addSuccesfulTransfer(Integer DtRegId) throws Exception {
        log.trace("addSuccesfulTransfer: " + DtRegId);
        if (RedIdToTransferId.containsKey(DtRegId)) {
            int DtOpId = RedIdToTransferId.get(DtRegId);
            RedIdToTransferId.remove(DtRegId);
            transferIdToRegIds.get(DtOpId).remove(DtRegId);
            transferIDToNumOfDts.put(DtOpId, transferIDToNumOfDts.get(DtOpId) - 1);

            if (transferIDToNumOfDts.get(DtOpId) == 0) {

                AbstractOperatorImpl aoimp = transferIdToAoimpl.get(DtOpId);

                aoimp.exit(0);
                aoimp.finalizeOperator();

                long end = System.currentTimeMillis();
                aoimp.getSessionManager().getOperatorStatistics().setEndTime_ms(end);
                aoimp.getSessionManager().getOperatorStatistics().setExitCode(aoimp.getExitCode());
                aoimp.getSessionManager().getOperatorStatistics()
                    .setExitMessage(aoimp.getExitMessage());

                aoimp.getSessionManager().getOperatorStatistics().
                    setTotalTime_ms(end - aoimp.start,
                        ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() / 1000000);

                log.trace("addSuccesfulTransfer done all");
                aoimp.getSessionManager().getSessionReportID().reportManagerProxy
                    .operatorSuccess(aoimp.getSessionManager().getOpID(), aoimp.getExitCode(),
                        aoimp.getExitMessage(), new Date(),
                        aoimp.getSessionManager().getContainerID(), true);
                aoimp.freeresources(aoimp.getSessionManager().getOpID());
                transferIdToAoimpl.remove(DtOpId);

                transferIDToNumOfDts.remove(DtOpId);

            }
            RegIDToFilename.remove(DtRegId);
            RegIDToTableFile.remove(DtRegId);
            RedIdToTransferId.remove(DtRegId);
        }
    }

    public String getFileNamefromRegID(Integer regId) {
        return RegIDToFilename.get(regId);
    }

    public String getTableFilefromRegID(Integer regId) {
        return RegIDToTableFile.get(regId);
    }

    @Override public void destroySessions(PlanSessionID sessionID) {


    }

    @Override public void restart() {

    }

}
