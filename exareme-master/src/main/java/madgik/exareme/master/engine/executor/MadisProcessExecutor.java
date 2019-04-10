/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor;

import madgik.exareme.common.app.engine.ExecutionStatistics;
import madgik.exareme.common.app.engine.MadisExecutorResult;
import madgik.exareme.common.consts.DBConstants;
import madgik.exareme.common.schema.TableView;
import madgik.exareme.common.schema.expression.DataPattern;
import madgik.exareme.master.engine.executor.remote.operator.ExecuteQueryState;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.embedded.db.DBUtils;
import madgik.exareme.utils.embedded.db.SQLDatabase;
import madgik.exareme.utils.embedded.db.TableInfo;
import madgik.exareme.utils.properties.AdpDBProperties;
import madgik.exareme.worker.arm.storage.client.ArmStorageClient;
import madgik.exareme.worker.arm.storage.client.ArmStorageClientException;
import madgik.exareme.worker.arm.storage.client.ArmStorageClientFactory;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author herald
 */
public class MadisProcessExecutor {
    private static final String additionalCommands =
            AdpDBProperties.getAdpDBProps().getString("db.engine.script.additional");
    private static final Logger log = Logger.getLogger(MadisProcessExecutor.class);
    private File directory = null;
    private long page_size_B = 0;
    private int memory_MB = 0;
    private long pages = 0;
    private ProcessManager procManager = null;
    private String compresion = AdpDBProperties.getAdpDBProps().getString("db.execute.compresion");

    public MadisProcessExecutor(File directory, int page_size_B, int memory_MB,
                                ProcessManager procManager) {
        this.directory = directory;
        this.page_size_B = page_size_B;
        this.memory_MB = memory_MB;
        this.pages = memory_MB * 1024L * 1024L / this.page_size_B;
        this.procManager = procManager;
        log.debug("Directory is : " + directory.getAbsoluteFile());
    }

