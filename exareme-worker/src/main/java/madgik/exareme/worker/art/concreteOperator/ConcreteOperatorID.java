/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.concreteOperator;

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
public class ConcreteOperatorID implements Comparable<ConcreteOperatorID>, Serializable {

    private static final long serialVersionUID = 1L;
    public long uniqueID;
    public String operatorName;
    public long sessionID;

    public ConcreteOperatorID(long uniqueId, String operatorName) {
        this.uniqueID = uniqueId;
        this.operatorName = operatorName;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ConcreteOperatorID) {
            ConcreteOperatorID opId = (ConcreteOperatorID) object;
            return (uniqueID == opId.uniqueID && operatorName.equals(opId.operatorName));
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int) (this.uniqueID ^ (this.uniqueID >>> 32));
        hash = 37 * hash + (int) (this.sessionID ^ (this.sessionID >>> 32));
        return hash;
    }

    @Override
    public int compareTo(ConcreteOperatorID opId) {
        return (int) (uniqueID - opId.uniqueID);
    }
}
