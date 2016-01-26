/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.decomposer.query.visitors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.*;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import org.apache.log4j.Logger;

/**
 * @author heraldkllapi
 */
public class SQLQueryVisitor extends AbstractVisitor {

    private boolean stop = false;

    public SQLQueryVisitor(SQLQuery query) {
        super(query);
    }

    @Override public Visitable visit(Visitable node) throws StandardException {


        if (node instanceof JoinNode) {
            if (query.getJoinType() == null) {
                decomposeJoinNode((JoinNode) node);
                WhereClauseVisitor whereVisitor = new WhereClauseVisitor(query);
                node.accept(whereVisitor);
            }

        }

        if (node instanceof UnionNode) {
            decomposeUnionNode((UnionNode) node);
            this.query.setHasUnionRootNode(true);
            stop = true;
        }
        if (node instanceof CursorNode) {
            CursorNode cNode = (CursorNode) node;
            if (cNode.getFetchFirstClause() != null) {
                query.setLimit(
                    (int) (Integer) ((ConstantNode) cNode.getFetchFirstClause()).getValue());
            }
            if (cNode.getResultSetNode() instanceof UnionNode) {
                //top node is a union
                UnionNode uNode = (UnionNode) cNode.getResultSetNode();

                decomposeUnionNode(uNode);
                this.query.setHasUnionRootNode(true);
                stop = true;
            }

        }
        if (node instanceof SelectNode) {
            SelectVisitor selectVis = new SelectVisitor(query);
            node.accept(selectVis);
        }
        if (node instanceof OrderByList) {
            OrderByVisitor orderVisitor = new OrderByVisitor(query);
            node.accept(orderVisitor);
        }

        if (node instanceof FromList) {
            //check if we have nested subquery (union or nested select)
            FromList fl = (FromList) node;
            for (int i = 0; i < fl.size(); i++) {
                if (fl.get(i) instanceof FromSubquery) {
                    FromSubquery from = (FromSubquery) fl.get(i);
                    String alias = from.getCorrelationName();
                    ResultSetNode rs = from.getSubquery();
                    if (rs instanceof UnionNode) {
                        UnionNode uNode = (UnionNode) rs;
                        decomposeUnionNode(uNode);
                        this.query.setUnionAlias(alias);

                    }
                    if (rs instanceof SelectNode) {
                        //nested select
                        SelectNode nestedSelectNode = (SelectNode) rs;

                        SQLQuery nestedSelectSubquery = new SQLQuery();
                        //query.readDBInfo();
                        SQLQueryVisitor subqueryVisitor = new SQLQueryVisitor(nestedSelectSubquery);
                        nestedSelectNode.accept(subqueryVisitor);

                        this.query.addNestedSelectSubquery(nestedSelectSubquery, alias);
                        //;.nestedSelectSubquery = nestedSelectSubquery;
                        //this.query.setNestedSelectSubqueryAlias(alias);
                    }
                }
            }
            //   if (fl.get(0) instanceof JoinNode) {
            //        JoinNode jNode = (JoinNode) fl.get(0);
            //        decomposeJoinNode(jNode);
            //    }


        }



        // Limit
        //    if ().getFetchFirstClause()
        return node;
    }

    @Override public boolean skipChildren(Visitable node) {
        return FromSubquery.class.isInstance(node) || (node instanceof JoinNode
            && query.getJoinType() != null);
    }

    @Override public boolean stopTraversal() {
        return stop;
    }

    private void decomposeUnionNode(UnionNode uNode) throws StandardException {
        SQLQuery leftSubquery = new SQLQuery();
        SQLQuery rightSubquery = new SQLQuery();
        //query.readDBInfo();
        SQLQueryVisitor leftVisitor = new SQLQueryVisitor(leftSubquery);
        SQLQueryVisitor rightVisitor = new SQLQueryVisitor(rightSubquery);

        if (uNode.getResultColumns() != null) {
            //uNode.getResultColumns().accept(leftVisitor);
            //uNode.getResultColumns().accept(rightVisitor);
        }
        if (uNode.getLeftResultSet() != null) {
            // uNode.getLeftResultSet().treePrint();
            if (uNode.getLeftResultSet() instanceof UnionNode) {
                decomposeUnionNode((UnionNode) uNode.getLeftResultSet());
            } else if (uNode.getLeftResultSet() instanceof JoinNode) {
                SQLQueryVisitor v = new SQLQueryVisitor(leftSubquery);
                uNode.getLeftResultSet().accept(v);
                this.query.getUnionqueries().add(leftSubquery);
            } else {
                uNode.getLeftResultSet().accept(leftVisitor);
                this.query.getUnionqueries().add(leftSubquery);
            }
        }
        if (uNode.getRightResultSet() != null) {
            // uNode.getLeftResultSet().treePrint();
            if (uNode.getRightResultSet() instanceof UnionNode) {
                decomposeUnionNode((UnionNode) uNode.getRightResultSet());
            } else if (uNode.getRightResultSet() instanceof JoinNode) {
                SQLQueryVisitor v = new SQLQueryVisitor(rightSubquery);
                uNode.getRightResultSet().accept(v);
                this.query.getUnionqueries().add(rightSubquery);
            } else {
                uNode.getRightResultSet().accept(rightVisitor);
                this.query.getUnionqueries().add(rightSubquery);
            }

        }
        //uNode.accept(visitor);
        this.query.setUnionAll(uNode.isAll());

    }

