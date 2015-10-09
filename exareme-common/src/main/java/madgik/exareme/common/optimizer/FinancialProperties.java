/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.optimizer;

import java.io.Serializable;

/**
 * @author Herald Kllapi <br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class FinancialProperties implements Serializable {

    private static final long serialVersionUID = 1L;
    public double timeQuantumCost = 1.0;
    public double ioCostPerMBPerQuantum = 0.0;
    public double storageCostPerMBPerQuantum = 1.0;

    public FinancialProperties() {
    }

    public FinancialProperties(FinancialProperties from) {
        this.timeQuantumCost = from.timeQuantumCost;
        this.ioCostPerMBPerQuantum = from.ioCostPerMBPerQuantum;
        this.storageCostPerMBPerQuantum = from.storageCostPerMBPerQuantum;
    }
}
