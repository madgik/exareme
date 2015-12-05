/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.ColumnReference;
import com.foundationdb.sql.parser.OrderByColumn;
import com.foundationdb.sql.parser.Visitable;
import madgik.exareme.master.queryProcessor.decomposer.query.ColumnOrderBy;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;

/**
 * @author heraldkllapi
 */
public class OrderByColumnVisitor extends AbstractVisitor {

    public OrderByColumnVisitor(SQLQuery query) {
        super(query);
    }

    @Override public Visitable visit(Visitable node) throws StandardException {
        if (node instanceof OrderByColumn) {
            ColumnOrderBy column = new ColumnOrderBy();

            ColumnReference parserColumn = (ColumnReference) ((OrderByColumn) node).getExpression();
            column.setAlias(parserColumn.getTableName());
            column.setName(parserColumn.getColumnName());
            column.isAsc = ((OrderByColumn) node).isAscending();

            query.getOrderBy().add(column);
            return node;
        }
        return node;
    }
}
