/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.FromBaseTable;
import com.foundationdb.sql.parser.FromSubquery;
import com.foundationdb.sql.parser.JoinNode;
import com.foundationdb.sql.parser.Visitable;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import madgik.exareme.master.queryProcessor.decomposer.query.Table;

/**
 * @author heraldkllapi
 */
public class FromBaseTableVisitor extends AbstractVisitor {

    public FromBaseTableVisitor(SQLQuery query) {
        super(query);
    }

    @Override public Visitable visit(Visitable node) throws StandardException {
        if (node instanceof FromBaseTable) {
            Table queryTable = new Table();
            FromBaseTable parserTable = (FromBaseTable) node;

            queryTable.setName(parserTable.getOrigTableName().getTableName());
            queryTable.setAlias(parserTable.getExposedTableName().getTableName());
            query.getInputTables().add(queryTable);
            return node;
        }

        return node;
    }

    public boolean skipChildren(Visitable node) {
        return FromSubquery.class.isInstance(node) || (node instanceof JoinNode
            && query.getJoinType() != null);
    }
}
