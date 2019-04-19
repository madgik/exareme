/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.art.container.dataTrasferMgr;

import java.util.HashMap;
import java.util.Map;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class DataTransferMgrLocator {
    private static DataTransferMgrInterface dtMgr = null;

    private static Map<Integer, DataTransferMgrInterface> dtMgrMap = new HashMap<>();

    public static void setDataTransferMgr(DataTransferMgrInterface engine, int port) {
        DataTransferMgrLocator.dtMgr = engine;
        dtMgrMap.put(port, engine);
    }

    public static DataTransferMgrInterface getDataTransferManager(int port) {
        return dtMgrMap.get(port);
    }

    public static DataTransferMgrInterface getDataTransferManager() {
        return dtMgr;
    }
}
