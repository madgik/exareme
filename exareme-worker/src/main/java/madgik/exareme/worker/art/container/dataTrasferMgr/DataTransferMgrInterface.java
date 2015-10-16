/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.art.container.dataTrasferMgr;

import madgik.exareme.common.art.PlanSessionID;
import madgik.exareme.worker.art.concreteOperator.AbstractOperatorImpl;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public interface DataTransferMgrInterface {

    //returns the dt id to be used to identify all the register and oks TODO(J) not serial ids

    int addDataTransfer(AbstractOperatorImpl aoimpl, int numberofDTs);

    void addFailedTransfer(int DtOpId) throws Exception;

    //returns the id to be used for this register
    int addRegister(Integer DtOpId, String filename, String tableFile, String Fip, String Fport,
        String Tip, String Tport, PlanSessionID pid) throws IOException;

    void addSuccesfulTransfer(Integer DtRegId) throws Exception;

    String getFileNamefromRegID(Integer regId);

    String getTableFilefromRegID(Integer regId);

    int getDTId(String operatorName);

    void stopDataTransferServer();

    void destroySessions(PlanSessionID sessionID);

    void restart();

    CloseableHttpClient getHttpClient();

    int getPort();
}
