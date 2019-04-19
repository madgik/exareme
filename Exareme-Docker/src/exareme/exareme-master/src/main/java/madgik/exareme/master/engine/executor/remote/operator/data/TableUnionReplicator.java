/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator.data;

import madgik.exareme.common.app.engine.AdpDBSelectOperator;
import madgik.exareme.common.schema.Select;
import madgik.exareme.common.schema.Table;
import madgik.exareme.master.engine.executor.remote.operator.ExecuteQueryState;
import madgik.exareme.utils.encoding.Base64Util;
import madgik.exareme.utils.file.FileReaderThread;
import madgik.exareme.worker.art.concreteOperator.AbstractMiMo;
import madgik.exareme.worker.art.container.adaptor.WriteAdaptorWrapper;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author herald
 */
public class TableUnionReplicator extends AbstractMiMo {

    private static Logger log = Logger.getLogger(TableUnionReplicator.class);

    @Override
    public void run() throws Exception {
        log.trace("Parse DB Operator ...");
        AdpDBSelectOperator dbOp =
                Base64Util.decodeBase64(super.getParameterManager().getQueryString());

        String operatorName = super.getSessionManager().getOperatorName();
        log.debug("Operator Name : " + operatorName);
        dbOp.printStatistics(operatorName);

        log.trace("Create state ...");
        ExecuteQueryState state =
                new ExecuteQueryState(dbOp, getDiskManager(), getProcessManager(), false);

        log.debug("Read inputs ...");
        state.readInputs(super.getAdaptorManager());
        super.getAdaptorManager().closeAllInputs();

        log.debug("Execute query ...");
        Select q = state.getOperator().getQuery();
        q.clearQueryStatement();
        q.addQueryStatement(q.getQuery());
        state.executeSelect();

        ArrayList<FileReaderThread> fileReaders = new ArrayList<FileReaderThread>();
        for (String table : dbOp.getOutputTables()) {
            List<Integer> partitions = dbOp.getOutputPartitions(table);
            for (int part : partitions) {
                log.debug("Write partition: " + table + "/" + part);

                for (int out = 0; out < super.getAdaptorManager().getOutputCount(); out++) {
                    WriteAdaptorWrapper outStream =
                            super.getAdaptorManager().getWriteStreamAdaptor(out);

                    fileReaders.add(state.writeTable(table, part, outStream.getOutputStream()));
                }
            }
        }
        log.debug("Waiting the writing of the tables ... ");
        for (FileReaderThread reader : fileReaders) {
            if (reader != null) {
                reader.join();

                if (reader.getException() != null) {
                    throw new Exception("Cannot write tables ... ", reader.getException());
                }
            }
        }
        log.debug("Finished writing tables!");
        super.getAdaptorManager().closeAllOutputs();
        if (super.getAdaptorManager().getOutputCount() == 0) {
            log.debug("Save the non-temporary tables ...");
            Table outputTable = dbOp.getQuery().getOutputTable().getTable();
            if (outputTable.isTemp() == false) {
                log.debug("Saving output table (" + outputTable.getName() + ") ...");
                state.saveOutputTable();
            }
        } else {
            log.debug("Skip saving tables ... ");
        }
        log.debug(state.toString());
        exit(0, state.getExitMessage());
        //        exit(0);
    }
}
