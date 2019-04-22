/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator;

import madgik.exareme.common.app.engine.*;
import madgik.exareme.common.consts.AdpDBArtPlanGeneratorConsts;
import madgik.exareme.common.consts.DBConstants;
import madgik.exareme.master.engine.executor.MadisDataManipulationExecutor;
import madgik.exareme.master.engine.executor.MadisProcessExecutor;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.embedded.db.TableInfo;
import madgik.exareme.utils.file.FileReaderThread;
import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.utils.file.FileWriterThread;
import madgik.exareme.utils.properties.AdpDBProperties;
import madgik.exareme.utils.properties.AdpProperties;
import madgik.exareme.utils.units.Metrics;
import madgik.exareme.worker.arm.storage.client.ArmStorageClient;
import madgik.exareme.worker.arm.storage.client.ArmStorageClientException;
import madgik.exareme.worker.arm.storage.client.ArmStorageClientFactory;
import madgik.exareme.worker.art.concreteOperator.manager.AdaptorManager;
import madgik.exareme.worker.art.concreteOperator.manager.DiskManager;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;
import madgik.exareme.worker.art.container.adaptor.ReadAdaptorWrapper;
import madgik.exareme.worker.art.container.adaptor.WriteAdaptorWrapper;
import madgik.exareme.worker.art.parameter.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;
import org.apache.log4j.Logger;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author herald
 */
public class ExecuteQueryState {

    private static final int ioBufferSize =
            AdpProperties.getArtProps().getInt("art.container.ioBufferSize_kb") * Metrics.KB;
    private static final String execMethod =
            AdpDBProperties.getAdpDBProps().getString("db.execution.method");
    private static Logger log = Logger.getLogger(ExecuteQueryState.class);
    private final String compresion =
            AdpDBProperties.getAdpDBProps().getString("db.execute.compresion");
    private AdpDBSelectOperator selOperator = null;
    private AdpDBDMOperator dmOperator = null;
    private MadisExecutorResult execResult = null;
    private DiskManager diskManager = null;
    private ProcessManager procManager = null;
    private File rootDirectory = null;
    private boolean isInitialized = false;
    // (table -> (partition -> count))
    private HashMap<String, HashMap<Integer, Integer>> inputFileMap = null;
    private HashMap<String, File> outputFiles = null;
    private boolean streaming = false;

    public ExecuteQueryState(AdpDBSelectOperator selOperator, DiskManager diskManager,
                             ProcessManager procManager, boolean streaming) {
        this(selOperator, null, diskManager, procManager, streaming);
    }

    public ExecuteQueryState(AdpDBDMOperator dmOperator, DiskManager diskManager,
                             ProcessManager procManager, boolean streaming) {
        this(null, dmOperator, diskManager, procManager, streaming);
    }

    private ExecuteQueryState(AdpDBSelectOperator selOperator, AdpDBDMOperator dmOperator,
                              DiskManager diskManager, ProcessManager procManager, boolean streaming) {
        this.selOperator = selOperator;
        this.dmOperator = dmOperator;
        this.diskManager = diskManager;
        this.procManager = procManager;
        this.streaming = streaming;
        this.inputFileMap = new HashMap<String, HashMap<Integer, Integer>>();
        this.outputFiles = new HashMap<String, File>();
    }

    @Override
    public String toString() {
        return "ExecuteQueryState{" +
                "compresion='" + compresion + '\'' +
                ", selOperator=" + selOperator +
                ", dmOperator=" + dmOperator +
                ", execResult=" + execResult +
                ", diskManager=" + diskManager +
                ", procManager=" + procManager +
                ", rootDirectory=" + rootDirectory +
                ", isInitialized=" + isInitialized +
                ", inputFileMap=" + inputFileMap +
                ", outputFiles=" + outputFiles +
                ", streaming=" + streaming +
                '}';
    }

    private void initialize() throws RemoteException {
        rootDirectory = diskManager.getContainerSession().requestAccessRandomFile("AdpDBRoot");
        rootDirectory.mkdirs();
        isInitialized = true;
    }

