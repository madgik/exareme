/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.dag.PartitionCols;
import madgik.exareme.master.queryProcessor.decomposer.dag.ResultList;
import madgik.exareme.master.queryProcessor.decomposer.query.*;

import java.util.*;

/**
 * @author dimitris
 */
public class SinlgePlanDFLGenerator {

    private Node root;
    private SinglePlan plan;
    private int partitionNo;
    private Memo memo;

    SinlgePlanDFLGenerator(Node n, SinglePlan p, int partNo, Memo m) {
        this.plan = p;
        this.root = n;
        this.partitionNo = partNo;
        this.memo = m;
    }

    private void createNode(Node oldNode, Node newNode, Iterator<Integer> p,
        Map<Node, Node> correspondingNodes, Set<Node> reusedNodes) {
        // try{
        // if(n.getChildren().isEmpty()){
        // base table
        // return;
        // }
        // System.out.println(oldNode.getChildren().size());
        // System.out.println(oldNode.getChildAt(0).getObject().toString());
        Node o = oldNode.getChildAt(p.next());

        // Table oTable = (Table) n.getObject();
        newNode.setObject(o.getObject());
        newNode.setOperator(o.getOpCode());

        // String next = getStringForNode(o, n.isCentralised(),
        // n.isPartitionedOn(), oTable.getName());
        // r.remove(next);
        // r.add(0, next);
        // System.out.println(o.getObject().toString());
        for (Node e2 : o.getChildren()) {
            Node newChild = new Node(Node.AND);
            if (!correspondingNodes.containsKey(e2)) {
                correspondingNodes.put(e2, newChild);
                newNode.addChild(newChild);
                if (!e2.getChildren().isEmpty()) {
                    createNode(e2, newChild, p, correspondingNodes, reusedNodes);
                } else {
                    newChild.setObject(e2.getObject());
                }
            } else {
                reusedNodes.add(correspondingNodes.get(e2));
                // reuse
                newNode.addChild(correspondingNodes.get(e2));

                // iterate on plan to consume elements that are not needed
                // maybe deal with this during plan generation???
                consumePlanChoices(correspondingNodes.get(e2), p);
            }

        }
        // }catch(java.lang.Exception e){
        // System.out.print("ss, args");
        // }
    }

