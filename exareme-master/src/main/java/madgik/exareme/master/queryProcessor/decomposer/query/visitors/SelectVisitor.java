/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.FromSubquery;
import com.foundationdb.sql.parser.SelectNode;
import com.foundationdb.sql.parser.Visitable;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import org.apache.log4j.Logger;

/**
 * @author heraldkllapi
 */
public class SelectVisitor extends AbstractVisitor {

    public SelectVisitor(SQLQuery query) {
        super(query);
    }

    @Override public Visitable visit(Visitable node) throws StandardException {
        if (node instanceof SelectNode) {
            if (((SelectNode) node).isDistinct()) {
                query.setOutputColumnsDistinct(true);
            }
            // Result columns
            ResultColumnsVisitor projectVisitor = new ResultColumnsVisitor(query);
            node.accept(projectVisitor);
            // Input tables
            FromListVisitor fromVisitor = new FromListVisitor(query);
            node.accept(fromVisitor);
            // Where conditions
            WhereClauseVisitor whereVisitor = new WhereClauseVisitor(query);
            whereVisitor.setVisitedJoin(true);
	    node.accept(whereVisitor);
            // Group by
            GroupByListVisitor groupByVisitor = new GroupByListVisitor(query);
            node.accept(groupByVisitor);
            return node;
        }
        return node;
    }

    @Override public boolean skipChildren(Visitable node) {
        return FromSubquery.class.isInstance(node);
    }
}
