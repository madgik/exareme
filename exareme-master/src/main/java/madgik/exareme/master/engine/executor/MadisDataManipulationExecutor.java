package madgik.exareme.master.engine.executor;

import madgik.exareme.common.app.engine.AdpDBDMOperator;
import madgik.exareme.common.app.engine.AdpDBOperatorType;
import madgik.exareme.common.app.engine.MadisExecutorResult;
import madgik.exareme.master.engine.executor.remote.operator.ExecuteQueryState;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.art.concreteOperator.manager.ProcessManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class MadisDataManipulationExecutor {

    private static Logger log = Logger.getLogger(MadisDataManipulationExecutor.class);
    private File directory = null;
    private int page_size_B = 0;
    private int memory_MB = 0;
    private int pages = 0;
    private ProcessManager procManager = null;

    public MadisDataManipulationExecutor(File directory, int page_size_B, int memory_MB,
        ProcessManager procManager) {
        this.directory = directory;
        this.page_size_B = page_size_B;
        this.memory_MB = memory_MB;
        this.pages = memory_MB * 1024 * 1024 / page_size_B;
        this.procManager = procManager;

        log.debug("Directory is : " + directory.getAbsoluteFile());
    }

    public MadisExecutorResult exec(ExecuteQueryState state) throws RemoteException {
        try {
            StringBuilder queryScript = new StringBuilder();

            // Add pragmas
            queryScript.append("PRAGMA journal_mode = OFF; \n");
            queryScript.append("PRAGMA synchronous = OFF; \n");
            //      queryScript.append("PRAGMA automatic_index = FALSE; \n");
            queryScript.append("PRAGMA locking_mode = EXCLUSIVE; \n");
            queryScript.append("PRAGMA cache_size = " + pages + "; \n");
            queryScript.append("PRAGMA page_size = " + page_size_B + "; \n");
            queryScript.append("PRAGMA page_size; \n");
            queryScript.append("\n");

            AdpDBDMOperator dmOperator = state.getDMOperator();
            int part = dmOperator.getPart();

            String databaseDir =
                new File(dmOperator.getDMQuery().getDatabaseDir()).getAbsolutePath();
            String tableName = dmOperator.getDMQuery().getTable();
            String tableLocation = databaseDir + "/" + tableName + "." + part + ".db";
            String tempTableLocation =
                directory.getAbsoluteFile() + "/" + tableName + "." + part + ".db";
            if (!dmOperator.getType().equals(AdpDBOperatorType.dropTable)) {
                ExecUtils.copyPartition(tableLocation, tempTableLocation, directory, procManager);
            }
            String query = dmOperator.getDMQuery().getQuery();
            queryScript.append(query + ";");

            log.debug("Executing script : \n" + queryScript.toString());
            log.debug("Main Database    : " + tableLocation + " \n");
            log.debug("Temp Database    : " + tempTableLocation + " \n");

            // Run query
            if (!dmOperator.getType().equals(AdpDBOperatorType.dropTable)) {
                ExecUtils.runQueryOnTable(queryScript, tempTableLocation, directory, procManager);
            }

            log.debug("Moving partition from temp to database ...");
            try {
                if (dmOperator.getType().equals(AdpDBOperatorType.dropTable)) {
                    Pair<String, String> stdOutErr =
                        procManager.createAndRunProcess(directory, "rm", tableLocation);
                    if (stdOutErr.b.trim().isEmpty() == false) {
                        throw new ServerException("Cannot execute rm: " + stdOutErr.b);
                    }
                    log.debug(stdOutErr.a);
                } else {
                    Pair<String, String> stdOutErr = procManager
                        .createAndRunProcess(directory, "mv", tempTableLocation, tableLocation);
                    if (stdOutErr.b.trim().isEmpty() == false) {
                        throw new ServerException("Cannot execute mv: " + stdOutErr.b);
                    }
                    log.debug(stdOutErr.a);
                }
            } catch (Exception e) {
                throw new ServerException("Cannot save table: " + tableLocation, e);
            }
            return new MadisExecutorResult();
        } catch (Exception e) {
            throw new ServerException("Cannot execute sqlite", e);
        }
    }
}