    private void pushProjections(Projection p, Node n) throws CloneNotSupportedException {

        // Projection p=new Projection();
        // List<Output> projection=p.getOperands();
        // for(Node n:root.getChildren()){
        if (n.getOpCode() == Node.PROJECT) {
            Projection p2 = (Projection) n.getObject();
            // projection.addAll(p2.getOperands());
            List<Output> copy = new ArrayList<Output>();
            for (Output o : p2.getOperands()) {
                copy.add(o.clone());
            }
            pushProjections(new Projection(copy), n.getChildAt(0));
        } else if (n.getOpCode() == Node.JOIN) {
            NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) n.getObject();
            Column leftColumn = nuwc.getLeftOp().getAllColumnRefs().get(0);
            Column rightColumn = nuwc.getRightOp().getAllColumnRefs().get(0);
            if (n.getChildren().size() > 1) {
                List<Output> copyL = new ArrayList<Output>(p.getOperands());
                Projection pLeft = new Projection();
                for (Output o : copyL) {
                    // check if condition comes from this child!
                    // traverse tree to find if table with given alias is
                    // accesed from this child
                    // not efficient(?) but we do it just for the final plan
                    // !! what should we do in the search procedure?
                    for (Column c : o.getObject().getAllColumnRefs()) {
                        if (n.getChildAt(0).isDescendantOfBaseTable(c.tableAlias)) {
                            pLeft.addOperand(o.clone());
                        }
                    }

                }
        /*
         * Column cl=new Column(null, leftColumn.columnName); if
				 * (!pLeft.getAllColumnRefs().contains(cl)) {
				 * pLeft.addOperand(new Output(leftColumn.columnName, cl)); }
				 */
                if (!pLeft.getAllColumnRefs().contains(leftColumn)) {
                    pLeft.addOperand(new Output(leftColumn.columnName, leftColumn.clone()));
                }
                Node prjL = addProjection(n, pLeft, 0);
                pushProjections(pLeft, prjL.getChildAt(0));
                List<Output> copyR = new ArrayList<Output>(p.getOperands());
                Projection pRight = new Projection();
                for (Output o : copyR) {
                    // check if condition comes from this child!
                    // traverse tree to find if table with given alias is
                    // accesed from this child
                    // not efficient(?) but we do it just for the final plan
                    // !! what should we do in the search procedure?
                    for (Column c : o.getObject().getAllColumnRefs()) {
                        if (n.getChildAt(1).isDescendantOfBaseTable(c.tableAlias)) {
                            pRight.addOperand(o.clone());
                        }
                    }

                }
        /*
         * Column cr=new Column(null, rightColumn.columnName); if
				 * (!pRight.getAllColumnRefs().contains(cr)) {
				 * pRight.addOperand(new Output(rightColumn.columnName, cr)); }
				 */
                if (!pRight.getAllColumnRefs().contains(rightColumn)) {
                    pRight.addOperand(new Output(rightColumn.columnName, rightColumn.clone()));
                }
                Node prjR = addProjection(n, pRight, 1);
                pushProjections(pRight, prjR.getChildAt(0));
            } else {
                // self join
                // Column cl=new Column(null, leftColumn.columnName);
                if (!p.getAllColumnRefs().contains(leftColumn)) {
                    p.addOperand(new Output(leftColumn.columnName, leftColumn.clone()));
                }
                // Column cr=new Column(null, rightColumn.columnName);
                if (!p.getAllColumnRefs().contains(rightColumn)) {
                    p.addOperand(new Output(rightColumn.columnName, rightColumn.clone()));
                }
                Node prj = addProjection(n, p, 0);
                List<Output> copy = new ArrayList<Output>(p.getOperands());
                pushProjections(new Projection(copy), prj.getChildAt(0));
            }

        } else if (n.getOpCode() == Node.SELECT) {
            Selection s = (Selection) n.getObject();
            // Column c = new Column(null, uwc.getColumn().columnName);
            for (Column c : s.getAllColumnRefs()) {
                if (!p.getAllColumnRefs().contains(c)) {

                    p.addOperand(new Output(c.columnName, c.clone()));
                }
            }
            Node prj = addProjection(n, p, 0);
            List<Output> copy = new ArrayList<Output>(p.getOperands());
            pushProjections(new Projection(copy), prj.getChildAt(0));
        } else {
            for (Node c : n.getChildren()) {
                List<Output> copy = new ArrayList<Output>(p.getOperands());
                pushProjections(new Projection(copy), c);
            }
        }
        // }
    }

    private void combineOperatorsAndOutputQueries(Node n, ResultList tempResult, SQLQuery q,
        HashMap<Node, SQLQuery> visited) {
        // SQLQuery q=new SQLQuery();
        if (visited.containsKey(n)) {
            tempResult.setLastTableName(visited.get(n));
            return;
        }
        if (n.getOpCode() == Node.PROJECT) {
            Projection p = (Projection) n.getObject();
            q.getOutputs().addAll(p.getOperands());
            visited.put(n, q);
            combineOperatorsAndOutputQueries(n.getChildAt(0), tempResult, q, visited);
        } else if (n.getOpCode() == Node.JOIN) {
            NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) n.getObject();
            q.addBinaryWhereCondition(nuwc);
            List<String> inputNames = new ArrayList<String>();
            for (int i = 0; i < n.getChildren().size(); i++) {
                // for(Column c:nuwc.getAllColumnRefs()){

                if (i == 0) {
                    visited.put(n, q);
                    combineOperatorsAndOutputQueries(n.getChildAt(i), tempResult, q, visited);
                    inputNames.add(tempResult.getLastTableName());
                } else {
                    SQLQuery newq = new SQLQuery();
                    // inputNames.add(newq.getTemporaryTableName());
                    visited.put(n, q);
                    combineOperatorsAndOutputQueries(n.getChildAt(i), tempResult, newq, visited);
                    inputNames.add(tempResult.getLastTableName());
                }
				/*
				 * Table t = new Table(c.tableAlias, null);
				 * 
				 * if (!q.getInputTables().contains(t)) {
				 * q.getInputTables().add(t); if(i==0){
				 * combineOperatorsAndOutputQueries(n.getChildAt(i), tempResult,
				 * q); } else{ combineOperatorsAndOutputQueries(n.getChildAt(i),
				 * tempResult, new SQLQuery()); }
				 * 
				 * }
				 */
            }
            if (!inputNames.get(0).equals(q.getTemporaryTableName())) {
                for (NonUnaryWhereCondition bwc : q.getBinaryWhereConditions()) {
                    // also change the table name in BWCs from previous nodes
                    // (in case more than 1 BWC are in q)
                    for (int i = 0; i < n.getChildren().size(); i++) {
                        // String
                        // tName=tempResult.get(tempResult.size()-2+i).getTemporaryTableName();

                        q.addInputTableIfNotExists(new Table(inputNames.get(i), inputNames.get(i)));
                        for (Operand o : bwc.getOperands()) {
                            List<Column> cs = o.getAllColumnRefs();
                            if (!cs.isEmpty()) {
                                // not constant
                                Column c = cs.get(0);
                                if (n.getChildAt(i).isDescendantOfBaseTable(c.tableAlias)) {
                                    o.changeColumn(c, new Column(inputNames.get(i), c.columnName));
                                }
                            }
                        }
                    }
                }
            }

            for (Output o : q.getOutputs()) {
                for (Column c : o.getObject().getAllColumnRefs()) {
                    {
                        for (int i = 0; i < n.getChildren().size(); i++) {
                            Node child = n.getChildAt(i);
                            if (child.isDescendantOfBaseTable(c.tableAlias)) {
                                o.getObject()
                                    .changeColumn(c, new Column(inputNames.get(i), c.columnName));
                            }
                        }
                    }
                    // o.getObject().changeColumn(c, c);
                }
            }
            tempResult.add(q);
        } else if (n.getOpCode() == Node.REPARTITION) {
            if (q.getPartitionColumn() == null && q.getBinaryWhereConditions().isEmpty()) {
                // if (q.getPartitionColumn() == null){
                Column partition = (Column) n.getObject();
                q.setPartitionColumn(partition);
                visited.put(n, q);

                if (visited.containsKey(n.getChildAt(0))) {
                    // just repartition
                    String tableName = visited.get(n.getChildAt(0)).getTemporaryTableName();
                    q.addInputTableIfNotExists(new Table(tableName, tableName));
                    tempResult.add(q);
                } else {
                    combineOperatorsAndOutputQueries(n.getChildAt(0), tempResult, q, visited);
                }
            } else {
                combineOperatorsAndOutputQueries(n, tempResult, new SQLQuery(), visited);
            }
        } else if (n.getOpCode() == Node.UNION || n.getOpCode() == Node.UNIONALL) {
            List<SQLQuery> unions = new ArrayList<SQLQuery>();
            for (Node c : n.getChildren()) {
                SQLQuery u = new SQLQuery();
                unions.add(u);
                visited.put(n, q);
                combineOperatorsAndOutputQueries(c, tempResult, u, visited);
            }
            if (unions.size() > 1) {
                tempResult.add(q);
                q.setUnionqueries(unions);
                if (n.getOpCode() == Node.UNIONALL) {
                    q.setUnionAll(true);
                }
            }
        } else if (n.getOpCode() == Node.SELECT) {
            visited.put(n, q);
            combineOperatorsAndOutputQueries(n.getChildAt(0), tempResult, q, visited);
            String inputName = tempResult.getLastTableName();

            Selection s = (Selection) n.getObject();
            Iterator<Operand> it = s.getOperands().iterator();
            while (it.hasNext()) {
                Operand o = it.next();
                if (o instanceof UnaryWhereCondition) {
                    UnaryWhereCondition uwc = (UnaryWhereCondition) o;
                    if (!inputName.equals(q.getTemporaryTableName())) {
                        Column c = uwc.getColumn();
                        uwc.changeColumn(c, new Column(inputName, c.columnName));
                    }
                    q.addUnaryWhereCondition(uwc);
                } else {
                    NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o;
                    if (!inputName.equals(q.getTemporaryTableName())) {
                        for (Column c : nuwc.getAllColumnRefs()) {
                            nuwc.changeColumn(c, new Column(inputName, c.columnName));
                        }
                    }
                    q.addBinaryWhereCondition(nuwc);
                }
            }

            for (Output o : q.getOutputs()) {
                for (Column c : o.getObject().getAllColumnRefs()) {
                    {
                        if (n.getChildAt(0).isDescendantOfBaseTable(c.tableAlias)) {
                            o.getObject().changeColumn(c, new Column(inputName, c.columnName));
                        }
                    }
                    o.getObject().changeColumn(c, c);
                }
            }
            // tempResult.add(q);
        } else {
            for (Node c : n.getChildren()) {
                visited.put(n, q);
                combineOperatorsAndOutputQueries(c, tempResult, new SQLQuery(), visited);
                tempResult.add(q);
            }
            if (n.getChildren().isEmpty() && n.getObject() instanceof Table) {
                q.getInputTables().add((Table) n.getObject());
                tempResult.add(q);
            }
        }
    }

    private void gen(Node n, List<String> r, Iterator<Integer> p) {
        // try{
        // if(n.getChildren().isEmpty()){
        // base table
        // return;
        // }
        Node o = n.getChildAt(p.next());
        Table oTable = (Table) n.getObject();
        String next =
            getStringForNode(o, n.isCentralised(), n.getFirstPartitionedSet(), oTable.getName());
        r.remove(next);
        r.add(0, next);

        // System.out.println(o.getObject().toString());
        for (Node e2 : o.getChildren()) {
            if (!e2.getChildren().isEmpty()) {
                gen(e2, r, p);
            }
        }
        // }catch(java.lang.Exception e){
        // System.out.print("ss, args");
        // }
    }

    private String getStringForNode(Node o, boolean centralized, PartitionCols p, String name) {
        int pNo = 1;
        if (!centralized) {
            pNo = partitionNo;
        }
        // String pName=p.get(0).columnName;
        String pName = "0";
        if (!p.isEmpty()) {
            pName = p.getFirstCol().columnName;
        }
        if (o.getOpCode() == Node.PROJECT) {
            Table t = (Table) o.getChildAt(0).getObject();
            Projection prj = (Projection) o.getObject();
            List<Column> prjCols = prj.getAllColumnRefs();
            // List<Column> renamed=new ArrayList<Column>();
            for (Column c : prjCols) {
                prj.changeColumn(c, new Column(t.getName(), c.columnName));
                // c.tableAlias=t.getName();
                // renamed.add(new Column(c.columnName, t.getName()));
            }

            return "distributed create table " + name + " to " + pNo + " on " + t.getName() + "."
                + pName + " as select " + o.getObject().toString() + " from " + t.getName() + ";";
        } else if (o.getOpCode() == Node.SELECT) {
            Table t = (Table) o.getChildAt(0).getObject();
            return "distributed create table " + name + " to " + pNo + " on " + t.getName() + "."
                + pName + " as select * from " + t.getName() + " where " + o.getObject().toString()
                + ";";
        } else if (o.getOpCode() == Node.JOIN) {
            Table t1 = (Table) o.getChildAt(0).getObject();
            String t2name = "";
            Table t2 = new Table();
            if (o.getChildren().size() > 1) {
                t2 = (Table) o.getChildAt(1).getObject();
                t2name = ", " + t2.getName();
            }
            NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o.getObject();
            List<Column> leftCols = nuwc.getLeftOp().getAllColumnRefs();
            for (Column c : leftCols) {
                nuwc.getLeftOp().changeColumn(c, new Column(t1.getName(), c.columnName));
                // c.tableAlias=t1.getName();
                // renamed.add(new Column(c.columnName, t.getName()));
            }
            List<Column> rightCols = nuwc.getRightOp().getAllColumnRefs();
            if (!t2.getName().equals("")) {
                for (Column c : rightCols) {
                    nuwc.getRightOp().changeColumn(c, new Column(t2.getName(), c.columnName));
                    // c.tableAlias=t2.getName();
                    // renamed.add(new Column(c.columnName, t.getName()));
                }
            } else {
                // one table
                for (Column c : rightCols) {
                    nuwc.getRightOp().changeColumn(c, new Column(t1.getName(), c.columnName));
                    // c.tableAlias=t2.getName();
                    // renamed.add(new Column(c.columnName, t.getName()));
                }
            }
            return "distributed create table " + name + " to " + pNo + " on " + pName
                + " as direct select * from " + t1.getName() + t2name + " where " + o.getObject()
                .toString() + ";";
        } else if (o.getOpCode() == Node.UNION || o.getOpCode() == Node.UNIONALL) {

            StringBuilder sb = new StringBuilder();
            sb.append(" as select * from ");
            Table t = (Table) o.getChildAt(0).getObject();
            sb.append(t.getName());
            for (int i = 1; i < o.getChildren().size(); i++) {
                if (o.getOpCode() == Node.UNION) {
                    sb.append(" UNION ");
                } else {
                    sb.append(" UNION ALL ");
                }
                sb.append(" select * from ");
                sb.append(((Table) o.getChildAt(i).getObject()).getName());
            }

            return "distributed create table " + name + " to " + pNo + " on " + t.getName() + "."
                + pName + sb.toString() + ";";
        }
        if (o.getOpCode() == Node.REPARTITION) {
            Table t = (Table) o.getChildAt(0).getObject();
            return "distributed create table " + name + " to " + pNo + " on " + t.getName() + "."
                + pName + " as select * from " + t.getName() + ";";
        } else {
            return "";
        }
    }

    private Node addProjection(Node n, Projection p, int childNo) {
        // pushes the projection below n, before the child with no childNo
        // if this child is repartition, it pushes the projection one more level
        // down
        Node prj = new Node(Node.AND, Node.PROJECT);
        prj.setObject(new Projection(p.getOperands()));
        Node c = n.getChildAt(childNo);
        if (c.getOpCode() == Node.REPARTITION) {
            Node g = c.getChildAt(0);
            prj.addChild(g);
            c.removeChildAt(0);
            c.addChildAt(prj, 0);
        } else {
            prj.addChild(n.getChildAt(childNo));
            n.removeChildAt(childNo);
            n.addChildAt(prj, childNo);
        }
        return prj;
    }

	/*
	 * public static boolean baseTableIsDescendantOfNode(Node n, String
	 * tableAlias) { // move to utils?? if (n.getObject() instanceof Table) {
	 * Table t = (Table) n.getObject(); if (t.getAlias() != null &&
	 * t.getAlias().equals(tableAlias)) { return true; } } for (Node c :
	 * n.getChildren()) { if (baseTableIsDescendantOfNode(c, tableAlias)) {
	 * return true; } } return false; }
	 */

    private void consumePlanChoices(Node n, Iterator<Integer> p) {
        if (!n.getChildren().isEmpty()) {
            p.next();
        }
        for (Node n2 : n.getChildren()) {
            consumePlanChoices(n2, p);
        }
    }

    public ResultList generate2() {
        ResultList qs = new ResultList();
        MemoKey rootkey = new MemoKey(root, null);

        // printPlan(rootkey);
        qs.setCurrent(new SQLQuery());
        if (partitionNo == 1) {
            combineOperatorsAndOutputQueries3(rootkey, qs, new HashMap<MemoKey, SQLQuery>());
        } else {
            combineOperatorsAndOutputQueries2(rootkey, qs, new HashMap<MemoKey, SQLQuery>());
        }

        // for (SQLQuery q : qs) {
        // System.out.println(q.toDistSQL() + "\n");
        // }
        for (int i = 0; i < qs.size(); i++) {
            SQLQuery q = qs.get(i);
            if (q.getOutputs().isEmpty()) {
                for (Table t : q.getInputTables()) {
                    for (int j = 0; j < i; j++) {
                        SQLQuery q2 = qs.get(j);
                        if (q2.getTemporaryTableName().equals(t.getName())) {
                            for (Output o2 : q2.getOutputs()) {
                                q.addOutput(t.getAlias(), o2.getOutputName());
                            }
                            break;
                        }
                    }
                }
            }
        }
        return qs;
    }

    private void combineOperatorsAndOutputQueries2(MemoKey k, ResultList tempResult,
        HashMap<MemoKey, SQLQuery> visited) {

        // SQLQuery q=new SQLQuery();
        if (visited.containsKey(k) && visited.get(k).isMaterialised()) {
            tempResult.setLastTableName(visited.get(k));
            return;
        }
        SQLQuery current = tempResult.getCurrent();
        MemoValue v = memo.getMemoValue(k);

        // if(v.isMaterialised()){
        // }
        SinglePlan p = v.getPlan();
        Node e = k.getNode();
        if (!e.getObject().toString().startsWith("table")) {
            Table t = (Table) k.getNode().getObject();
            tempResult.setLastTableName(t.getlocalName());
            visited.put(k, current);
            if (memo.getMemoValue(k).isMaterialised()) {
                current.setMaterialised(true);
            }
            return;
        }

        // Iterator<Integer> pIt = p.getPathIterator();
        // while (pIt.hasNext()) {
        // int i = pIt.next();
        Column repBefore = p.getRepartitionBeforeOp();
        if (repBefore != null) {
            if (current.getPartitionColumn() == null) {
                // if (q.getPartitionColumn() == null){

                current.setPartition(repBefore, partitionNo);
                // visited.put(n, q);
            } else {
                tempResult.add(current);
                SQLQuery q2 = new SQLQuery();
                current = q2;
                tempResult.setCurrent(q2);

                current.setPartition(repBefore, partitionNo);
                // combineOperatorsAndOutputQueries2(n, tempResult, q2, visited,
                // p.getInputPlan());
            }
        }
        Node op = e.getChildAt(p.getChoice());

        if (op.getOpCode() == Node.PROJECT) {

            Column repAfter = p.getRepartitionAfterOp(0);
            // boolean called=false;
            if (repAfter != null && !visited.containsKey(p.getInputPlan(0))) {
                String inputName;
                if (visited.containsKey(p.getInputPlan(0))) {
                    String n = visited.get(p.getInputPlan(0)).getTemporaryTableName();
                    inputName = n;
                    current.addInputTableIfNotExists(new Table(n, n));
                } else {
                    Projection prj = (Projection) op.getObject();
                    SQLQuery q2 = new SQLQuery();
                    // current = q2;
                    q2.setPartition(repAfter, partitionNo);
                    tempResult.setCurrent(q2);
                    q2.setOutputColumnsDinstict(prj.isDistinct());
                    // visited.put(op, q2);

                    q2.getOutputs().addAll(prj.getOperands());
                    combineOperatorsAndOutputQueries2(p.getInputPlan(0), tempResult, visited);
                    // visited.put(p.getInputPlan(j), q2);
                    tempResult.add(q2);
                    inputName = tempResult.getLastTableName();
                    current.addInputTableIfNotExists(new Table(inputName, inputName));
                    tempResult.setCurrent(current);
                    visited.put(k, q2);
                    if (memo.getMemoValue(k).isMaterialised()) {
                        q2.setMaterialised(true);
                    }
                }
                for (Output o : current.getOutputs()) {
                    for (Column c : o.getObject().getAllColumnRefs()) {
                        {
                            // if (baseTableIsDescendantOfNode(op.getChildAt(0),
                            // c.tableAlias)) {
                            o.getObject().changeColumn(c, new Column(inputName, c.columnName));
                            // }
                        }
                        // o.getObject().changeColumn(c, c);
                    }
                }

				/*
				 * for (Output o : current.getOutputs()) { for (Column c :
				 * o.getObject().getAllColumnRefs()) { { Node child =
				 * op.getChildAt(0); if (baseTableIsDescendantOfNode(child,
				 * c.tableAlias)) { o.getObject().changeColumn(c, new
				 * Column(op.getChildAt(0), c.columnName));
				 * 
				 * } } //o.getObject().changeColumn(c, c); } }
				 */
                // visited.put(n, q);
                // int nextChoice=plan.getNextChoice();
                // if (v.isMaterialised()) {
                // } else {
            } else {
                if (!current.getOutputs().isEmpty() && !op.getChildAt(0).getObject().toString()
                    .startsWith("table")) {
					/*
					 * Table t=(Table)op.getChildAt(0) .getObject(); for (Output
					 * o : current.getOutputs()) { for (Column c :
					 * o.getObject().getAllColumnRefs()) { {
					 * 
					 * o.getObject() .changeColumn( c, new Column(t.getAlias(),
					 * c.columnName));
					 * 
					 * } //o.getObject().changeColumn(c, c); } }
					 */

                } else {
                    // ignore refCols if this already has a projection

                    Projection prj = (Projection) op.getObject();
                    current.getOutputs().addAll(prj.getOperands());
                    current.setOutputColumnsDinstict(prj.isDistinct());
                    visited.put(k, current);
                    if (memo.getMemoValue(k).isMaterialised()) {
                        current.setMaterialised(true);
                    }
					/*
					 * for (Output o : current.getOutputs()) { for (Column c :
					 * o.getObject().getAllColumnRefs()) { { Node child =
					 * op.getChildAt(0); if (baseTableIsDescendantOfNode(child,
					 * c.tableAlias)) { o.getObject().changeColumn(c, new
					 * Column(op.getChildAt(0), c.columnName));
					 * 
					 * } } //o.getObject().changeColumn(c, c); } }
					 */
                    // visited.put(n, q);
                    // int nextChoice=plan.getNextChoice();
                    // if (v.isMaterialised()) {

                    // } else {
                }

				/*
				 * if(op.getChildAt(0)
				 * .getObject().toString().startsWith("table")){ SQLQuery q2 =
				 * new SQLQuery(); // current = q2; tempResult.setCurrent(q2); }
				 */
                combineOperatorsAndOutputQueries2(p.getInputPlan(0), tempResult, visited);
                String inputName = tempResult.getLastTableName();
                current.addInputTableIfNotExists(new Table(inputName, inputName));
                for (Output o : current.getOutputs()) {
                    for (Column c : o.getObject().getAllColumnRefs()) {
                        {
                            // if (baseTableIsDescendantOfNode(
                            // op.getChildAt(0), c.tableAlias)) {
                            if (op.getChildAt(0).isDescendantOfBaseTable(c.tableAlias)) {
                                o.getObject().changeColumn(c, new Column(inputName, c.columnName));
                            }
                            // }
                        }
                        // o.getObject().changeColumn(c, c);
                    }
                }

            }
        } else if (op.getOpCode() == Node.JOIN) {
            NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) op.getObject();
            NonUnaryWhereCondition nuwcCloned =
                new NonUnaryWhereCondition(nuwc.getLeftOp(), nuwc.getRightOp(), nuwc.getOperator());
            current.addBinaryWhereCondition(nuwcCloned);
            List<String> inputNames = new ArrayList<String>();
            for (int j = 0; j < op.getChildren().size(); j++) {
                // for(Column c:nuwc.getAllColumnRefs()){

                // if (j == 0) {
                Column repAfter = p.getRepartitionAfterOp(j);
                Column repBeforeChild =
                    memo.getMemoValue(p.getInputPlan(j)).getPlan().getRepartitionBeforeOp();
                // boolean called=false;
                if ((repAfter != null || repBeforeChild != null) && !visited
                    .containsKey(p.getInputPlan(j))) {
                    // if (current.getPartitionColumn() == null &&
                    // q.getBinaryWhereConditions().isEmpty()) {
                    // if (q.getPartitionColumn() == null){

                    // current.setPartitionColumn(repAfter);
                    // visited.put(n, q);
                    // } else {
                    // called=true;
                    // tempResult.add(current);
                    if (visited.containsKey(p.getInputPlan(j))) {
                        String n = visited.get(p.getInputPlan(j)).getTemporaryTableName();
                        inputNames.add(n);
                        current.addInputTableIfNotExists(new Table(n, n));
                        tempResult.setLastTableName(n);
                    } else {
                        SQLQuery q2 = new SQLQuery();
                        // current = q2;
                        if (repAfter != null) {
                            q2.setPartition(repAfter, partitionNo);
                        }
                        tempResult.setCurrent(q2);
                        // visited.put(op, q2);

                        combineOperatorsAndOutputQueries2(p.getInputPlan(j), tempResult, visited);
                        // visited.put(p.getInputPlan(j), q2);
                        q2.addInputTableIfNotExists(new Table(tempResult.getLastTableName(),
                            tempResult.getLastTableName()));
                        tempResult.add(q2);
                        inputNames.add(tempResult.getLastTableName());
                        current.addInputTableIfNotExists(new Table(tempResult.getLastTableName(),
                            tempResult.getLastTableName()));
                        tempResult.setCurrent(current);
                    }
                    // combineOperatorsAndOutputQueries2(n, tempResult, q2,
                    // visited, p.getInputPlan());
                    // }
                } else {
                    // visited.put(op, current);

                    combineOperatorsAndOutputQueries2(p.getInputPlan(j), tempResult, visited);
                    // current=tempResult.getCurrent();
                    // visited.put(p.getInputPlan(j), current);
                    inputNames.add(tempResult.getLastTableName());
                    current.addInputTableIfNotExists(
                        new Table(tempResult.getLastTableName(), tempResult.getLastTableName()));
                } // } else {
                // SQLQuery newq = new SQLQuery();

                // inputNames.add(newq.getTemporaryTableName());
                // visited.put(op, current);
                // combineOperatorsAndOutputQueries2(p.getInputPlan(j),
                // tempResult, newq, visited);
                // inputNames.add(tempResult.getLastTableName());
                // }
            }
            visited.put(k, current);
            if (memo.getMemoValue(k).isMaterialised()) {
                current.setMaterialised(true);
            }

			/*
			 * Operand left=nuwc.getLeftOp(); for(Column
			 * c:left.getAllColumnRefs()){ left.changeColumn(c, new
			 * Column(inputNames.get(0), c.columnName)); } Operand
			 * right=nuwc.getRightOp(); for(Column c:right.getAllColumnRefs()){
			 * right.changeColumn(c, new Column(inputNames.get(1),
			 * c.columnName)); }
			 */
            // if (!inputNames.get(0).equals(current.getTemporaryTableName())) {
            for (NonUnaryWhereCondition bwc : current.getBinaryWhereConditions()) {
                // also change the table name in BWCs from previous nodes (in
                // case more than 1 BWC are in q)

                for (int j = 0; j < op.getChildren().size(); j++) {
                    for (Operand o : bwc.getOperands()) {

                        List<Column> cs = o.getAllColumnRefs();
                        if (!cs.isEmpty()) {
                            // not constant
                            Column c = cs.get(0);
                            if (op.getChildAt(j).isDescendantOfBaseTable(c.tableAlias)) {
                                bwc.changeColumn(c, new Column(inputNames.get(j), c.columnName));
                            }
                        }

                    }
                }
            }
            // }

            for (Output o : current.getOutputs()) {
                for (Column c : o.getObject().getAllColumnRefs()) {
                    {
                        for (int j = 0; j < op.getChildren().size(); j++) {
                            Node child = op.getChildAt(j);
                            if (child.isDescendantOfBaseTable(c.tableAlias)) {
                                o.getObject()
                                    .changeColumn(c, new Column(inputNames.get(j), c.columnName));
                            }
                        }
                    }
                    // o.getObject().changeColumn(c, c);
                }
            }
            // tempResult.add(current);
        } else if (op.getOpCode() == Node.UNION || op.getOpCode() == Node.UNIONALL) {
            List<SQLQuery> unions = new ArrayList<SQLQuery>();
            // for (Node c : op.getChildren()) {
            for (int l = 0; l < op.getChildren().size(); l++) {
                SQLQuery u = new SQLQuery();
                unions.add(u);
                // visited.put(op, current);
                tempResult.setCurrent(u);
                combineOperatorsAndOutputQueries2(p.getInputPlan(l), tempResult, visited);
                // visited.put(p.getInputPlan(l), u);
                tempResult.add(u);
            }
            if (unions.size() > 1) {
                tempResult.add(current);
                current.setUnionqueries(unions);
                if (op.getOpCode() == Node.UNIONALL) {
                    current.setUnionAll(true);
                }
                visited.put(k, current);
                if (memo.getMemoValue(k).isMaterialised()) {
                    current.setMaterialised(true);
                }
            } else {
                visited.put(k, unions.get(0));
                if (memo.getMemoValue(k).isMaterialised()) {
                    unions.get(0).setMaterialised(true);
                }
            }
        } else if (op.getOpCode() == Node.SELECT) {
            Column repAfter = p.getRepartitionAfterOp(0);
            Column repBeforeChild =
                memo.getMemoValue(p.getInputPlan(0)).getPlan().getRepartitionBeforeOp();
            if ((repAfter != null || repBeforeChild != null) && !visited
                .containsKey(p.getInputPlan(0))) {
                // Selection sel = (Selection) op.getObject();

                String inputName;
                if (visited.containsKey(p.getInputPlan(0))) {
                    String n = visited.get(p.getInputPlan(0)).getTemporaryTableName();
                    inputName = n;
                    current.addInputTableIfNotExists(new Table(n, n));
                } else {

                    SQLQuery q2 = new SQLQuery();
                    // current = q2;
                    if (repAfter != null) {
                        q2.setPartition(repAfter, partitionNo);
                    } // else{
                    // q2.setPartitionColumn(repBeforeChild);
                    // }

                    tempResult.setCurrent(q2);
                    // visited.put(op, q2);
                    // q2.get.getOutputs().addAll(sel.getOperands());
                    combineOperatorsAndOutputQueries2(p.getInputPlan(0), tempResult, visited);
                    // visited.put(p.getInputPlan(j), q2);
                    q2.addInputTableIfNotExists(
                        new Table(tempResult.getLastTableName(), tempResult.getLastTableName()));
                    inputName = tempResult.getLastTableName();
                    tempResult.add(q2);

                    current.addInputTableIfNotExists(
                        new Table(tempResult.getLastTableName(), tempResult.getLastTableName()));
                    tempResult.setCurrent(current);
                    Selection s = (Selection) op.getObject();
                    Iterator<Operand> it = s.getOperands().iterator();
                    while (it.hasNext()) {
                        Operand o = it.next();
                        if (o instanceof UnaryWhereCondition) {
                            UnaryWhereCondition uwc = (UnaryWhereCondition) o;
                            UnaryWhereCondition uwcCloned =
                                new UnaryWhereCondition(uwc.getType(), uwc.getColumn(),
                                    uwc.getNot());
                            if (!inputName.equals(q2.getTemporaryTableName())) {
                                Column c = uwcCloned.getColumn();
                                uwcCloned.changeColumn(c, new Column(inputName, c.columnName));
                            }
                            q2.addUnaryWhereCondition(uwcCloned);
                        } else {
                            NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o;
                            NonUnaryWhereCondition nuwcCloned =
                                new NonUnaryWhereCondition(nuwc.getLeftOp(), nuwc.getRightOp(),
                                    nuwc.getOperator());
                            if (!inputName.equals(q2.getTemporaryTableName())) {
                                for (Column c : nuwcCloned.getAllColumnRefs()) {
                                    nuwcCloned.changeColumn(c, new Column(inputName, c.columnName));
                                }
                            }
                            q2.addBinaryWhereCondition(nuwcCloned);
                        }
                    }
                    visited.put(k, q2);
                    if (memo.getMemoValue(k).isMaterialised()) {
                        current.setMaterialised(true);
                    }
                    for (Output o : q2.getOutputs()) {
                        for (Column c : o.getObject().getAllColumnRefs()) {
                            {
                                if (op.isDescendantOfBaseTable(c.tableAlias)) {
                                    if (op.getChildAt(0).isDescendantOfBaseTable(c.tableAlias)) {
                                        o.getObject()
                                            .changeColumn(c, new Column(inputName, c.columnName));
                                    }
                                }
                            }
                            o.getObject().changeColumn(c, c);
                        }
                    }

                }

                // visited.put(n, q);
                // int nextChoice=plan.getNextChoice();
                // if (v.isMaterialised()) {
                // } else {
            } else {
                // visited.put(op, current);

                combineOperatorsAndOutputQueries2(p.getInputPlan(0), tempResult, visited);
                visited.put(k, current);
                if (memo.getMemoValue(k).isMaterialised()) {
                    current.setMaterialised(true);
                }
                String inputName = tempResult.getLastTableName();

                Selection s = (Selection) op.getObject();
                Iterator<Operand> it = s.getOperands().iterator();
                while (it.hasNext()) {
                    Operand o = it.next();
                    if (o instanceof UnaryWhereCondition) {
                        UnaryWhereCondition uwc = (UnaryWhereCondition) o;
                        UnaryWhereCondition uwcCloned =
                            new UnaryWhereCondition(uwc.getType(), uwc.getColumn(), uwc.getNot());
                        if (!inputName.equals(current.getTemporaryTableName())) {
                            Column c = uwcCloned.getColumn();
                            uwcCloned.changeColumn(c, new Column(inputName, c.columnName));
                        }
                        current.addUnaryWhereCondition(uwcCloned);
                    } else {
                        NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o;
                        NonUnaryWhereCondition nuwcCloned =
                            new NonUnaryWhereCondition(nuwc.getLeftOp(), nuwc.getRightOp(),
                                nuwc.getOperator());
                        if (!inputName.equals(current.getTemporaryTableName())) {
                            for (Column c : nuwcCloned.getAllColumnRefs()) {
                                nuwcCloned.changeColumn(c, new Column(inputName, c.columnName));
                            }
                        }
                        current.addBinaryWhereCondition(nuwcCloned);
                    }
                }

                for (Output o : current.getOutputs()) {
                    for (Column c : o.getObject().getAllColumnRefs()) {
                        {
                            if (op.isDescendantOfBaseTable(c.tableAlias)) {
                                // if
                                // (baseTableIsDescendantOfNode(op.getChildAt(0),
                                // c.tableAlias)) {

                                o.getObject().changeColumn(c, new Column(inputName, c.columnName));
                            }
                        }
                        // }
                        o.getObject().changeColumn(c, c);
                    }
                }
                // tempResult.add(q);
            }
        } else {
            for (int l = 0; l < op.getChildren().size(); l++) {
                // visited.put(op, current);
                tempResult.setCurrent(new SQLQuery());
                combineOperatorsAndOutputQueries2(p.getInputPlan(l), tempResult, visited);
                SQLQuery q3 = new SQLQuery();
                visited.put(p.getInputPlan(l), q3);
                if (memo.getMemoValue(p.getInputPlan(l)).isMaterialised()) {
                    q3.setMaterialised(true);
                }
                tempResult.add(current);
            }
            if (op.getChildren().isEmpty() && op.getObject() instanceof Table) {
                current.getInputTables().add((Table) op.getObject());
                tempResult.add(current);
            }
        }

		/*
		 * if (visited.containsKey(o)) { //just repartition String tableName =
		 * visited.get(o).getTemporaryTableName();
		 * current.addInputTableIfNotExists(new Table(tableName, tableName));
		 * tempResult.add(current); } else {
		 * combineOperatorsAndOutputQueries2(k, tempResult, q, visited); }
		 */

		/*
		 * if (visited.containsKey(op)) {
		 * tempResult.setLastTableName(visited.get(op)); return; }
		 */
    }

    private void printPlan(MemoKey key) {
        if (this.memo.containsMemoKey(key)) {
            System.out.println("memo key: " + key);

            System.out.println("mater::" + memo.getMemoValue(key).isMaterialised());
            // System.out.println("plan: " +
            // memo.getMemoValue(key).getPlan().getPath().toString());
            System.out.println("reps: ");
            // while (true) {
            Column r = null;
            try {
                r = memo.getMemoValue(key).getPlan().getRepartitionBeforeOp();
            } catch (java.lang.IndexOutOfBoundsException f) {
            }
            // break;
            // }
            System.out.println("repBef:" + r);
            for (int i = 0; i < 2; i++) {
                System.out.println(
                    "repAfter:" + memo.getMemoValue(key).getPlan().getRepartitionAfterOp(i));
            }
            for (int i = 0; i < 99999999; i++) {
                MemoKey p2;
                try {
                    p2 = memo.getMemoValue(key).getPlan().getInputPlan(i);
                } catch (java.lang.IndexOutOfBoundsException f) {
                    break;
                }
                printPlan(p2);

            }
        } else {
            System.out.println("not contains: " + key.toString());
        }

    }

    private void combineOperatorsAndOutputQueries3(MemoKey k, ResultList tempResult,
        HashMap<MemoKey, SQLQuery> visited) {

        // SQLQuery q=new SQLQuery();
        if (visited.containsKey(k) && visited.get(k).isMaterialised()) {
            tempResult.setLastTableName(visited.get(k));
            return;
        }
        SQLQuery current = tempResult.getCurrent();
        MemoValue v = memo.getMemoValue(k);

        // if(v.isMaterialised()){
        // }
        SinglePlan p = v.getPlan();
        Node e = k.getNode();
        if (!e.getObject().toString().startsWith("table")) {
            Table t = (Table) k.getNode().getObject();
            tempResult.setLastTableName(t.getlocalName());
            visited.put(k, current);
            if (memo.getMemoValue(k).isMaterialised()) {
                current.setMaterialised(true);
            }
            return;
        }

        Node op = e.getChildAt(p.getChoice());

        if (op.getOpCode() == Node.PROJECT) {


            if (!current.getOutputs().isEmpty() && !op.getChildAt(0).getObject().toString()
                .startsWith("table")) {

            } else {

                // ignore refCols if this already has a projection

                Projection prj = (Projection) op.getObject();
                current.getOutputs().addAll(prj.getOperands());
                current.setOutputColumnsDinstict(prj.isDistinct());
                visited.put(k, current);
                if (memo.getMemoValue(k).isMaterialised()) {
                    current.setMaterialised(true);
                }
				/*
				 * for (Output o : current.getOutputs()) { for (Column c :
				 * o.getObject().getAllColumnRefs()) { { Node child =
				 * op.getChildAt(0); if (baseTableIsDescendantOfNode(child,
				 * c.tableAlias)) { o.getObject().changeColumn(c, new
				 * Column(op.getChildAt(0), c.columnName));
				 * 
				 * } } //o.getObject().changeColumn(c, c); } }
				 */
                // visited.put(n, q);
                // int nextChoice=plan.getNextChoice();
                // if (v.isMaterialised()) {

                // } else {
            }

			/*
			 * if(op.getChildAt(0) .getObject().toString().startsWith("table")){
			 * SQLQuery q2 = new SQLQuery(); // current = q2;
			 * tempResult.setCurrent(q2); }
			 */
            CentralizedMemoValue cmv = (CentralizedMemoValue) memo.getMemoValue(p.getInputPlan(0));
            String inputName = "";
            if (cmv.isMaterialised() && op.getChildAt(0).getObject().toString()
                .startsWith("table")) {
                if (visited.containsKey(p.getInputPlan(0))) {
                    String n = visited.get(p.getInputPlan(0)).getTemporaryTableName();
                    inputName = n;
                    current.addInputTableIfNotExists(new Table(n, n));
                    tempResult.setLastTableName(n);
                } else {
                    SQLQuery q2 = new SQLQuery();
                    // current = q2;

                    tempResult.setCurrent(q2);
                    // visited.put(op, q2);

                    combineOperatorsAndOutputQueries3(p.getInputPlan(0), tempResult, visited);

                    // visited.put(p.getInputPlan(j), q2);
                    q2.addInputTableIfNotExists(
                        new Table(tempResult.getLastTableName(), tempResult.getLastTableName()));
                    tempResult.add(q2);
                    inputName = tempResult.getLastTableName();
                    current.addInputTableIfNotExists(
                        new Table(tempResult.getLastTableName(), tempResult.getLastTableName()));
                    tempResult.setCurrent(current);
                }
            } else {

                combineOperatorsAndOutputQueries3(p.getInputPlan(0), tempResult, visited);
                // current=tempResult.getCurrent();
                // visited.put(p.getInputPlan(j), current);
                inputName = tempResult.getLastTableName();
                current.addInputTableIfNotExists(
                    new Table(tempResult.getLastTableName(), tempResult.getLastTableName()));
            }


            //combineOperatorsAndOutputQueries3(p.getInputPlan(0), tempResult, visited);
            //String inputName = tempResult.getLastTableName();
            //current.addInputTableIfNotExists(new Table(inputName, inputName));
            for (Output o : current.getOutputs()) {
                for (Column c : o.getObject().getAllColumnRefs()) {
                    {
                        // if (baseTableIsDescendantOfNode(
                        // op.getChildAt(0), c.tableAlias)) {
                        if (op.getChildAt(0).isDescendantOfBaseTable(c.tableAlias)) {
                            o.getObject().changeColumn(c, new Column(inputName, c.columnName));
                        } // }
                    }
                    // o.getObject().changeColumn(c, c);
                }
            }

        } else if (op.getOpCode() == Node.JOIN) {
            NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) op.getObject();
            NonUnaryWhereCondition nuwcCloned =
                new NonUnaryWhereCondition(nuwc.getLeftOp(), nuwc.getRightOp(), nuwc.getOperator());
            current.addBinaryWhereCondition(nuwcCloned);
            List<String> inputNames = new ArrayList<String>();

            for (int j = 0; j < op.getChildren().size(); j++) {
                // for(Column c:nuwc.getAllColumnRefs()){

                // if (j == 0) {
                CentralizedMemoValue cmv =
                    (CentralizedMemoValue) memo.getMemoValue(p.getInputPlan(j));
                if (cmv.isMaterialised()) {
                    if (visited.containsKey(p.getInputPlan(j))) {
                        String n = visited.get(p.getInputPlan(j)).getTemporaryTableName();
                        inputNames.add(n);
                        current.addInputTableIfNotExists(new Table(n, n));
                        tempResult.setLastTableName(n);
                    } else {
                        SQLQuery q2 = new SQLQuery();
                        // current = q2;

                        tempResult.setCurrent(q2);
                        // visited.put(op, q2);

                        combineOperatorsAndOutputQueries3(p.getInputPlan(j), tempResult, visited);

                        // visited.put(p.getInputPlan(j), q2);
                        q2.addInputTableIfNotExists(new Table(tempResult.getLastTableName(),
                            tempResult.getLastTableName()));
                        tempResult.add(q2);
                        inputNames.add(tempResult.getLastTableName());
                        current.addInputTableIfNotExists(new Table(tempResult.getLastTableName(),
                            tempResult.getLastTableName()));
                        tempResult.setCurrent(current);
                    }
                } else {

                    combineOperatorsAndOutputQueries3(p.getInputPlan(j), tempResult, visited);
                    // current=tempResult.getCurrent();
                    // visited.put(p.getInputPlan(j), current);
                    inputNames.add(tempResult.getLastTableName());
                    current.addInputTableIfNotExists(
                        new Table(tempResult.getLastTableName(), tempResult.getLastTableName()));
                }

            }
            visited.put(k, current);
            if (memo.getMemoValue(k).isMaterialised()) {
                current.setMaterialised(true);
            }
            // if (!inputNames.get(0).equals(current.getTemporaryTableName())) {
            for (NonUnaryWhereCondition bwc : current.getBinaryWhereConditions()) {
                // also change the table name in BWCs from previous nodes (in
                // case more than 1 BWC are in q)
                for (int j = 0; j < op.getChildren().size(); j++) {
                    // String
                    // tName=tempResult.get(tempResult.size()-2+i).getTemporaryTableName();

                    // current.addInputTableIfNotExists(new
                    // Table(inputNames.get(j), inputNames.get(j)));
                    for (Operand o : bwc.getOperands()) {

                        List<Column> cs = o.getAllColumnRefs();
                        if (!cs.isEmpty()) {
                            // not constant
                            Column c = cs.get(0);
                            if (op.getChildAt(j).isDescendantOfBaseTable(c.tableAlias)) {
                                bwc.changeColumn(c, new Column(inputNames.get(j), c.columnName));
                            }
                        }

                    }
                }
            }
            // }

            for (Output o : current.getOutputs()) {
                for (Column c : o.getObject().getAllColumnRefs()) {
                    {
                        for (int j = 0; j < op.getChildren().size(); j++) {
                            Node child = op.getChildAt(j);
                            if (child.isDescendantOfBaseTable(c.tableAlias)) {
                                o.getObject()
                                    .changeColumn(c, new Column(inputNames.get(j), c.columnName));
                            }
                        }
                    }
                    // o.getObject().changeColumn(c, c);
                }
            }
            // tempResult.add(current);
        } else if (op.getOpCode() == Node.UNION || op.getOpCode() == Node.UNIONALL) {
            List<SQLQuery> unions = new ArrayList<SQLQuery>();
            // for (Node c : op.getChildren()) {
            for (int l = 0; l < op.getChildren().size(); l++) {
                SQLQuery u = new SQLQuery();
                unions.add(u);
                // visited.put(op, current);
                tempResult.setCurrent(u);
                combineOperatorsAndOutputQueries3(p.getInputPlan(l), tempResult, visited);
                // visited.put(p.getInputPlan(l), u);
                tempResult.add(u);
            }
            if (unions.size() > 1) {
                tempResult.add(current);
                current.setUnionqueries(unions);
                if (op.getOpCode() == Node.UNIONALL) {
                    current.setUnionAll(true);
                } else {
                    // "push" distinct if qs are federated
                    for (SQLQuery sql : unions) {
                        if (sql.isFederated()) {
                            sql.setOutputColumnsDistinct(true);
                        }
                    }
                }
                visited.put(k, current);
                if (memo.getMemoValue(k).isMaterialised()) {
                    current.setMaterialised(true);
                }
            } else {
                visited.put(k, unions.get(0));
                if (memo.getMemoValue(k).isMaterialised()) {
                    unions.get(0).setMaterialised(true);
                }
            }
        } else if (op.getOpCode() == Node.SELECT) {

            // visited.put(op, current);
            combineOperatorsAndOutputQueries3(p.getInputPlan(0), tempResult, visited);
            visited.put(k, current);
            if (memo.getMemoValue(k).isMaterialised()) {
                current.setMaterialised(true);
            }
            String inputName = tempResult.getLastTableName();

            Selection s = (Selection) op.getObject();
            Iterator<Operand> it = s.getOperands().iterator();
            while (it.hasNext()) {
                Operand o = it.next();
                if (o instanceof UnaryWhereCondition) {
                    UnaryWhereCondition uwc0 = (UnaryWhereCondition) o;
                    UnaryWhereCondition uwcCloned =
                        new UnaryWhereCondition(uwc0.getType(), uwc0.getColumn(), uwc0.getNot());

                    if (!inputName.equals(current.getTemporaryTableName())) {
                        Column c = uwcCloned.getColumn();
                        uwcCloned.changeColumn(c, new Column(inputName, c.columnName));
                    }
                    current.addUnaryWhereCondition(uwcCloned);

                } else {
                    NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o;
                    NonUnaryWhereCondition nuwcCloned =
                        new NonUnaryWhereCondition(nuwc.getLeftOp(), nuwc.getRightOp(),
                            nuwc.getOperator());
                    if (!inputName.equals(current.getTemporaryTableName())) {
                        for (Column c : nuwcCloned.getAllColumnRefs()) {
                            nuwcCloned.changeColumn(c, new Column(inputName, c.columnName));
                        }
                    }
                    current.addBinaryWhereCondition(nuwcCloned);
                }
            }

            for (Output o : current.getOutputs()) {
                for (Column c : o.getObject().getAllColumnRefs()) {
                    {
                        if (op.isDescendantOfBaseTable(c.tableAlias)) {
                            // if (baseTableIsDescendantOfNode(op.getChildAt(0),
                            // c.tableAlias)) {
                            if (op.getChildAt(0).isDescendantOfBaseTable(c.tableAlias)) {
                                o.getObject().changeColumn(c, new Column(inputName, c.columnName));
                            }
                        }
                    }
                    o.getObject().changeColumn(c, c);
                }
            }
            // tempResult.add(q);

        } else {
            for (int l = 0; l < op.getChildren().size(); l++) {
                // visited.put(op, current);
                tempResult.setCurrent(new SQLQuery());
                combineOperatorsAndOutputQueries3(p.getInputPlan(l), tempResult, visited);
                SQLQuery q3 = new SQLQuery();
                visited.put(p.getInputPlan(l), q3);
                if (memo.getMemoValue(p.getInputPlan(l)).isMaterialised()) {
                    q3.setMaterialised(true);
                }
                tempResult.add(current);
            }
            if (op.getChildren().isEmpty() && op.getObject() instanceof Table) {
                current.getInputTables().add((Table) op.getObject());
                tempResult.add(current);
            }
        }

		/*
		 * if (visited.containsKey(o)) { //just repartition String tableName =
		 * visited.get(o).getTemporaryTableName();
		 * current.addInputTableIfNotExists(new Table(tableName, tableName));
		 * tempResult.add(current); } else {
		 * combineOperatorsAndOutputQueries2(k, tempResult, q, visited); }
		 */

		/*
		 * if (visited.containsKey(op)) {
		 * tempResult.setLastTableName(visited.get(op)); return; }
		 */
    }

}
