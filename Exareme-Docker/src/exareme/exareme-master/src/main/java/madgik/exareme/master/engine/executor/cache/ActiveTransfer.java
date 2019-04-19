/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.executor.cache;

/**
 * @author heraldkllapi
 */
public class ActiveTransfer {
    private final String fileName;
    private boolean isReady;
    private boolean hasError;

    public ActiveTransfer(String file) {
        this.fileName = file;
    }

    public boolean waitToCompete() {
        synchronized (fileName) {
            while (!isReady) {
                try {
                    fileName.wait();
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
            return !hasError;
        }
    }

    public void setReady() {
        synchronized (fileName) {
            isReady = true;
            hasError = false;
            fileName.notifyAll();
        }
    }

    public void setError() {
        synchronized (fileName) {
            isReady = true;
            hasError = true;
            fileName.notifyAll();
        }
    }

    public String getFileName() {
        return fileName;
    }
}
