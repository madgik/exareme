/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.art.container.dataTransfer.rest;

import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 * University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class RequestFileThread extends Thread {

    private static final org.apache.log4j.Logger log =
            org.apache.log4j.Logger.getLogger(RequestFileThread.class);
    String workerIP;
    Integer fid;
    String fileName;
    String workerPort;
    String pid;
    DiskManagerInterface diskManagerInterface;

    RequestFileThread(String workerIP, Integer fid, String fileName, String workerPort, String pid,
                      DiskManagerInterface diskManagerInterface) {
        this.workerIP = workerIP;
        this.fid = fid;
        this.diskManagerInterface = diskManagerInterface;
        this.pid = pid;
        this.workerPort = workerPort;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        try {
            DataTransferClient
                    .requestByID(workerIP, fid, fileName, workerPort, pid, diskManagerInterface);
        } catch (Exception ex) {
            Logger.getLogger(RequestFileThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
