/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.remote.operator.data;

import madgik.exareme.utils.file.FileUtil;
import madgik.exareme.worker.art.concreteOperator.AbstractSiSo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectOutputStream;

/**
 * @author herald
 */
public class InterContainerMediatorTo extends AbstractSiSo {

    private static Logger log = Logger.getLogger(InterContainerMediatorTo.class);

    @Override
    public void run() throws Exception {

        //        String sessionName = super.getParameterManager().getParameter("Name").get(0).getValue();

        InputStream in = super.getAdaptorManager().getReadStreamAdaptor(0).getInputStream();
        File tableFile =
                super.getDiskManager().getGlobalSession().requestAccessRandomFile("InputFile");

        log.debug("Reading file from input stream to file '" + tableFile.getAbsolutePath() + "'");
        FileUtil.readFromStream(in, tableFile);
        in.close();
        log.debug("File read!");

        ObjectOutputStream outStream = new ObjectOutputStream(
                super.getAdaptorManager().getWriteStreamAdaptor(0).getOutputStream());

        log.debug("Writing file name ... ");
        outStream.writeObject(tableFile.getAbsolutePath());
        outStream.flush();
        outStream.close();
        log.debug("File name written!");

        exit(0);
    }
}
