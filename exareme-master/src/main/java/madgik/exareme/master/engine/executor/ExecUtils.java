/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.file.InputStreamConsumerThread;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author heraldkllapi
 */
public class ExecUtils {
    private static final String python =
        AdpProperties.getSystemProperties().getString("EXAREME_PYTHON");
    private static final String engine =
        AdpProperties.getSystemProperties().getString("EXAREME_MADIS");
    private static Logger log = Logger.getLogger(ExecUtils.class);

    public static void copyPartition(String tableLocation, String tempTableLocation, File directory,
        ProcessManager procManager) throws RemoteException {
        log.debug("Copying partition to temp file ...");
        try {
            Pair<String, String> stdOutErr =
                procManager.createAndRunProcess(directory, "cp", tableLocation, tempTableLocation);

            if (stdOutErr.b.trim().isEmpty() == false) {
                throw new ServerException("Cannot execute cp: " + stdOutErr.b);
            }

            log.debug(stdOutErr.a);
        } catch (Exception e) {
            throw new ServerException("Cannot copy table: " + tempTableLocation, e);
        }
    }

    public static String runQueryOnTable(StringBuilder query, String madisMainDB, File directory,
        ProcessManager procManager) throws RemoteException {
        log.debug("Process Directory: " + directory.getAbsolutePath());
        try {
            Process p = procManager.createProcess(directory, python, engine, madisMainDB);
            p.getOutputStream().write(query.toString().getBytes());
            p.getOutputStream().flush();
            p.getOutputStream().close();

            InputStreamConsumerThread stdout =
                new InputStreamConsumerThread(p.getInputStream(), false);
            stdout.start();

            InputStreamConsumerThread stderr =
                new InputStreamConsumerThread(p.getErrorStream(), false);
            stderr.start();

            int exitCode = p.waitFor();
            stdout.join();
            stderr.join();

            if (exitCode != 0 || stderr.getOutput().trim().isEmpty() == false) {
                log.error(stderr.getOutput());
                throw new ServerException(
                    "Cannot execute db (code: " + exitCode + "): " + stderr.getOutput());
            }
            String output = stdout.getOutput();
            log.debug(output);
            return output;
        } catch (Exception e) {
            throw new ServerException("Cannot run query", e);
        }
    }

    public static void enforceNoStatsOnTable(String tableName, String madisMainDB, File directory,
        ProcessManager procManager) throws RemoteException {
        ExecUtils.runQueryOnTable(new StringBuilder("create table dummy(id); \n" +
                "analyze dummy; \n" +
                "drop table dummy; \n" +
                "delete from sqlite_stat1 where tbl = '" + tableName + "'; \n" +
                "insert into sqlite_stat1 values('" + tableName + "', null, 1000000); \n"),
            madisMainDB, directory, procManager);
    }
}