    public MadisExecutorResult exec(ExecuteQueryState state) throws RemoteException {
        try {
            StringBuilder script = new StringBuilder();
            script.append("\n-- Script BEGIN \n\n");
            // Add pragmas
            script.append("-- Optimization Pragmas \n");
            script.append("PRAGMA journal_mode = OFF; \n");
            script.append("PRAGMA synchronous = OFF; \n");
            script.append("PRAGMA automatic_index = TRUE; \n");
//            script.append("PRAGMA locking_mode = EXCLUSIVE; \n");
            script.append("PRAGMA auto_vacuum = NONE; \n");
            script.append("PRAGMA ignore_check_constraints = true; \n");
            script.append("PRAGMA cache_size = " + pages + "; \n");
            script.append("PRAGMA page_size = " + page_size_B + "; \n");
            script.append("PRAGMA page_size; \n");
            script.append("\n");
            script.append("-- Additional Commands \n");
            if (additionalCommands != null) {
                script.append(additionalCommands + "\n");
            }
            script.append("\n");

            HashMap<String, ArrayList<String>> nonLocalTableDatabases = new HashMap<>();

            HashMap<String, HashMap<Integer, Integer>> currentCount = new HashMap<>();

            HashMap<String, ArrayList<String>> nonLocalTablePartLocations = new HashMap<>();

            String dbDir = state.getOperator().getQuery().getDatabaseDir();
            Boolean broadcast = state.getOperator().getQuery().getOutputTable().getPattern() == DataPattern.broadcast;
            String absoluteDBDir = new File(dbDir).getAbsolutePath();

            // Append additional tables and views
            String mappings = state.getOperator().getQuery().getMappings();
            script.append("-- Mappings \n");
            if (mappings != null) {
                script.append(mappings);
            }
            script.append("\n");

            int numInputDatabases = 0;

            ArmStorageClient storageClient = null;
            try {
                storageClient = ArmStorageClientFactory.createArmStorageClient();
                storageClient.connect();
            } catch (ArmStorageClientException e) {
                log.error("Error occured while storage client try to connect! ");
                throw new RemoteException();
            }

            // Attach the input tables.
            StringBuilder attachedDBs = new StringBuilder();
            for (String input : state.getOperator().getInputTables()) {
                List<Integer> parts = state.getOperator().getInputPartitions(input);
                for (int part : parts) {
                    String loc = null;
                    String dbName = null;
                    boolean local = false;
                    HashMap<Integer, Integer> partCount = state.getInputPartitions(input);
                    if (partCount == null) { // Local
                        loc = absoluteDBDir + "/" + input + DBConstants.DB_SEPERATOR + part + ".db";
                        dbName = input + part;
                        local = true;
                    } else {
                        Integer count = partCount.get(part);
                        if (count == null) { // Local
                            loc = absoluteDBDir + "/" + input + DBConstants.DB_SEPERATOR + part
                                    + ".db";
                            dbName = input + part;
                            local = true;
                        } else { // Remote
                            HashMap<Integer, Integer> current = currentCount.get(input);
                            if (current == null) {
                                current = new HashMap<Integer, Integer>();
                                currentCount.put(input, current);
                            }
                            if (current.containsKey(part) == false) {
                                current.put(part, 0);
                            }
                            int c = current.get(part);
                            current.put(part, c + 1);
                            loc = directory.getAbsolutePath() + "/" + input +
                                    DBConstants.DB_SUBPART_SEPERATOR + part +
                                    DBConstants.DB_SEPERATOR + c + ".db";
                            dbName = input + part + c;
                        }
                    }
                    //          if dfs is used then can not skip
                    //          if (new File(loc).length() == 0) {
                    //            log.info("Skipping table: " + input + "." + part);
                    //            continue;
                    //          }
                    // Attach only local databases
                    if (local == true) {

                        try {
                            storageClient.fetch(loc, loc);
                        } catch (ArmStorageClientException e) {
                            log.error(
                                    "Error occurred while storage client try to fetch partitions!");
                            throw new RemoteException();
                        }

                        attachedDBs.append("attach database '" + loc + "' as " + dbName + "; \n");
                        attachedDBs.append(".schema " + dbName + "." + input + "; \n");
                        numInputDatabases++;
                    } else {
                        // Database name
                        ArrayList<String> databases = nonLocalTableDatabases.get(input);
                        if (databases == null) {
                            databases = new ArrayList<String>();
                            nonLocalTableDatabases.put(input, databases);
                        }
                        databases.add(dbName);
                        // Database location
                        ArrayList<String> locations = nonLocalTablePartLocations.get(input);
                        if (locations == null) {
                            locations = new ArrayList<String>();
                            nonLocalTablePartLocations.put(input, locations);
                        }
                        locations.add(loc);
                    }
                }
            }
            // Create input tables
            StringBuilder createTables = new StringBuilder();
            for (String input : nonLocalTableDatabases.keySet()) {
                String loc = nonLocalTablePartLocations.get(input).get(0);
                if (nonLocalTablePartLocations.get(input).size() == 1) {
                    //          attachedDBs.append("attach database '" + loc + "' as " + input + "; \n");
                    //          attachedDBs.append(".schema " + input + "." + input + "; \n");
                    attachedDBs.append("attach database '" + loc + "' as " + input + "_0; \n");
                    attachedDBs.append(".schema " + input + "_0." + input + "; \n");
                    attachedDBs.append("create table " + input + " as select 0 as __local_id, * from " + input + "_0." + input + ";\n");
                    numInputDatabases++;

                } else {
                    ArrayList<String> locations = nonLocalTablePartLocations.get(input);
                    for (int part = 0; part < locations.size(); ++part) {
                        String dbName = input + part;
                        createTables.append(
                                "attach database '" + locations.get(part) + "' as " + dbName + "; \n");
                        if (part == 0) {
                            createTables.append(
                                    "create  table " + input + " as select " + part
                                            + " as __local_id, * from " + dbName + "." + input + "; \n");
                            //                            createTables.append(
//                                "create temp table " + input + " as select * from " + dbName + "."
//                                    + input + "; \n");
                        } else {
                            createTables.append(
                                    "insert into " + input + " select " + part
                                            + " as __local_id, * from " + dbName + "." + input + "; \n");
                            //                            createTables.append(
//                                "insert into " + input + " select * from " + dbName + "." + input
//                                    + "; \n");
                        }
                        createTables.append("detach database " + dbName + "; \n");
                    }
                    createTables.append(".schema " + input + "; \n");
                }
            }
            script.append("-- Attach databases \n");
            script.append(attachedDBs);
            script.append("-- Create tables \n");
            script.append(createTables);
            script.append("\n");

            String query = state.getOperator().getQuery().getSelectQueryStatement();
            log.debug("Query : " + query);
            TableView output = state.getOperator().getQuery().getOutputTable();
            String outputTable = output.getTable().getName();
            String madisMainDB = outputTable + ".dummy.db";

            //            script.append("-- Explain Query \n");
            // Skip explain query <- valuable when the has net communications with external sources (e.g. oracle)
            //            script.append("explain query plan " + query + ";\n\n");

            script.append("-- Run Query \n");
            List<String> queryStms = state.getOperator().getQuery().getQueryStatements();
            for (int queryNo = 0; queryNo < (queryStms.size() - 1); ++queryNo) {
                script.append(queryStms.get(queryNo) + ";\n\n");
            }
            int outputParts = output.getNumOfPartitions();
            if (outputParts < 2) {
                // Create the query
                script.append("create table " + outputTable + " as \n");
                script.append(query + ";\n\n");
                List<Integer> parts = state.getOperator().getOutputPartitions(outputTable);
                if (parts.size() != 1) {
                    throw new ServerException(
                            "Output table extected to have only one output but has " + parts.size());
                }
                int part = parts.get(0);

                madisMainDB = outputTable + DBConstants.DB_SEPERATOR + part + ".db";
            } else {
                if (broadcast) {
                    script.append("output split:1 '" + outputTable + ".db' select 0, * from (" + query + ") as q;\n\n");
                } else {
                    script.append("output split:" + outputParts + " '" + outputTable + ".db'");
//                    script.append(" select hashmd5mod(");
                    // TODO(herald): change it to the following ...
                    script.append(" select hashmodarchdep(");
                    for (String column : output.getPatternColumnNames()) {
                        script.append(column + ", ");
                    }
                    script.append(outputParts + "),* from (" + query + ") as q;\n\n");
                }
            }
            script.append("-- Cleanup \n");
            for (String input : nonLocalTableDatabases.keySet()) {
                ArrayList<String> databases = nonLocalTableDatabases.get(input);
                if (databases.size() == 1) {
                    continue;
                }
            }
            script.append("\n-- Script END \n\n");
            log.debug("Executing script : \n" + script.toString());
            log.debug(
                    "Main Database    : " + directory.getAbsolutePath() + "/" + madisMainDB + " \n");

            // optimization -> if the result is select * ... then
            //                 just alter the input table to the output table.
            //                 This is the case in the import.
            String simpleQuery = "select_*_from_" + outputTable;
            String inputQuery =
                    query.trim().replaceAll("( )+", " ").replaceAll("\n", " ").replaceAll(" ", "_");
            log.debug("Input Query: '" + inputQuery + "'");
            log.debug("Simple Query: '" + simpleQuery + "'");

            String stats = "";
            if (numInputDatabases == 1 &&
                    (inputQuery.equals("select_*_from_" + outputTable) ||
                            inputQuery.equals("select_from_" + outputTable))) {
                log.debug("Optimized ... just use ln ...");
                String input = state.getOperator().getInputTables().iterator().next();
                if (nonLocalTablePartLocations.get(input) == null) {
                    log.warn("input non local & non remote ?");
                } else {
                    String loc = nonLocalTablePartLocations.get(input).get(0);
                    Pair<String, String> stdOutErr = procManager
                            .createAndRunProcess(directory, "ln", loc,
                                    directory.getAbsolutePath() + "/" + madisMainDB);

                    if (stdOutErr.b.trim().isEmpty() == false) {
                        throw new ServerException("Cannot execute ln: " + stdOutErr.b);
                    }
                    log.debug(stdOutErr.a);
                }
            } else {
                // Run query
                stats = ExecUtils.runQueryOnTable(script, madisMainDB, directory, procManager);
            }

            String outputDBFile = null;
            log.debug("Check if all output files are created ...");
            if (outputParts > 1) {
                File f = null;
                for (int part = 0; part < outputParts; ++part) {
                    if (broadcast && part != 0) {
                        File clonefile = new File(directory.getAbsolutePath() + "/" + outputTable + "." + part + ".db");
//                        clonefile.createNewFile();
                        Pair<String, String> stdOutErr = procManager.createAndRunProcess(
                                directory,
                                "ln",
                                directory.getAbsolutePath() + "/" + outputTable + ".0.db",
                                clonefile.getAbsolutePath()
                        );

                        if (stdOutErr.b.trim().isEmpty() == false) {
                            throw new ServerException("Cannot execute ln: " + stdOutErr.b);
                        }
                        log.debug(stdOutErr.a);
                    }

                    f = new File(directory.getAbsolutePath() + "/" + outputTable + "." + part + ".db");

                    if (!f.exists()) {
                        throw new ServerException(
                                "Partition not found:" + outputTable + "/" + part);
                    }
                    if (f.createNewFile()) {
                        log.debug("No records in : " + outputTable + "." + part);
                    }
                }
                outputDBFile = directory.getAbsolutePath() + "/" + outputTable + "." + 0 + ".db";
            } else {
                outputDBFile = directory.getAbsolutePath() + "/" + madisMainDB;
            }
            MadisExecutorResult execResult = new MadisExecutorResult();
            SQLDatabase sqlDB = DBUtils.createEmbeddedSqliteDB(outputDBFile);
            TableInfo tableInfo = sqlDB.getTableInfo(outputTable);
            sqlDB.close();
            execResult.setTableInfo(tableInfo);
            ExecutionStatistics execStats = new ExecutionStatistics(script.toString(), stats);
            execResult.setExecStats(execStats);
            log.debug("SQL Definition is: " + execResult.getTableInfo().getSQLDefinition());

            try {
                storageClient.disconnect();
            } catch (ArmStorageClientException e) {
                log.error("Error while storage client try to disconnect!");
                throw new RemoteException();
            }

            return execResult;
        } catch (Exception e) {
            throw new ServerException("Cannot execute madis", e);
        }
    }
}
