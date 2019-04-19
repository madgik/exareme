/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.file;

import java.io.File;
import java.io.InputStream;

/**
 * @author herald
 */
public class FileWriterThread extends Thread {

    private InputStream in = null;
    private File file = null;
    private Exception exception = null;

    public FileWriterThread(InputStream in, File file) {
        this.in = in;
        this.file = file;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public void run() {
        try {
            FileUtil.readFromStream(in, file);
        } catch (Exception e) {
            exception = e;
        }
    }
}
