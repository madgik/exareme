/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.Visitable;
import com.foundationdb.sql.parser.Visitor;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;

/**
 * @author heraldkllapi
 */
public abstract class AbstractVisitor implements Visitor {

    protected SQLQuery query;

    public AbstractVisitor(SQLQuery query) {
        this.query = query;
    }

    @Override public boolean visitChildrenFirst(Visitable node) {
        return false;
    }

    @Override public boolean stopTraversal() {
        return false;
    }

    @Override public boolean skipChildren(Visitable node) throws StandardException {
        return false;
    }
}
