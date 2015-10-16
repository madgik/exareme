/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator.data;

import madgik.exareme.worker.art.concreteOperator.AbstractNiSo;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author herald
 */
public class MaterializedReader extends AbstractNiSo {

    @Override public void run() throws Exception {
        String sessionName = super.getParameterManager().getParameter("Name").get(0).getValue();

        // Read the name of the file and the random file.
        File sessionFile = super.getDiskManager().getGlobalSession().requestAccess(sessionName);
        ObjectInputStream sessionFileStream = new ObjectInputStream(
            super.getDiskManager().getGlobalSession().openInputStream(sessionFile));

        String tableFile = (String) sessionFileStream.readObject();

        sessionFileStream.close();

        ObjectOutputStream outStream = new ObjectOutputStream(
            super.getAdaptorManager().getWriteStreamAdaptor(0).getOutputStream());

        outStream.writeObject(tableFile);
        outStream.close();

        exit(0);
    }
}
