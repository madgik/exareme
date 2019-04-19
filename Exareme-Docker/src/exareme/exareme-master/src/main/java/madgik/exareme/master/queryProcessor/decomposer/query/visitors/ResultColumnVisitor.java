/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.AllResultColumn;
import com.foundationdb.sql.parser.ResultColumn;
import com.foundationdb.sql.parser.ValueNode;
import com.foundationdb.sql.parser.Visitable;
import madgik.exareme.master.queryProcessor.decomposer.query.Operand;
import madgik.exareme.master.queryProcessor.decomposer.query.Output;
import madgik.exareme.master.queryProcessor.decomposer.query.QueryUtils;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;

/**
 * @author heraldkllapi
 */
public class ResultColumnVisitor extends AbstractVisitor {

    public ResultColumnVisitor(SQLQuery query) {
        super(query);
    }

    @Override
    public Visitable visit(Visitable node) throws StandardException {
        if (node instanceof ResultColumn) {
            ResultColumn parserColumn = (ResultColumn) node;

            if (parserColumn instanceof AllResultColumn) {
                query.setSelectAll(true);
                return node;
            }
            if (parserColumn.getExpression() instanceof ValueNode) {
                Operand op = QueryUtils.getOperandFromNode(parserColumn.getExpression());
                Output out = new Output(parserColumn.getName(), op);
                query.getOutputs().add(out);
            }
            /*  if (parserColumn.getExpression() instanceof ConstantNode) {
             Constant oc;
             ConstantNode cn = (ConstantNode) parserColumn.getExpression();
             if (cn.getValue() != null) {
             oc = new Constant(cn.getValue().toString());
             } else {
             oc = new Constant("null");
             }
             Output out = new Output(parserColumn.getName(), oc);
             query.outputs.add(out);

             }*/
            // Function output column

            /*     if (parserColumn.getExpression() instanceof JavaToSQLValueNode) {
             Function func = new Function();
             StaticMethodCallNode call = (StaticMethodCallNode) ((JavaToSQLValueNode) parserColumn.getExpression()).getJavaValueNode();
             func.setFunctionName(call.getMethodName().toLowerCase());
             // Params
             for (JavaValueNode param : call.getMethodParameters()) {
             SQLToJavaValueNode jNode = (SQLToJavaValueNode) param;
             func.addParameter(QueryUtils.getOperandFromNode(jNode.getSQLValueNode()));
             /*   Column column = new Column();
             if (jNode.getSQLValueNode().isConstantExpression()) {
             System.out.println(((ConstantNode) jNode.getSQLValueNode()).getValue());
             func.addParameter(new FunctionParameter(((ConstantNode) jNode.getSQLValueNode()).getValue()));
             } else {
             //it's a column
             ColumnReference col = (ColumnReference) jNode.getSQLValueNode();
             column.tableAlias = col.getTableName();
             column.columnName = col.getColumnName();
             func.addParameter(new FunctionParameter(column));
             }*/
            //           }
            //         Output out = new Output(parserColumn.getName(), func);
            //       query.outputs.add(out);
            // }*/

            /*  if (parserColumn.getExpression() instanceof CastNode) {
             CastNode cNode=(CastNode) parserColumn.getExpression();
             CastOperand co=new CastOperand(QueryUtils.getOperandFromNode(cNode.getCastOperand()), cNode.getType().toString());
             Output out = new Output(parserColumn.getName(), co);
             query.outputs.add(out);
                
             }*/


            return node;
        }
        return node;
    }
}
