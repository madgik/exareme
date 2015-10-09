/*
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.cache;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public class CacheInfo {

    public String directory;
    public double currentSize;
    public double totalSize;

    public CacheInfo(String directory, double currentSize) {

        this.directory = directory;
        this.currentSize = currentSize;
    }

    public CacheInfo(String directory, double totalSize, double currentSize) {

        this.directory = directory;
        this.totalSize = totalSize;
        this.currentSize = currentSize;
    }

}
