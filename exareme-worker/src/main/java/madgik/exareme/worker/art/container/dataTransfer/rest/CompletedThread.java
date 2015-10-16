/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.art.container.dataTransfer.rest;

import madgik.exareme.worker.art.container.diskMgr.DiskManagerInterface;

/**
 * @author John Chronis <br>
 * @author Vaggelis Nikolopoulos <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class CompletedThread extends Thread {

    private static final org.apache.log4j.Logger log =
        org.apache.log4j.Logger.getLogger(CompletedThread.class);
    String receiverIP;
    Integer fid;
    String fileName;
    String port;
    String pid;
    DiskManagerInterface diskManagerInterface;

    CompletedThread(String receiverIP, Integer fid, String filename, String port, String pid) {
        this.receiverIP = receiverIP;
        this.fid = fid;
        this.port = port;
        this.fileName = filename;
        this.pid = pid;
    }

    @Override public void run() {
        try {
            DataTransferClient.datatranferCompletedByID(receiverIP, fid, fileName, port, pid);
        } catch (Exception ex) {
            log.error(ex);
        }
    }

}
