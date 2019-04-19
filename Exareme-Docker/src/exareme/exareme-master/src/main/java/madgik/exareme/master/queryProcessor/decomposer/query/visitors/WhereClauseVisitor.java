/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.*;
import madgik.exareme.master.queryProcessor.decomposer.query.*;

/**
 * @author heraldkllapi
 */
public class WhereClauseVisitor extends AbstractVisitor {

    private boolean hasVisitedJoin = false;

    public WhereClauseVisitor(SQLQuery query) {
        super(query);
    }

    @Override
    public Visitable visit(Visitable node) throws StandardException {
        if (node instanceof JoinNode) {
            hasVisitedJoin = true;
        }

        if (node instanceof NotNode) {
            //IS NOT NULL
            NotNode not = (NotNode) node;
            if (not.getOperand() instanceof IsNullNode) {
                IsNullNode isNull = (IsNullNode) not.getOperand();
                Operand cr = QueryUtils.getOperandFromNode(isNull.getOperand());
                // ColumnReference cr = (ColumnReference) isNull.getOperand();
                UnaryWhereCondition unary = new UnaryWhereCondition(UnaryWhereCondition.IS_NULL,
                        cr, true);
                query.getUnaryWhereConditions().add(unary);
            }
        } else if (node instanceof BinaryRelationalOperatorNode) {
            BinaryRelationalOperatorNode binOp = (BinaryRelationalOperatorNode) node;
            // Do nothing in the inner nodes of the tree
            Operand left = QueryUtils.getOperandFromNode(binOp.getLeftOperand());
            Operand right = QueryUtils.getOperandFromNode(binOp.getRightOperand());
            query.getBinaryWhereConditions()
                    .add(new NonUnaryWhereCondition(left, right, binOp.getOperator()));
        } else if (node instanceof OrNode) {
            OrNode orOp = (OrNode) node;
            // Do nothing in the inner nodes of the tree
            Operand left = QueryUtils.getOperandFromNode(orOp.getLeftOperand());
            Operand right = QueryUtils.getOperandFromNode(orOp.getRightOperand());
            query.getBinaryWhereConditions()
                    .add(new NonUnaryWhereCondition(left, right, orOp.getOperator()));

        } else if (node instanceof LikeEscapeOperatorNode) {
            LikeEscapeOperatorNode like = (LikeEscapeOperatorNode) node;
            Operand c = QueryUtils.getOperandFromNode(like.getReceiver());
            //Column c =
            //   new Column(like.getReceiver().getTableName(), like.getReceiver().getColumnName());
            CharConstantNode q = (CharConstantNode) like.getLeftOperand();
            String s = q.getString();
            //System.out.println("dddd");
            UnaryWhereCondition unary =
                    new UnaryWhereCondition(UnaryWhereCondition.LIKE, c, true, s);
            query.getUnaryWhereConditions().add(unary);

        }
        return node;
    }

    @Override
    public boolean skipChildren(Visitable node) {
        return FromSubquery.class.isInstance(node) || (node instanceof JoinNode && hasVisitedJoin)
                || (node instanceof OrNode);
    }

    public void setVisitedJoin(boolean b) {
        this.hasVisitedJoin = b;
    }


    /*  public boolean nestedJoins(Visitable node){
     if(!(node instanceof JoinNode))
     return false;
     JoinNode j=(JoinNode) node;
     if(j.getLeftResultSet() instanceof JoinNode || j.getRightResultSet() instanceof JoinNode)
     return true;
     return false;
     }*/
}
