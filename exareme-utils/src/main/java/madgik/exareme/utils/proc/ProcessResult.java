/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.proc;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class ProcessResult implements Serializable {

    public int exitCode;
    public String stdout;
    public String stderr;

    public long execTime_ms;
}
