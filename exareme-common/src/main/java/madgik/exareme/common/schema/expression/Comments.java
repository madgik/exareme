/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema.expression;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author herald
 */
public class Comments implements Serializable {

    private static final long serialVersionUID = 1L;
    private ArrayList<String> lines = null;
    private StringBuilder sb = null;

    public Comments() {
        lines = new ArrayList<String>();
        sb = new StringBuilder();
    }

    public void addLine(String line) {
        lines.add(line);
        sb.append(line);
    }

    public ArrayList<String> getLines() {
        return lines;
    }

    @Override public String toString() {
        return sb.toString();
    }
}
