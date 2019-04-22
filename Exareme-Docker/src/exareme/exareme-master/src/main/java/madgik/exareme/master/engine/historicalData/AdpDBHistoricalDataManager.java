/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.historicalData;

import org.apache.log4j.Logger;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.HashMap;

/**
 * @author heraldkllapi
 */
public class AdpDBHistoricalDataManager {

    private static Logger log = Logger.getLogger(AdpDBHistoricalDataManager.class);
    private String database = null;
    private File dataFolder = null;
    private File historyDataFile = null;
    private HashMap<String, AdpDBHistoricalQueryData> queryDataMap = null;

    public AdpDBHistoricalDataManager(String database) {
        this.database = database;
        try {
            this.dataFolder = new File(database + "/history/");
            this.historyDataFile = new File(dataFolder, "history.data");
            try {
                ObjectInputStream inStream =
                        new ObjectInputStream(new FileInputStream(historyDataFile));
                queryDataMap = (HashMap<String, AdpDBHistoricalQueryData>) inStream.readObject();
                inStream.close();
                log.debug("Loaded " + queryDataMap.size() + " historical queries ...");
            } catch (IOException e) {
                log.debug("Cannot load history file: " + e.toString());
                this.queryDataMap = new HashMap<String, AdpDBHistoricalQueryData>();
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot load historical data", e);
        }
    }

    public void addQueryData(String queryId, AdpDBHistoricalQueryData queryData)
            throws RemoteException {
        AdpDBHistoricalQueryData data = queryDataMap.get(queryId.toString());
        if (data != null) {
            data.combineWith(queryData);
        } else {
            queryDataMap.put(queryId.toString(), queryData);
        }
    }

    public AdpDBHistoricalQueryData getQueryData(String queryId) throws RemoteException {
        return queryDataMap.get(queryId);
    }

    public void updateHistory() throws RemoteException {
        try {
            dataFolder.mkdirs();
            ObjectOutputStream outStream =
                    new ObjectOutputStream(new FileOutputStream(historyDataFile));
            outStream.writeObject(queryDataMap);
            outStream.close();
        } catch (Exception e) {
            throw new ServerException("Cannot update historical data", e);
        }
    }
}
