/*
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator.data;

import com.sun.management.UnixOperatingSystemMXBean;
import madgik.exareme.worker.art.concreteOperator.AbstractMiMo;
import madgik.exareme.worker.art.concreteOperator.DataTransferOperatorException;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vaggelis Nikolopoulos
 */
public class DataTransferRegister extends AbstractMiMo {

    private static Logger log = Logger.getLogger(DataTransferRegister.class);

    @Override public void run() throws Exception {
        log.info("Starting DataTransfer Register, out operators: " + super.getParameterManager()
            .getOutOperators());
        int size = super.getParameterManager().getOutOperators().size();
        int datatranferID = super.getDataTransferManagerDTP().addDataTransfer(this, size);

        String FIP = null, Fport = null, TIP = null, Tport = null, filename = null;
        String sessionName = null;
        boolean hasExeption = false;
        List<String> failedOutOperators = new LinkedList<>();
        //     exit(0);//JV91
        for (String outOp : super.getParameterManager().getOutOperators()) {
            for (Parameter param : super.getParameterManager().getOutOperatorParameters(outOp)) {
                if (param.name.equals("Name")) {
                    sessionName = param.value;
                    filename = param.value;
                } else if (param.name.equals(OperatorEntity.FROM_CONTAINER_IP_PARAM)) {
                    FIP = param.value;
                } else if (param.name.equals(OperatorEntity.FROM_CONTAINER_PORT_PARAM)) {
                    Fport = param.value;
                } else if (param.name.equals(OperatorEntity.TO_CONTAINER_IP_PARAM)) {
                    TIP = param.value;
                } else if (param.name.equals(OperatorEntity.TO_CONTAINER_PORT_PARAM)) {
                    Tport = param.value;
                }

            }

            log.debug("SessionName: " + sessionName);
            File sessionFile = super.getDiskManager().getGlobalSession().requestAccess(sessionName);
            log.debug("SessionFile: " + sessionFile.getAbsolutePath());

            ObjectInputStream sessionFileStream = null;
            String tableFile = null;
            try {
                sessionFileStream = new ObjectInputStream(
                    super.getDiskManager().getGlobalSession().openInputStream(sessionFile));

                tableFile = (String) sessionFileStream.readObject();
            } finally {
                try {
                    if (sessionFileStream != null)
                        sessionFileStream.close();
                } catch (Exception e) {
                    log.warn("Unable to close stream.", e);
                }

            }

            //filename : tableFile san periexomeno
            // tableFile : data
            //
            try {
                super.getDataTransferManagerDTP()
                    .addRegister(datatranferID, filename, tableFile, FIP, Fport, TIP, Tport,
                        super.getSessionManager().getSessionID());
            } catch (Exception e) {
                log.debug("data transfer register exception: " + e.toString());
                hasExeption = true;
                failedOutOperators.add(outOp);
            }
        }

        if (hasExeption) {
            OperatingSystemMXBean oss = ManagementFactory.getOperatingSystemMXBean();
            long cc = (((UnixOperatingSystemMXBean) oss).getOpenFileDescriptorCount());

            Exception ex = new DataTransferOperatorException(failedOutOperators, cc);
            throw ex;
        }

        super.getAdaptorManager().closeAllOutputs();
        super.getAdaptorManager().closeAllInputs();


        log.debug(exitMessage);

    }

}