    public void readInputs(AdaptorManager adaptorMgr) throws Exception {
        if (isInitialized == false) {
            initialize();
        }

        if (selOperator.getTotalInputs() != adaptorMgr.getInputCount()) {
            int local = selOperator.getTotalInputs() - adaptorMgr.getInputCount();
            log.debug("Inputs \n" + "Local  : " + local + "\n" + "Remote : " + adaptorMgr
                    .getInputCount());
        }

        // The number of outputs is the same with the number of partitions.
        int numOfOutputs = selOperator.getTotalOutputs();

        // The final result is not transfered.
        if (selOperator.getQuery().getOutputTable().getTable().isTemp() == false) {
            numOfOutputs = 0;
        }

        if (numOfOutputs != adaptorMgr.getOutputCount()) {
            log.debug("Outputs \n" + "Tables        : " + numOfOutputs + "\n" + "Replicated to : "
                    + adaptorMgr.getOutputCount());
        }

        ArrayList<FileWriterThread> fileWriters = new ArrayList<FileWriterThread>();
        for (String table : selOperator.getInputTables()) {
            log.debug("Read input table: " + table);

            List<String> bnames = adaptorMgr
                    .getReadStreamAdaptorNamesByParam(AdpDBArtPlanGeneratorConsts.BUFFER_TABLE_NAME,
                            table);

            if (bnames == null) {
                log.debug("table : " + table + " is local!");
                continue;
            }

            HashSet<String> used = new HashSet<String>();
            List<Integer> partitions = selOperator.getInputPartitions(table);
            for (int part : partitions) {
                log.debug("Read partition: " + table + "/" + part);
                String readFrom = null;

                for (String bname : bnames) {
                    if (used.contains(bname)) {
                        continue;
                    }

                    Parameters params = adaptorMgr.getInputParams(bname);
                    List<Parameter> param =
                            params.getParameter(AdpDBArtPlanGeneratorConsts.BUFFER_TABLE_PART_NAME);

                    if (param.size() != 1) {
                        throw new RuntimeException(
                                AdpDBArtPlanGeneratorConsts.BUFFER_TABLE_PART_NAME
                                        + " param not found or are more than one!");
                    }

                    int paramPart = Integer.parseInt(param.get(0).getValue());
                    if (part == paramPart) {
                        readFrom = bname;
                        break;
                    }
                }

                if (readFrom == null) {
                    throw new RuntimeException(
                            "Input for partition not found: " + table + "/" + part);
                }

                log.debug("Read partition: " + table + "/" + part + " from input: " + readFrom);
                used.add(readFrom);
                ReadAdaptorWrapper inStream = adaptorMgr.getReadStreamAdaptor(readFrom);
                fileWriters.add(this.readTable(table, part, inStream.getInputStream()));
            }
        }

        log.debug("Waiting the reading of the tables ... ");
        for (FileWriterThread writer : fileWriters) {
            if (writer != null) {
                writer.join();
                if (writer.getException() != null) {
                    throw new ServerException("Cannot read tables ... ", writer.getException());
                }
            }
        }
        log.debug("Finished reading tables!");
    }

    public FileWriterThread readTable(String table, int partition, InputStream in)
            throws Exception {
        if (!isInitialized) {
            initialize();
        }
        HashMap<Integer, Integer> partFileMap = inputFileMap.get(table);
        if (partFileMap == null) {
            partFileMap = new HashMap<Integer, Integer>();
            inputFileMap.put(table, partFileMap);
        }
        if (!partFileMap.containsKey(partition)) {
            partFileMap.put(partition, 0);
        }
        int subPart = partFileMap.get(partition);

        File tableFile;
        tableFile = diskManager.getContainerSession().requestAccess(rootDirectory,
                table + DBConstants.DB_SUBPART_SEPERATOR + partition + DBConstants.DB_SEPERATOR
                        + subPart + ".db");

        partFileMap.put(partition, subPart + 1);

        if (execMethod.equalsIgnoreCase("simple")) {
            log.debug("Reading file from input");
            FileWriterThread writerThread = null;
            if (streaming) {
                writerThread = new FileWriterThread(in, tableFile);
                writerThread.start();
            } else {
                FileUtil.readFromStream(in, tableFile);
            }
            return writerThread;
        }

        if (execMethod.equalsIgnoreCase("optimized")) {
            ObjectInputStream inStream = new ObjectInputStream(in);
            String filePath = (String) inStream.readObject();

            log.debug("Output only the file name: " + filePath);
            try {
                Pair<String, String> stdOutErr = procManager
                        .createAndRunProcess(rootDirectory, "ln", filePath,
                                tableFile.getAbsolutePath());

                if (stdOutErr.b.trim().isEmpty() == false) {
                    throw new ServerException("Cannot execute ln: " + stdOutErr.b);
                }
                log.debug(stdOutErr.a);
            } catch (RemoteException e) {
                throw new ServerException("Cannot save table: " + tableFile, e);
            }
        }

        return null;
    }

