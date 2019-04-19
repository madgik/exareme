package madgik.exareme.worker.art.concreteOperator;

import java.io.Serializable;

/**
 * @author orestisp
 */
public class ConcreteOperatorInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    public final String className;
    public final int seqNum;
    private int[] seqArr;

    public ConcreteOperatorInfo(String operatorName) {
        if (operatorName == null) {
            className = null;
            seqNum = -1;
            seqArr = null;
        } else if (operatorName.indexOf('_') < 0) {
            className = operatorName;
            seqNum = -1;
            seqArr = null;
        } else {
            String[] parts = operatorName.split("_");
            className = parts[0];
            seqArr = new int[parts.length - 1];
            for (int i = 0; i != seqArr.length; ++i) {
                try {
                    seqArr[i] = Integer.parseInt(parts[i + 1]);
                } catch (Exception e) {
                    seqArr[i] = -1;
                }
            }
            seqNum = seqArr[seqArr.length - 1];
        }
    }

    public String seqNumSeq() {
        if (seqArr == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(seqArr[0]);
        for (int i = 1; i != seqArr.length; ++i) {
            buf.append('.');
            buf.append(seqArr[i]);
        }
        return buf.toString();
    }
}
