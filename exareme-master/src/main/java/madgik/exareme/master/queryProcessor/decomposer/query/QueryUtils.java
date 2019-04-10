/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.query;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.*;
import madgik.exareme.master.queryProcessor.decomposer.query.visitors.ColumnReferenceVisitor;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dimitris
 */
public class QueryUtils {

    public static Operand getOperandFromNode(ValueNode node) {
        Operand result = new Constant();
        // node.treePrint();
        if (node instanceof ColumnReference) {
            ColumnReference c = (ColumnReference) node;
            result = new Column(c.getTableName(), c.getColumnName());
        } else if (node instanceof ConstantNode) {
            ConstantNode cn = (ConstantNode) node;

            if (cn.getValue() == null) {
                result = new Constant("null");
            } else if (cn.getNodeType() == NodeTypes.CHAR_CONSTANT_NODE) {
                if (((ConstantNode) node).getValue().toString().equals("'")) {
                    // parser bug!!!!
                    result = new Constant("''''");
                } else {
                    result = new Constant("'"
                            + ((ConstantNode) node).getValue().toString() + "'");
                }
            } else {
                result = new Constant(((ConstantNode) node).getValue()
                        .toString());
            }

            if (arithmeticConstantNodes().contains(cn.getNodeType())) {
                ((Constant) result).setArithmetic(true);
            }
        } else if (node instanceof BinaryOperatorNode) {
            BinaryOperatorNode bnode = (BinaryOperatorNode) node;
            BinaryOperand bo = new BinaryOperand();
            bo.setOperator(bnode.getOperator());
            bo.setLeftOp(getOperandFromNode(bnode.getLeftOperand()));
            bo.setRightOp(getOperandFromNode(bnode.getRightOperand()));
            result = bo;
        } else if (node instanceof AggregateNode) {
            AggregateNode call = (AggregateNode) node;
            Function func = new Function();
            func.setFunctionName(call.getAggregateName().toLowerCase());
            // func.outputName = parserColumn.getName();
            func.addParameter(getOperandFromNode(call.getOperand()));
            result = func;
        } else if (node instanceof JavaToSQLValueNode) {
            Function func = new Function();
            JavaValueNode jvn = ((JavaToSQLValueNode) node).getJavaValueNode();
            if (jvn instanceof StaticMethodCallNode) {
                StaticMethodCallNode call = (StaticMethodCallNode) ((JavaToSQLValueNode) node)
                        .getJavaValueNode();
                func.setFunctionName(call.getMethodName().toLowerCase());
                // Params
                for (JavaValueNode param : call.getMethodParameters()) {
                    SQLToJavaValueNode jNode = (SQLToJavaValueNode) param;
                    func.addParameter(getOperandFromNode(jNode
                            .getSQLValueNode()));
                }
            } else if (jvn instanceof NonStaticMethodCallNode) {
                try {
                    NonStaticMethodCallNode call = (NonStaticMethodCallNode) ((JavaToSQLValueNode) node)
                            .getJavaValueNode();
                    ColumnReferenceVisitor crv = new ColumnReferenceVisitor();
                    call.accept(crv);
                    StringBuilder functionName = new StringBuilder();
                    if (crv.getTablename() != null) {
                        functionName.append(crv.getTablename());
                        functionName.append(".");
                    }
                    if (crv.getColumnname() != null) {
                        functionName.append(crv.getColumnname());
                        functionName.append(".");
                    }
                    functionName.append(call.getMethodName());
                    func.setFunctionName(functionName.toString());
                    // Params
                    for (JavaValueNode param : call.getMethodParameters()) {
                        SQLToJavaValueNode jNode = (SQLToJavaValueNode) param;
                        func.addParameter(getOperandFromNode(jNode
                                .getSQLValueNode()));
                    }
                } catch (StandardException ex) {
                    Logger.getLogger(QueryUtils.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }

            result = func;
        } else if (node instanceof CastNode) {
            CastNode cNode = (CastNode) node;
            // parses for some reason parses CAST AS CHAR -> CAST AS CHAR(1)
            if (cNode.getType().getSQLstring().equalsIgnoreCase("char(1)")) {
                CastOperand co = new CastOperand(
                        QueryUtils.getOperandFromNode(cNode.getCastOperand()),
                        "CHAR(8000)");
                result = co;
            } else {
                CastOperand co = new CastOperand(
                        QueryUtils.getOperandFromNode(cNode.getCastOperand()),
                        cNode.getType().getSQLstring());
                result = co;
            }

        } else if (node instanceof NotNode) {
            // IS NOT NULL
            NotNode not = (NotNode) node;
            if (not.getOperand() instanceof IsNullNode) {
                IsNullNode isNull = (IsNullNode) not.getOperand();
                ColumnReference cr = (ColumnReference) isNull.getOperand();
                UnaryWhereCondition unary = new UnaryWhereCondition(
                        UnaryWhereCondition.IS_NULL, new Column(
                        cr.getTableName(), cr.getColumnName()), true);
                result = unary;
            }
        } else if (node instanceof IsNullNode) {
            // IS NULL
            IsNullNode isNull = (IsNullNode) node;
            ColumnReference cr = (ColumnReference) isNull.getOperand();
            UnaryWhereCondition unary = new UnaryWhereCondition(
                    UnaryWhereCondition.IS_NULL, new Column(cr.getTableName(),
                    cr.getColumnName()), false);
            result = unary;

        } else if (node instanceof TernaryOperatorNode) {
            // substring!

            TernaryOperatorNode ton = (TernaryOperatorNode) node;
            Function func = new Function();
            func.setFunctionName(ton.getMethodName());
            if (func.getFunctionName().equalsIgnoreCase("substring")) {
                func.setFunctionName("substr");
            }
            // func.outputName = parserColumn.getName();
            func.addParameter(getOperandFromNode(ton.getReceiver()));
            func.addParameter(getOperandFromNode(ton.getLeftOperand()));
            if (ton.getRightOperand() != null) {
                func.addParameter(getOperandFromNode(ton.getRightOperand()));
            }

            result = func;
        } else if (node instanceof SimpleStringOperatorNode) {
            SimpleStringOperatorNode vn = (SimpleStringOperatorNode) node;
            String method = vn.getMethodName();
            Operand o = getOperandFromNode(vn.getOperand());
            Function f = new Function();
            f.setFunctionName(method);
            f.addParameter(o);
            result = f;
        } else {
            // Exception?

        }

        return result;
    }

    public static Operand convertToMySQLDialect(Operand o) {
        // returns CONCAT('a', b') from 'a' || 'b'
        if (o instanceof BinaryOperand) {
            BinaryOperand bo = (BinaryOperand) o;
            if (bo.getOperator().equalsIgnoreCase("||")) {
                Function result = new Function();
                result.setFunctionName("CONCAT");
                result.addParameter(convertToMySQLDialect(bo.getLeftOp()));
                result.addParameter(convertToMySQLDialect(bo.getRightOp()));
                return result;
            }
            return o;
        }
        if (o instanceof CastOperand) {
            CastOperand co = (CastOperand) o;
            if (co.getCastType().equalsIgnoreCase("INTEGER")) {
                CastOperand signed = new CastOperand(
                        convertToMySQLDialect(co.getCastOp()), "SIGNED");
                return signed;
            }
            if (co.getCastType().equalsIgnoreCase("DOUBLE")
                    || co.getCastType().equalsIgnoreCase("FLOAT")) {
                CastOperand signed = new CastOperand(
                        convertToMySQLDialect(co.getCastOp()), "DECIMAL");
                return signed;
            }
            if (co.getCastType().equalsIgnoreCase("REAL")) {
                CastOperand signed = new CastOperand(
                        convertToMySQLDialect(co.getCastOp()), "DECIMAL");
                return signed;
            }
            if (co.getCastType().equalsIgnoreCase("TIMESTAMP")) {
                CastOperand signed = new CastOperand(
                        convertToMySQLDialect(co.getCastOp()), "DATETIME");
                return signed;
            }
            return co;
        }
        return o;
    }

    static void createOracleVarCharCast(Operand o) {
        if (o instanceof CastOperand) {
            CastOperand co = (CastOperand) o;
            if (co.getCastType().equals("CHAR(8000)")) {
                co.setCastType("VARCHAR(2200)");
            }
        } else if (o instanceof BinaryOperand) {
            BinaryOperand bo = (BinaryOperand) o;
            createOracleVarCharCast(bo.getLeftOp());
            createOracleVarCharCast(bo.getRightOp());
        } else if (o instanceof Function) {
            Function f = (Function) o;
            for (Operand p : f.getParameters()) {
                createOracleVarCharCast(p);
            }
        } else if (o instanceof NonUnaryWhereCondition) {
            NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o;
            for (Operand n : nuwc.getOperands()) {
                createOracleVarCharCast(n);
            }
        }

    }

    public static final Set arithmeticConstantNodes() {
        Set res = new HashSet();
        res.add(NodeTypes.DECIMAL_CONSTANT_NODE);
        res.add(NodeTypes.DOUBLE_CONSTANT_NODE);
        res.add(NodeTypes.FLOAT_CONSTANT_NODE);
        res.add(NodeTypes.INT_CONSTANT_NODE);
        res.add(NodeTypes.LONGINT_CONSTANT_NODE);
        res.add(NodeTypes.SMALLINT_CONSTANT_NODE);
        res.add(NodeTypes.TINYINT_CONSTANT_NODE);
        return res;
    }
}
