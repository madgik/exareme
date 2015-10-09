/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.session;

import java.io.Serializable;
import java.util.*;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ConcreteOperatorStatus implements Serializable {

    private static final long serialVersionUID = 1L;
    public String operatorName = null;
    public boolean hasFinished = false;
    public int exitCode = -1;
    public Serializable exitMessage = null;
    public Date exitDate = null;
    public boolean hasException = false;
    public List<Exception> exceptions = null;
    public Map<Exception, Date> exceptionDateMap = null;
    public Map<String, ConcreteOperatorTask> taskMap = null;

    public ConcreteOperatorStatus(String operatorName) {
        this.operatorName = operatorName;
        this.taskMap = Collections.synchronizedMap(new HashMap<String, ConcreteOperatorTask>());
        this.exceptions = Collections.synchronizedList(new LinkedList<Exception>());
        this.exceptionDateMap = Collections.synchronizedMap(new HashMap<Exception, Date>());
    }
}