    public void executeSelect() throws RemoteException {
        if (isInitialized == false) {
            initialize();
        }

        for (String outTable : selOperator.getOutputTables()) {
            List<Integer> partitions = selOperator.getOutputPartitions(outTable);

            File file;
            for (int part : partitions) {
                file = diskManager.getContainerSession().requestAccess(rootDirectory,
                        outTable + DBConstants.DB_SEPERATOR + part + ".db");

                outputFiles.put(outTable + DBConstants.DB_SEPERATOR + part, file);
            }
        }

        // Create the executor
        MadisProcessExecutor exec = new MadisProcessExecutor(rootDirectory,
                AdpDBProperties.getAdpDBProps().getInt("db.engine.pageSize_b"),
                AdpDBProperties.getAdpDBProps().getInt("db.engine.defaultMemory_mb"), procManager);

        // Execute select query
        execResult = exec.exec(this);
    }

    public void executeDM() throws RemoteException {
        if (isInitialized == false) {
            initialize();
        }

        // Create the executor
        MadisDataManipulationExecutor exec = new MadisDataManipulationExecutor(rootDirectory,
                AdpDBProperties.getAdpDBProps().getInt("db.engine.pageSize_b"),
                AdpDBProperties.getAdpDBProps().getInt("db.engine.defaultMemory_mb"), procManager);

        // Execute db query
        execResult = exec.exec(this);
    }

    public void writeOutputs(AdaptorManager adaptorMgr) throws Exception {
        if (isInitialized == false) {
            initialize();
        }
        ArrayList<FileReaderThread> fileReaders = new ArrayList<FileReaderThread>();
        for (String table : selOperator.getOutputTables()) {
            log.debug("Write output table: " + table);

            List<String> bnames = adaptorMgr
                    .getWriteStreamAdaptorNamesByParam(AdpDBArtPlanGeneratorConsts.BUFFER_TABLE_NAME,
                            table);

            HashSet<String> used = new HashSet<String>();
            List<Integer> partitions = selOperator.getOutputPartitions(table);
            for (int part : partitions) {
                log.debug("Write partition: " + table + "/" + part);
                String writeTo = null;
                for (String bname : bnames) {
                    if (used.contains(bname)) {
                        continue;
                    }
                    Parameters params = adaptorMgr.getOutputParams(bname);
                    List<Parameter> param =
                            params.getParameter(AdpDBArtPlanGeneratorConsts.BUFFER_TABLE_PART_NAME);
                    if (param.size() != 1) {
                        throw new RuntimeException(
                                AdpDBArtPlanGeneratorConsts.BUFFER_TABLE_PART_NAME
                                        + " param not found or are more than one!");
                    }
                    int paramPart = Integer.parseInt(param.get(0).getValue());
                    if (part == paramPart) {
                        writeTo = bname;
                        break;
                    }
                }

                if (writeTo == null) {
                    throw new RuntimeException(
                            "Output for partition not found: " + table + "/" + part);
                }

                log.debug("Write partition: " + table + "/" + part + " from output: " + writeTo);
                used.add(writeTo);

                WriteAdaptorWrapper outStream = adaptorMgr.getWriteStreamAdaptor(writeTo);
                fileReaders.add(this.writeTable(table, part, outStream.getOutputStream()));
            }
        }

        log.debug("Waiting the writing of the tables ... ");
        for (FileReaderThread reader : fileReaders) {
            if (reader != null) {
                reader.join();
                if (reader.getException() != null) {
                    throw new ServerException("Cannot write tables ... ", reader.getException());
                }
            }
        }
        log.debug("Finished writing tables!");
    }

