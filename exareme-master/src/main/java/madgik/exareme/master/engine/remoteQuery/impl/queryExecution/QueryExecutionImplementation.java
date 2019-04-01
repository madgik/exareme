/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.queryExecution;

import madgik.exareme.master.engine.executor.ExecUtils;
import madgik.exareme.master.engine.remoteQuery.ServerInfo;
import madgik.exareme.master.engine.remoteQuery.impl.RemoteQueryInternal;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * @author Christos Mallios <br>cacheHit University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class QueryExecutionImplementation implements QueryExecution {

    private static final int ID = 0;

    private final RemoteQueryInternal remoteQuery;
    //Log Declaration
    private static final Logger log = Logger.getLogger(QueryExecutionImplementation.class);
    private final Object lock = new Object();

    public QueryExecutionImplementation(RemoteQueryInternal remoteQuery) {
        this.remoteQuery = remoteQuery;
    }

    //This function executes a query in a db server and returns the
    //results as a sqlite database
    @Override
    public synchronized void executeQuery(ServerInfo server, String query, File directory,
                                          String madisMainDB, String tableName, String lnDirectory, ProcessManager processManager)
            throws RemoteException {

        StringBuilder script = new StringBuilder();
        Thread queryExecution;

        //    log.debug("Directory is : " + directory.getAbsoluteFile());
        script.append("\n-- Script BEGIN \n\n");
        // Add pragmas
        script.append("-- Optimization Pragmas \n");
        script.append("PRAGMA journal_mode = OFF; \n");
        script.append("PRAGMA synchronous = OFF; \n");
        script.append("PRAGMA automatic_index = TRUE; \n");
        script.append("PRAGMA locking_mode = EXCLUSIVE; \n");
        script.append("PRAGMA auto_vacuum = NONE; \n");
        script.append("PRAGMA ignore_check_constraints = true; \n");
        script.append("PRAGMA page_size; \n");
        script.append("\n");

        // Add query
        StringBuilder executableQuery = new StringBuilder();
        executableQuery.append("create table ").append(tableName).
                append(" as ");
        //                .append("select *");
        //    executableQuery.append(" from(").append(server.sqlDatabase);
        //    executableQuery.append(" h:").append(server.ip);
        //    executableQuery.append(" port:").append(server.port);
        //    if (server.username != null) {
        //      executableQuery.append(" u:").append(server.username);
        //    }
        //    if (server.password != null) {
        //      executableQuery.append(" p:").append(server.password);
        //    }
        //    executableQuery.append(" db:").append(server.DBName);
        //    executableQuery.append(" ").append(query).append(");\n");
        executableQuery.append(query);
        script.append(executableQuery).append(";");
        //    script.append(query);

        script.append("\n-- Script END \n\n");

        final StringBuilder queryScript = script;
        final String originalQuery = query;
        final String madisDB = madisMainDB;
        final String storageTable = tableName;
        final File dir = directory;
        final String linkDirectory = lnDirectory;
        final ProcessManager procManager = processManager;
        final ServerInfo serverInfo = server;
        final RemoteQueryInternal remotedQuery = remoteQuery;

        System.out.println("~~~~~~~~Execute script : " + executableQuery); /*SOS*/
        System.out.println("scipt is " + queryScript);
        System.out.println("the directory is " + dir.toString() + "//" + madisDB);
        System.out.println("and table is " + storageTable);

        queryExecution = new Thread() {

            @Override
            public void run() {

                String stats;

                try {

                    long startTime = System.currentTimeMillis();

                    stats = ExecUtils.runQueryOnTable(queryScript, madisDB, dir, procManager);

                    log.debug(
                            "The results of the query were saved " + "in a local SQLite database");

                    long endTime = System.currentTimeMillis();

                    //                    remotedQuery.queryCompletion(serverInfo, originalQuery,
                    //                            dir.toString(), madisDB, storageTable,
                    //                            (int) (endTime - startTime), 10);
                    remotedQuery.queryCompletion(serverInfo, originalQuery, linkDirectory, madisDB,
                            storageTable, (int) (endTime - startTime), 10);

                } catch (RemoteException ex) {
                    log.error("The results of the query were failed to be saved"
                                    + " to a local SQLite database!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1 ",
                            ex);
                } catch (IOException | SQLException ex) {
                    java.util.logging.Logger.getLogger(QueryExecutionImplementation.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }

        };
        queryExecution.start();
    }

    @Override
    public synchronized void executeQuery(File directory, String baseTable, String view,
                                          ProcessManager processManager) throws RemoteException, InterruptedException {


        StringBuilder script = new StringBuilder();
        Thread queryExecution;

        script.append("\n-- Script BEGIN \n\n");
        script.append("-- Optimization Pragmas \n");
        script.append("PRAGMA journal_mode = OFF; \n");
        script.append("PRAGMA synchronous = OFF; \n");
        script.append("PRAGMA automatic_index = TRUE; \n");
        script.append("PRAGMA locking_mode = EXCLUSIVE; \n");
        script.append("PRAGMA auto_vacuum = NONE; \n");
        script.append("PRAGMA ignore_check_constraints = true; \n");
        script.append("PRAGMA page_size; \n");
        script.append("\n");

        // Add query
        StringBuilder executableQuery = new StringBuilder();
        executableQuery.append("create view if not exists ")
                .append(view.replaceAll("\\.[0-9]\\.db", "")).
                append(" as ");
        executableQuery.append("select * from " + baseTable.replaceAll("\\.[0-9]\\.db", ""));
        script.append(executableQuery).append(";");
        //    script.append(query);

        script.append("\n-- Script END \n\n");

        final StringBuilder queryScript = script;
        final String madisDB = baseTable;
        final File dir = directory;
        final ProcessManager procManager = processManager;
        final RemoteQueryInternal remotedQuery = remoteQuery;

        System.out.println("~~~~~~~~Execute script : " + executableQuery); /*SOS*/
        System.out.println("scipt is " + queryScript);
        System.out.println("the directory is " + dir.toString() + "//" + madisDB);

        queryExecution = new Thread() {

            @Override
            public void run() {

                String stats;

                try {

                    stats = ExecUtils.runQueryOnTable(queryScript, madisDB, dir, procManager);

                    log.debug("The view has been materialized!!");

                } catch (RemoteException ex) {
                    log.error("The view has not been materialized"
                                    + " to a local SQLite database!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1 ",
                            ex);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(QueryExecutionImplementation.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }

        };
        queryExecution.start();
        queryExecution.join();
    }
}