    private void decomposeJoinNode(JoinNode jNode) throws StandardException {
        SQLQuery leftSubquery = new SQLQuery();
        SQLQuery rightSubquery = new SQLQuery();
        //query.readDBInfo();


        //   if (jNode.getResultColumns() != null) {
        //uNode.getResultColumns().accept(leftVisitor);
        //uNode.getResultColumns().accept(rightVisitor);
        //   }
        if (jNode.getLeftResultSet() != null) {
            // for now we only consider that the join operators are base tables or nested joins
            if (jNode.getLeftResultSet() instanceof FromSubquery) {
                FromSubquery fs = (FromSubquery) jNode.getLeftResultSet();
                SQLQueryVisitor v = new SQLQueryVisitor(leftSubquery);
                fs.getSubquery().accept(v);
                this.query.setLeftJoinTableAlias(fs.getCorrelationName());
                //jNode.getLeftResultSet().accept(leftVisitor);
            } else if (jNode.getLeftResultSet() instanceof JoinNode) {
                SQLQueryVisitor v = new SQLQueryVisitor(leftSubquery);
                jNode.getLeftResultSet().accept(v);
                //leftSubquery.setSelectAll(true);
            } else if (jNode.getLeftResultSet() instanceof FromBaseTable) {



                FromBaseTableVisitor v = new FromBaseTableVisitor(leftSubquery);
                jNode.getLeftResultSet().accept(v);
                //leftSubquery.setSelectAll(true);
                //DO WE NEED EXTRA QUERY FOR EACH BASE TABLE????
                //FromBaseTable bt=(FromBaseTable) jNode.getLeftResultSet();
                leftSubquery.setIsBaseTable(true);
                //leftSubquery.setResultTableName(bt.getCorrelationName());
            }
            /* for now we only consider that the join operators are base tables or nested joins
             else {
             SQLQueryVisitor v=new SQLQueryVisitor(leftSubquery);
             jNode.getLeftResultSet().accept(v);
             }*/

            //System.out.println("Table "+query.getResultTableName()+" add left join table: "+leftSubquery.getResultTableName());
            this.query.setLeftJoinTable(leftSubquery);

        }
        /*if (jNode.getRightResultSet() != null) {
         // uNode.getLeftResultSet().treePrint();
         if (jNode.getRightResultSet() instanceof UnionNode) {
         decomposeUnionNode((UnionNode) jNode.getRightResultSet());
         } else if (jNode.getRightResultSet() instanceof JoinNode) {
         decomposeJoinNode((JoinNode) jNode.getRightResultSet());
         } else {
         if(jNode.getRightResultSet() instanceof FromSubquery){
         this.query.rightJoinTableAlias=((FromSubquery)jNode.getRightResultSet()).getCorrelationName();}
                
         jNode.getRightResultSet().accept(rightVisitor);
         this.query.rightJoinTable = rightSubquery;
         }
         }*/

        if (jNode.getRightResultSet() != null) {

            //for now we only consider that the join operators are base tables or nested joins
            if (jNode.getRightResultSet() instanceof FromSubquery) {
                FromSubquery fs = (FromSubquery) jNode.getRightResultSet();
                SQLQueryVisitor v = new SQLQueryVisitor(rightSubquery);
                fs.getSubquery().accept(v);
                this.query.setRightJoinTableAlias(fs.getCorrelationName());
                //jNode.getLeftResultSet().accept(leftVisitor);
            } else if (jNode.getRightResultSet() instanceof JoinNode) {
                SQLQueryVisitor v = new SQLQueryVisitor(rightSubquery);
                jNode.getRightResultSet().accept(v);
                //rightSubquery.setSelectAll(true);
            } else if (jNode.getRightResultSet() instanceof FromBaseTable) {
                //FromBaseTable bt=(FromBaseTable)jNode.getRightResultSet();
                FromBaseTableVisitor v = new FromBaseTableVisitor(rightSubquery);
                jNode.getRightResultSet().accept(v);
                //rightSubquery.setSelectAll(true);
                rightSubquery.setIsBaseTable(true);
                //rightSubquery.setResultTableName(bt.getCorrelationName());
            }
            /* for now we only consider that the join operators are base tables or nested joins
             else {
             SQLQueryVisitor v=new SQLQueryVisitor(rightSubquery);
             jNode.getRightResultSet().accept(v);
             }*/
            //System.out.println("Table "+query.getResultTableName()+" add right join table: "+rightSubquery.getResultTableName());
            this.query.setRightJoinTable(rightSubquery);

        }
        if (jNode instanceof HalfOuterJoinNode) {
            if (!((HalfOuterJoinNode) jNode).isRightOuterJoin()) {
                this.query.setJoinType("left outer join");
            } else {
                this.query.setJoinType("right outer join");
            }
        } else {
            //we only have joins and left outer joins to add condition to check!
            this.query.setJoinType("join");
        }
        //uNode.accept(visitor);

    }
}