    public FileReaderThread writeTable(String table, int partition, OutputStream out)
            throws Exception {
        if (isInitialized == false) {
            initialize();
        }
        File file = outputFiles.get(table + "." + partition);
        if (file == null) {
            throw new RuntimeException("Output table not found: " + table + "/" + partition);
        }
        if (file.exists() == false) {
            file = new File(file.getAbsolutePath() + ".db");
            if (file.exists() == false) {
                throw new RuntimeException("File not found: " + file.getAbsolutePath());
            }
        }
        String filePath = file.getAbsolutePath();
        if (execMethod.equalsIgnoreCase("simple")) {
            log.debug("Output file: " + filePath);
            FileReaderThread readerThread = null;
            if (streaming) {
                readerThread = new FileReaderThread(file, out);
                readerThread.start();
            } else {
                FileUtil.writeToStream(file, out);
                out.close();
            }
            return readerThread;
        }
        if (execMethod.equalsIgnoreCase("optimized")) {
            log.debug("Output only the file name: " + filePath);
            ObjectOutputStream outStream = new ObjectOutputStream(out);
            outStream.writeObject(filePath);
            outStream.flush();
            outStream.close();
        }

        return null;
    }

    public void saveOutputTable() throws RemoteException {
        if (isInitialized == false) {
            initialize();
        }
        // get arm storage client {local/cluster}
        ArmStorageClient storageClient = null;
        try {
            log.debug("Create storage client!");
            storageClient = ArmStorageClientFactory.createArmStorageClient();
            log.debug("Try to connect!");
            storageClient.connect();
        } catch (ArmStorageClientException e) {
            throw new RemoteException("Arm storage client : " + e.toString());
        }

        for (String table : selOperator.getOutputTables()) {
            List<Integer> parts = selOperator.getOutputPartitions(table);
            for (int part : parts) {
                File file = outputFiles.get(table + DBConstants.DB_SEPERATOR + part);
                //        if (file.exists() == false) {
                //          throw new RuntimeException("File not found: " + file.getAbsolutePath());
                //        }
                String tableFile = selOperator.getQuery().getDatabaseDir() + "/" + table +
                        DBConstants.DB_SEPERATOR + part + ".db";
                //        if (new File(tableFile).exists()) {
                //          throw new RuntimeException("Table already exists : " + tableFile);
                //        }
                try {
                    //          Pair<String, String> stdOutErr = procManager.createAndRunProcess(
                    //                  rootDirectory, "ln", file.getAbsolutePath(), tableFile);
                    //
                    //          if (stdOutErr.b.trim().isEmpty() == false) {
                    //            throw new ServerException("Cannot execute ln: " + stdOutErr.b);
                    //          }
                    //          log.debug(stdOutErr.a);

                    log.debug("Try to put : " + file.getAbsolutePath() + " to " + tableFile);
                    storageClient.put(file.getAbsolutePath(), tableFile);

                } catch (Exception e) {
                    throw new ServerException("Cannot save table: " + tableFile, e);
                }
            }
        }

        try {
            storageClient.disconnect();
        } catch (ArmStorageClientException e) {
            throw new RemoteException("Arm storage client" + e.toString());
        }

    }

    public HashMap<Integer, Integer> getInputPartitions(String table) {
        return inputFileMap.get(table);
    }

    public AdpDBSelectOperator getOperator() {
        return selOperator;
    }

    public AdpDBDMOperator getDMOperator() {
        return dmOperator;
    }

    public ExecuteQueryExitMessage getExitMessage() {
        String outputTable = selOperator.getOutputTables().iterator().next();
        TableInfo tableInfo = new TableInfo(outputTable);
        ExecutionStatistics execStats = new ExecutionStatistics("-- No query", "-- No statistics");
        tableInfo.setSqlDefinition("-- Not defined for '" + outputTable + "'");
        if (execResult != null) {
            tableInfo = execResult.getTableInfo();
            execStats = execResult.getExecStats();
        }
        return new ExecuteQueryExitMessage(tableInfo, execStats, selOperator.getQuery().getId(),
                selOperator.getSerialNumber(), selOperator.getType());
    }
}
