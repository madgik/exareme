/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.file;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author herald
 */
public class FileReaderThread extends Thread {

    private File file = null;
    private OutputStream out = null;
    private Exception exception = null;

    public FileReaderThread(File file, OutputStream out) {
        this.file = file;
        this.out = out;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public void run() {
        try {
            FileUtil.writeToStream(file, out);
        } catch (Exception e) {
            exception = e;
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                }
        }
    }
}
