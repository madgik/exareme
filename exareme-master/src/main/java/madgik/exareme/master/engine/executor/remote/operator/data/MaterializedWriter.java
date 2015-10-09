/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator.data;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.art.concreteOperator.AbstractSiNo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.ServerException;

/**
 * @author herald
 */
public class MaterializedWriter extends AbstractSiNo {

    private static Logger log = Logger.getLogger(MaterializedWriter.class);

    @Override public void run() throws Exception {
        ObjectInputStream inStream = null;
        File tableFile = null;
        String inputFileName = null;
        String sessionName = null;
        ObjectOutputStream sessionFileStream = null;

        try {
            log.debug("MaterializedWriter ...");
            sessionName = super.getParameterManager().getParameter("Name").get(0).getValue();
            log.debug("Session name : " + sessionName);

            inStream = new ObjectInputStream(
                super.getAdaptorManager().getReadStreamAdaptor(0).getInputStream());

            inputFileName = (String) inStream.readObject();
            log.debug("Input File Name : '" + inputFileName + "'");

            // Link the input with this file.
            tableFile =
                super.getDiskManager().getGlobalSession().requestAccessRandomFile("InputFile");
            log.debug("Table File : '" + tableFile.getAbsolutePath() + "'");

            Pair<String, String> stdOutErr = getProcessManager()
                .createAndRunProcess(tableFile.getParentFile(), "ln", inputFileName,
                    tableFile.getAbsolutePath());

            if (stdOutErr.b.trim().isEmpty() == false) {
                throw new ServerException("Cannot execute ln: " + stdOutErr.b);
            }

            // Add the name of the file.
            File sessionFile = super.getDiskManager().getGlobalSession().requestAccess(sessionName);
            sessionFileStream = new ObjectOutputStream(
                super.getDiskManager().getGlobalSession().openOutputStream(sessionFile, false));

            sessionFileStream.writeObject(tableFile.getAbsolutePath());
            sessionFileStream.flush();


        } catch (Exception e) {
            log.debug("materializedW exception about to happen");
            throw new Exception(
                "Cannot save table: " + tableFile + "(" + inputFileName + "|" + sessionName + ")",
                e);
        } finally {
            if (inStream != null)
                inStream.close();
            if (sessionFileStream != null)
                sessionFileStream.close();
            exit(0);
        }

    }
}
