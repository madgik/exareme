/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.session;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ConcreteOperatorTask implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name = null;
    private Date start = null;
    private Date end = null;

    public ConcreteOperatorTask(String name) {
        this.name = name;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public boolean hasEnded() {
        return end != null;
    }

    public long getDuration() {
        if (end == null) {
            return new Date().getTime() - start.getTime();
        } else {
            if (start == null) {
                return 0;
            } else {
                return end.getTime() - start.getTime();
            }
        }
    }

    public String getName() {
        return name;
    }
}
