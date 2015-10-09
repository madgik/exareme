/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.file;

import java.io.File;
import java.util.List;

/**
 * @author herald
 */
public class FileScannerThread extends Thread {

    private List<String> lines = null;
    private File file = null;
    private Exception exception = null;

    public FileScannerThread(File file) {
        this.file = file;
    }

    @Override public void run() {
        try {
            lines = FileUtil.realFileLines(file);
        } catch (Exception e) {
            exception = e;
        }
    }

    public Exception getException() {
        return exception;
    }

    public List<String> getLines() {
        return lines;
    }
}
