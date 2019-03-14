/**
 * Copyright MaDgIK Group 2014.
 */
package madgik.exareme.worker.art.container.dataTrasferMgr.sync;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.AbstractOperatorImpl;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgr;
import madgik.exareme.worker.art.container.dataTrasferMgr.DataTransferMgrInterface;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class SynchronizedDataTransferMgr implements DataTransferMgrInterface {

    private final DataTransferMgrInterface dataTransferManagerDTP;

    public SynchronizedDataTransferMgr(DataTransferMgr dataTransferManagerDTP) {
        this.dataTransferManagerDTP = dataTransferManagerDTP;
    }

    @Override
    public void addFailedTransfer(int DtOpId) throws Exception {
        synchronized (dataTransferManagerDTP) {
            dataTransferManagerDTP.addFailedTransfer(DtOpId);
        }
    }

    @Override
    public void addSuccesfulTransfer(Integer DtRegId) throws Exception {
        synchronized (dataTransferManagerDTP) {
            dataTransferManagerDTP.addSuccesfulTransfer(DtRegId);
        }
    }

    @Override
    public int getDTId(String operatorName) {
        synchronized (dataTransferManagerDTP) {
            return dataTransferManagerDTP.getDTId(operatorName);
        }
    }

    @Override
    public void stopDataTransferServer() {
        synchronized (dataTransferManagerDTP) {
            dataTransferManagerDTP.stopDataTransferServer();
        }
    }

    @Override
    public int addDataTransfer(AbstractOperatorImpl aoimpl, int ndts) {
        synchronized (dataTransferManagerDTP) {
            return dataTransferManagerDTP.addDataTransfer(aoimpl, ndts);
        }
    }

    @Override
    public int addRegister(Integer DtOpId, String filename, String tableFile, String Fip,
                           String Fport, String Tip, String Tport, PlanSessionID pid) throws IOException {
        synchronized (dataTransferManagerDTP) {
            return dataTransferManagerDTP
                    .addRegister(DtOpId, filename, tableFile, Fip, Fport, Tip, Tport, pid);
        }
    }

    @Override
    public String getFileNamefromRegID(Integer regId) {
        synchronized (dataTransferManagerDTP) {
            return dataTransferManagerDTP.getFileNamefromRegID(regId);
        }
    }

    @Override
    public String getTableFilefromRegID(Integer regId) {
        synchronized (dataTransferManagerDTP) {
            return dataTransferManagerDTP.getTableFilefromRegID(regId);
        }
    }

    @Override
    public void destroySessions(PlanSessionID sessionID) {
        synchronized (dataTransferManagerDTP) {
            dataTransferManagerDTP.destroySessions(sessionID);
        }
    }

    @Override
    public void restart() {
        synchronized (dataTransferManagerDTP) {
            dataTransferManagerDTP.restart();
        }
    }

    @Override
    public int getPort() {
        synchronized (dataTransferManagerDTP) {
            return dataTransferManagerDTP.getPort();
        }
    }

    @Override
    public CloseableHttpClient getHttpClient() {
        synchronized (dataTransferManagerDTP) {
            return dataTransferManagerDTP.getHttpClient();
        }
    }

}
