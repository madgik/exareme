/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator.manager;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.file.InputStreamConsumerThread;
import org.apache.log4j.Logger;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ProcessManager {

    private final static Logger log = Logger.getLogger(ProcessManager.class);
    private final SessionManager sessionManager;
    private final ArrayList<Process> processes = new ArrayList<Process>();

    public ProcessManager() {
        this.sessionManager = null;
    }

    public ProcessManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public Process createProcess(File directory, String... args) throws RemoteException {
        try {
            ProcessBuilder pb = new ProcessBuilder(args).directory(directory);
            Process proc = pb.start();
            processes.add(proc);
            return proc;
        } catch (Exception e) {
            throw new ServerException("Cannot create process", e);
        }
    }

    public Process createProcess(File directory, File out, File err, String... args)
        throws RemoteException {
        try {
            ProcessBuilder pb = new ProcessBuilder(args);
            log.debug("Creating process ...");
            pb.directory(directory);
            log.debug("Set current dir to: " + directory);
            //      pb.redirectOutput(out);
            log.debug("Set output to: " + out);
            //      pb.redirectError(err);
            log.debug("Set rrror to: " + out);
            Process proc = pb.start();
            processes.add(proc);
            return proc;
        } catch (Exception e) {
            throw new ServerException("Cannot create process", e);
        }
    }

    public Pair<String, String> createAndRunProcess(File directory, String... args)
        throws RemoteException {
        Process p = createProcess(directory, args);
        // Read stdout
        InputStreamConsumerThread stdout = new InputStreamConsumerThread(p.getInputStream(), false);
        stdout.start();
        // Read stdin
        InputStreamConsumerThread stderr = new InputStreamConsumerThread(p.getErrorStream(), false);
        stderr.start();
        try {
            p.waitFor();
            stdout.join();
            stderr.join();
        } catch (Exception e) {
            throw new RemoteException("Cannot wait for process to finish", e);
        }
        return new Pair<String, String>(stdout.getOutput(), stderr.getOutput());
    }

    public void closeAllProcesses() throws RemoteException {
        for (Process p : processes) {
            p.destroy();
        }
        log.debug("Stopped processes : " + processes.size());
    }
}
