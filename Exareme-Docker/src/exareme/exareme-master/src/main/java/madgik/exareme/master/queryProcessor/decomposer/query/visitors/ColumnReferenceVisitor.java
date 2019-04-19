/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.ColumnReference;
import com.foundationdb.sql.parser.Visitable;
import com.foundationdb.sql.parser.Visitor;

/**
 * @author dimitris
 */
public class ColumnReferenceVisitor implements Visitor {

    private String tablename;
    private String columnname;

    public ColumnReferenceVisitor() {
        super();
    }

    @Override
    public Visitable visit(Visitable node) throws StandardException {
        if (node instanceof ColumnReference) {
            ColumnReference cr = (ColumnReference) node;
            tablename = cr.getTableName();
            columnname = cr.getColumnName();
        }
        return node;
    }

    @Override
    public boolean skipChildren(Visitable node) {
        return false;
    }

    @Override
    public boolean visitChildrenFirst(Visitable vstbl) {
        return false;
    }

    @Override
    public boolean stopTraversal() {
        return false;
    }

    /**
     * @return the tablename
     */
    public String getTablename() {
        return tablename;
    }

    /**
     * @return the columnname
     */
    public String getColumnname() {
        return columnname;
    }
}
