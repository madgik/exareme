/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator.data;

import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.worker.art.concreteOperator.AbstractSiSo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.OutputStream;

/**
 * @author herald
 */
public class InterContainerMediatorFrom extends AbstractSiSo {

    private static Logger log = Logger.getLogger(InterContainerMediatorFrom.class);

    @Override public void run() throws Exception {
        ObjectInputStream inStream = new ObjectInputStream(
            super.getAdaptorManager().getReadStreamAdaptor(0).getInputStream());

        log.debug("Reading file name ... ");
        String fileName = (String) inStream.readObject();
        inStream.close();
        log.debug("File name is '" + fileName + "'");

        File file = new File(fileName);
        OutputStream out = super.getAdaptorManager().getWriteStreamAdaptor(0).getOutputStream();

        log.debug("Writing file to output stream ... ");
        FileUtil.writeToStream(file, out);
        out.flush();
        out.close();

        log.debug("File written!");
        exit(0);
    }
}
