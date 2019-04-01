/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.FromSubquery;
import com.foundationdb.sql.parser.GroupByList;
import com.foundationdb.sql.parser.Visitable;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;

/**
 * @author heraldkllapi
 */
public class GroupByListVisitor extends AbstractVisitor {

    public GroupByListVisitor(SQLQuery query) {
        super(query);
    }

    @Override
    public Visitable visit(Visitable node) throws StandardException {
        if (node instanceof GroupByList) {
            GroupByColumnVisitor groupByVisitor = new GroupByColumnVisitor(query);
            node.accept(groupByVisitor);
        }
        return node;
    }

    @Override
    public boolean skipChildren(Visitable node) {
        return FromSubquery.class.isInstance(node);
    }
}
