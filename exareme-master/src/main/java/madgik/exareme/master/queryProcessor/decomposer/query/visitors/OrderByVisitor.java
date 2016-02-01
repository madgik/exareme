/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.FromSubquery;
import com.foundationdb.sql.parser.OrderByList;
import com.foundationdb.sql.parser.Visitable;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import org.apache.log4j.Logger;

/**
 * @author heraldkllapi
 */
public class OrderByVisitor extends AbstractVisitor {


    public OrderByVisitor(SQLQuery query) {
        super(query);
    }

    @Override public Visitable visit(Visitable node) throws StandardException {
        if (node instanceof OrderByList) {
            OrderByColumnVisitor columnVisitor = new OrderByColumnVisitor(query);
            node.accept(columnVisitor);
            return node;
        }
        return node;
    }

    @Override public boolean skipChildren(Visitable node) {
        return FromSubquery.class.isInstance(node);
    }
}
