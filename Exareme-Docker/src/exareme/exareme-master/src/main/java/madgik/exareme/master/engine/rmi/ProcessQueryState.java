/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.rmi;

import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;

import java.util.ArrayList;

/**
 * @author heraldkllapi
 */
public class ProcessQueryState {
    public ArrayList<PhysicalTable> inputPhyTables = null;
    public ConcreteOperator[] outputs = null;
    public PhysicalTable outputTable = null;

    public ProcessQueryState() {
        inputPhyTables = new ArrayList<PhysicalTable>();
    }
}
