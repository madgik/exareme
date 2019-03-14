/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.FromBaseTable;
import com.foundationdb.sql.parser.Visitable;
import com.foundationdb.sql.parser.Visitor;
import madgik.exareme.master.queryProcessor.decomposer.query.Table;

import java.util.ArrayList;

/**
 * @author dimitris
 */
public class BaseTableCrawlerVisitor implements Visitor {

    protected ArrayList<Table> tables;

    public BaseTableCrawlerVisitor(ArrayList<Table> baseTables) {
        this.tables = baseTables;
    }

    @Override
    public boolean visitChildrenFirst(Visitable node) {
        return false;
    }

    @Override
    public boolean stopTraversal() {
        return false;
    }

    @Override
    public boolean skipChildren(Visitable node) throws StandardException {
        return false;
    }

    @Override
    public Visitable visit(Visitable vstbl) throws StandardException {
        if (vstbl instanceof FromBaseTable) {
            Table queryTable = new Table();
            FromBaseTable parserTable = (FromBaseTable) vstbl;

            queryTable.setName(parserTable.getOrigTableName().getFullTableName());
            queryTable.setAlias("alias");
            if (!tables.contains(queryTable)) {
                tables.add(queryTable);
            }

        }
        return vstbl;
    }
}
