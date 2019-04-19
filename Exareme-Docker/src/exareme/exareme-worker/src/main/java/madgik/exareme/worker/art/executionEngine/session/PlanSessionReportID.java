/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.session;

import madgik.exareme.worker.art.executionEngine.reportMgr.PlanSessionReportManagerProxy;

import java.io.Serializable;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class PlanSessionReportID implements Comparable<PlanSessionReportID>, Serializable {
    private static final long serialVersionUID = 1L;

    public PlanSessionReportManagerProxy reportManagerProxy = null;
    private long id;

    public PlanSessionReportID(long id) {
        this.id = id;
    }

    public long getLongId() {
        return id;
    }

    @Override
    public int compareTo(PlanSessionReportID sessionID) {
        return (int) (id - sessionID.id);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof PlanSessionReportID) {
            PlanSessionReportID sessionID = (PlanSessionReportID) object;
            return (id == sessionID.id);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
