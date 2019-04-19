/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.FromList;
import com.foundationdb.sql.parser.FromSubquery;
import com.foundationdb.sql.parser.JoinNode;
import com.foundationdb.sql.parser.Visitable;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;

/**
 * @author heraldkllapi
 */
public class FromListVisitor extends AbstractVisitor {

    public FromListVisitor(SQLQuery query) {
        super(query);
    }

    @Override
    public Visitable visit(Visitable node) throws StandardException {
        if (node instanceof FromList) {
            FromList from = (FromList) node;
            FromBaseTableVisitor fromVisitor = new FromBaseTableVisitor(query);
            from.accept(fromVisitor);
            return node;
        }
        return node;
    }

    @Override
    public boolean skipChildren(Visitable node) {
        return FromSubquery.class.isInstance(node) || node instanceof JoinNode;
    }
}
