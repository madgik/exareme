/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import java.io.Serializable;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class LinkData implements Serializable {
    private static final long serialVersionUID = 1L;
    // The data name (usually the name of the file)
    public String name = null;
    // Size in MB.
    public double size_MB = 0;
    // The operators that produce and consume the data
    private int fromOpID = -1;
    private int toOpID = -1;

    public LinkData(String name, double size_MB) {
        this.name = name;
        this.size_MB = size_MB;
    }

    public LinkData(LinkData from) {
        this.name = from.name;
        this.size_MB = from.size_MB;
        this.fromOpID = from.fromOpID;
        this.toOpID = from.toOpID;
    }

    public void updateLinks(Link link) {
        fromOpID = link.from.opID;
        toOpID = link.to.opID;
    }

    public int getFromOpID() {
        return fromOpID;
    }

    public void setFromOpID(int fromOpID) {
        this.fromOpID = fromOpID;
    }

    public int getToOpID() {
        return toOpID;
    }

    public void setToOpID(int toOpID) {
        this.toOpID = toOpID;
    }
}
