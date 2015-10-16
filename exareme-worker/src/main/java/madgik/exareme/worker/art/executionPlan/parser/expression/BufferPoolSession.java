/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.parser.expression;

import java.io.Serializable;

/**
 * @author herald
 */
public class BufferPoolSession implements Serializable {

    public String bufferPoolSessionName = null;

    public BufferPoolSession(String bufferPoolSessionName) {
        this.bufferPoolSessionName = bufferPoolSessionName;
    }
}
