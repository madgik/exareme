/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.engine.remoteQuery.impl.queryExecution;

import madgik.exareme.master.engine.remoteQuery.ServerInfo;
import madgik.exareme.master.engine.remoteQuery.impl.RemoteQueryInternal;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.logging.Level;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class QuerySimulationExecution implements QueryExecution {

    private static int ID = 0;

    private final RemoteQueryInternal remoteQuery;
    //Log Declaration
    private static final Logger log = Logger.getLogger(QueryExecutionImplementation.class);
    private final Object lock = new Object();

    public QuerySimulationExecution(RemoteQueryInternal remoteQuery) {
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

        StringBuilder executableQuery = new StringBuilder();
        executableQuery.append("create table ").append(tableName).
                append(" as select *");
        executableQuery.append(" from(").append(server.sqlDatabase);
        executableQuery.append(" h:").append(server.ip);
        executableQuery.append(" port:").append(server.port);
        if (server.username != null) {
            executableQuery.append(" u:").append(server.username);
        }
        if (server.password != null) {
            executableQuery.append(" p:").append(server.password);
        }
        executableQuery.append(" db:").append(server.DBName);
        executableQuery.append(" ").append(query).append(");\n");
        script.append(executableQuery);

        script.append("\n-- Script END \n\n");

        final StringBuilder queryScript = script;
        final String originalQuery = query;
        final String madisDB = madisMainDB;
        final String storageTable = tableName;
        final File dir = directory;
        final ProcessManager procManager = processManager;
        final ServerInfo serverInfo = server;
        final RemoteQueryInternal remotedQuery = remoteQuery;

        queryExecution = new Thread() {

            @Override
            public void run() {

                String stats;

                final long startTime = System.currentTimeMillis();

                String database = "/home/christos/simulations/cache.db";
                Connection connection;
                int duration = -1;
                int size = -1;

                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException ex) {
                    java.util.logging.Logger.getLogger(QueryExecutionImplementation.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                try {
                    connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                    Statement statement = connection.createStatement();

                    ////          String[] splitQuery = originalQuery.split("_");
                    ////          ResultSet rs = statement.executeQuery("SELECT `processing_time`, "
                    ////                  + "`size` "
                    ////                  + " FROM query_data WHERE `query` = '" + splitQuery[0] + "'");
                    ResultSet rs = statement.executeQuery("SELECT `processing_time`, " + "`size` "
                            + " FROM query_data WHERE `query` = '" + originalQuery + "'");

                    if (rs.next()) {
                        duration = rs.getInt("processing_time");
                        size = rs.getInt("size");
                    }

                    Thread.sleep(duration * 1000);

                    long endTime = System.currentTimeMillis();
                    System.out.println(
                            "wait gia query " + originalQuery + " gia " + (endTime - startTime));

                    ID++;
                    remotedQuery.queryCompletion(serverInfo, originalQuery, dir.toString(), madisDB,
                            storageTable, (int) (endTime - startTime), size);
                } catch (SQLException | IOException | InterruptedException ex) {
                    java.util.logging.Logger.getLogger(QueryExecutionImplementation.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        };
        queryExecution.start();
    }

    public synchronized void executeQuery(File directory, String baseTable, String view,
                                          ProcessManager processManager) throws RemoteException, InterruptedException {

    }

}
