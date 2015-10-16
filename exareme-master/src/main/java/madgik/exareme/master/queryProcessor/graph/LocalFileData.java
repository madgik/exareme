package madgik.exareme.master.queryProcessor.graph;

import java.io.Serializable;

/**
 * @author herald
 */
public class LocalFileData implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * The data name (usually the name of the file)
     */
    public String name = null;
    /**
     * Size in MB.
     */
    public double size_MB = 0;

    public LocalFileData(LocalFileData other) {
        this.name = other.name;
        this.size_MB = other.size_MB;
    }

    public LocalFileData(String name, double size_MB) {
        this.name = name;
        this.size_MB = size_MB;
    }
}
