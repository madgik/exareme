/**
 * Copyright MaDgIK Group 2010 - 2013.
 */
package madgik.exareme.utils.embedded.process;

import madgik.exareme.utils.embedded.utils.SqlParseUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author heraldkllapi
 * @author Christoforos Svingos
 */
public class MadisProcess {
    private static final Logger log = Logger.getLogger(MadisProcess.class);
    private static final String python = System.getProperty("EXAREME_PYTHON");
    private static final String madis = System.getProperty("EXAREME_MADIS");
    private String dbFile = null;
    private Process proc = null;
    private BufferedReader input = null;
    private InputStreamConsumerThread stderr = null;
    private boolean activeQuery = false;
    private String madisPath = null;

    public MadisProcess(String madisPath) {
        //        this.madisPath = getMadisPath(madisPath);
        this.madisPath = madis;
    }

    public MadisProcess(String dbFile, String madisPath) {
        this.dbFile = dbFile;
        this.madisPath = getMadisPath(madisPath);
    }

    private String getMadisPath(String madisPath) {
        String madisDefaultPath = "/opt/madis/src/mterm.py";

        if (madisPath == null)
            return madisDefaultPath;

        File madis = new File(madisPath);
        if (!madis.exists() || madis.isDirectory()) {
            log.warn(
                "Madis path does not exist... Default Path: " + madisDefaultPath + " is setting");
            return madisDefaultPath;
        }

        return madisPath;
    }

    public void start() throws IOException {
        log.debug("Starting process ... (" + dbFile + " "+ python + " " +madisPath);
        ProcessBuilder pb;
        if (dbFile == null) {
            pb = new ProcessBuilder("stdbuf", "-oL", python, madisPath);
//            pb = new ProcessBuilder(python, madisPath);
        } else {
            pb = new ProcessBuilder("stdbuf", "-oL", python, madisPath, dbFile);
//            pb = new ProcessBuilder(python, madisPath, dbFile);
        }
        pb.directory(new File("."));
        proc = pb.start();

        log.debug("Starting stderr consumer ...");
        stderr = new InputStreamConsumerThread(proc.getErrorStream(), true);
        stderr.start();

        input = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        log.info("Process started!");
    }

    public void stop() throws IOException, InterruptedException {
        log.info("Waiting for process to stop ...");
        proc.getOutputStream().close();
        int exitCode = proc.waitFor();
        stderr.join();
        input.close();
        log.info("Process stopped!");
        if (exitCode != 0) {
            throw new IOException("Cannot stop process: " + exitCode, stderr.getException());
        }
    }

    public void cancel() throws IOException, InterruptedException {
        log.info("Waiting for process to cancel ...");
        proc.getOutputStream().close();
        proc.destroy();
        stderr.join();
        input.close();
        log.info("Process canceled!");
    }

    public QueryResultStream execQuery(String query) throws IOException {
        query = query.trim();
        query += (query.substring(query.length() - 1).equals(";")) ? "" : ";";
        query = query.replaceAll(";\\s", ";\n\n");

        if (activeQuery) {
            throw new IOException("Only one query can be active at any given time!");
        }
        log.debug("Exec query and get results ...");
        proc.getOutputStream().write(query.getBytes());
        proc.getOutputStream().write("\r\n".getBytes());
        proc.getOutputStream().flush();
        activeQuery = true;

        return new QueryResultStream(input, stderr, this, SqlParseUtils.countOfqueries(query));
    }

    void queryFinished() {
        activeQuery = false;
    }
}
