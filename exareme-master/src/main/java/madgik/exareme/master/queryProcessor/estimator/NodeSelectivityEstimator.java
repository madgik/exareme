package madgik.exareme.master.queryProcessor.estimator;

import com.google.gson.Gson;
import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.query.*;
import madgik.exareme.master.queryProcessor.estimator.db.RelInfo;
import madgik.exareme.master.queryProcessor.estimator.db.Schema;
import madgik.exareme.master.queryProcessor.estimator.histogram.Histogram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jim
 */
public class NodeSelectivityEstimator implements SelectivityEstimator {
	private static final int HASH_STRING_CHARS = 3;
	private static final int HASH_STRING_BASE = 256;

	private Schema schema;
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SelectivityEstimator.class);

	public NodeSelectivityEstimator(String json) throws Exception {
		BufferedReader br;
		br = new BufferedReader(new FileReader(json));

		// convert the json string back to object
		Gson gson = new Gson();
		schema = gson.fromJson(br, Schema.class);

		// System.out.println(schema);

		// HashMap<String, HashSet<String>> keys = new HashMap<String,
		// HashSet<String>>();
		// try {
		// keys =
		// di.madgik.decomposer.util.Util.getMysqlIndices("jdbc:mysql://10.240.0.10:3306/npd?"
		// + "user=benchmark&password=gray769watt724!@#");
		// } catch (SQLException ex) {
		// Logger.getLogger(NodeSelectivityEstimator.class.getName()).log(Level.SEVERE,
		// null, ex);
		// }
		// for (String table : keys.keySet()) {
		// if(schema.getTableIndex().containsKey(table)){
		// schema.getTableIndex().get(table).setHashAttr(keys.get(table));
		// schema.getTableIndex().get(table).setNumberOfPartitions(1);}
		// else{
		// Logger.getLogger(NodeSelectivityEstimator.class.getName()).log(Level.WARNING,
		// "Table {0} does not exist in stat json file", table);
		// }
		// }

	}

	@Override
	public void makeEstimationForNode(Node n) {
		 try{
		if (!n.getObject().toString().startsWith("table")) {
			estimateBase(n);
		} else {
			Node o = n.getChildAt(0);
			if (o.getOpCode() == Node.JOIN) {
				NonUnaryWhereCondition bwc = (NonUnaryWhereCondition) o.getObject();
				if (o.getChildren().size() == 1) {
					estimateJoin(n, bwc, o.getChildAt(0), o.getChildAt(0));
				} else {
					estimateJoin(n, bwc, o.getChildAt(0), o.getChildAt(1));
				}
			} else if (o.getOpCode() == Node.PROJECT) {
				estimateProject(n);
			} else if (o.getOpCode() == Node.SELECT) {
				Selection s = (Selection) o.getObject();
				estimateFilter(n, s, o.getChildAt(0));
			} else if (o.getOpCode() == Node.UNION) {
				estimateUnion(n);
			}
		}
		 }catch(Exception ex){
		 log.error("cannot compute selectivity for node "+n.getObject().toString()+":"+ ex.getMessage());
		 }

	}

	public void estimateFilter(Node n, Selection s, Node child) {
		// Selection s = (Selection) n.getObject();
		NodeInfo ni = new NodeInfo();
		n.setNodeInfo(ni);
		NodeInfo childInfo = child.getNodeInfo();
		Set<Operand> filters = s.getOperands();

		RelInfo initRel = childInfo.getResultRel();

		// one select node can contain more than one filter!
		for (Operand nextFilter : filters) {
			if (nextFilter instanceof UnaryWhereCondition) {
				// normally you don't care for these conditions (Column IS NOT
				// NULL)
				// UnaryWhereCondition uwc = (UnaryWhereCondition) nextFilter;
				// Table t=(Table) child.getObject();
				// if(t.getName().startsWith("table")){
				// not base table

				// TODO: fix nodeInfo
				ni.setNumberOfTuples(childInfo.getNumberOfTuples());
				ni.setTupleLength(childInfo.getTupleLength());
				ni.setResultRel(new RelInfo(childInfo.getResultRel()));

				// this.planInfo.get(n.getHashId()).setNumberOfTuples(child.getNumberOfTuples());
				// this.planInfo.get(n.getHashId()).setTupleLength(child.getTupleLength());
				// System.out.println(uwc);
			} else {
				// TODO correct this!
				NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) nextFilter;
				// System.out.println(nuwc);
				String operator = nuwc.getOperator(); // e.g. =, <, >

				Column col;
				Constant con;

				if (nuwc.getLeftOp() instanceof Column) {// TODO: constant
					col = (Column) nuwc.getLeftOp();
					con = (Constant) nuwc.getRightOp();
				} else {
					col = (Column) nuwc.getRightOp();
					con = (Constant) nuwc.getLeftOp();
				}

				// RelInfo lRel =
				// this.schema.getTableIndex().get(col.tableAlias);
				RelInfo lRel = childInfo.getResultRel();
				// RelInfo resultRel = new RelInfo(lRel);
				RelInfo resultRel = initRel;

				Histogram resultHistogram = resultRel.getAttrIndex().get(col.getName()).getHistogram();

				double filterValue = 0;
				if (!con.isArithmetic()) {
					filterValue = hashString(con.getValue().toString());
				} else {
					filterValue = Double.parseDouble(con.getValue().toString());
				}

				if (operator.equals("="))
					resultHistogram.equal(filterValue);
				else if (operator.equals(">="))
					resultHistogram.greaterOrEqual(filterValue);
				else if (operator.equals("<="))
					resultHistogram.lessOrEqualValueEstimation(filterValue);
				else if (operator.equals(">"))
					resultHistogram.greaterThan(filterValue);
				else if (operator.equals("<"))
					resultHistogram.lessThanValueEstimation(filterValue);
				// else f = new Filter(col.tableAlias, col.columnName,
				// FilterOperand.NotEqual, Double.parseDouble(con.toString()));

				// adjust RelInfo's histograms based on the resulting histogram
				resultRel.adjustRelation(col.getName(), resultHistogram);

				// TODO: fix NOdeInfo!!
				ni.setNumberOfTuples(resultRel.getNumberOfTuples());
				ni.setTupleLength(resultRel.getTupleLength());
				ni.setResultRel(resultRel);
			}
		}
	}

	public void estimateJoin(Node n, NonUnaryWhereCondition nuwc, Node left, Node right) {
		// NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) n.getObject();
		NodeInfo ni = new NodeInfo();
		Column l = (Column) nuwc.getLeftOp();
		Column r = (Column) nuwc.getRightOp();
		// String equals = nuwc.getOperator();

		// RelInfo lRel = this.schema.getTableIndex().get(l.tableAlias);
		// RelInfo rRel = this.schema.getTableIndex().get(r.tableAlias);
		RelInfo lRel = left.getNodeInfo().getResultRel();
		RelInfo rRel = right.getNodeInfo().getResultRel();

		RelInfo resultRel = new RelInfo(lRel);

		Histogram resultHistogram = resultRel.getAttrIndex().get(l.getName()).getHistogram();

		resultHistogram.join(rRel.getAttrIndex().get(r.getName()).getHistogram());

		// lRel.getAttrIndex().get(l.columnName).getHistogram().join(rRel.getAttrIndex().get(r.columnName).getHistogram());

		// put all the right's RelInfo AttrInfos to the left one
		resultRel.getAttrIndex().putAll(rRel.getAttrIndex());

		// adjust RelInfo's histograms based on the resulting histogram
		resultRel.adjustRelation(l.getName(), resultHistogram);

		// fix alias mappings to RelInfo. The joining aliases must point to the
		// same RelInfo after the join operation
		schema.getTableIndex().put(l.getAlias(), resultRel);
		schema.getTableIndex().put(r.getAlias(), resultRel);

		// adding necessary equivalent hashing attribures
		resultRel.getHashAttr().addAll(rRel.getHashAttr());

		// TODO: fix nodeInfo
		ni.setNumberOfTuples(resultRel.getNumberOfTuples());
		ni.setTupleLength(resultRel.getTupleLength());
		ni.setResultRel(resultRel);
		n.setNodeInfo(ni);
	}

	public void estimateProject(Node n) {
		// String tableAlias;
		NodeInfo ni = new NodeInfo();
		n.setNodeInfo(ni);
		Set<String> columns = new HashSet<String>();
		Node prjNode = n.getChildAt(0);
		Node child = prjNode.getChildAt(0);
		Projection p = (Projection) prjNode.getObject();
		List<Output> outputs = p.getOperands();
		// tableAlias = ((Column)outputs.get(0).getObject()).tableAlias;

		// RelInfo rel = this.schema.getTableIndex().get(tableAlias);
		if (child.getNodeInfo() == null) {
			this.makeEstimationForNode(child);
		}
		RelInfo rel = child.getNodeInfo().getResultRel();

		RelInfo resultRel = new RelInfo(rel);

		for (Output o : outputs) {
			List<Column> cols = o.getObject().getAllColumnRefs();
			if (!cols.isEmpty()) {
				Column c = (Column) o.getObject().getAllColumnRefs().get(0);
				columns.add(c.getName());
			}
		}

		// remove unecessary columns
		resultRel.eliminteRedundantAttributes(columns);

		// TODO: fix nodeInfo
		ni.setNumberOfTuples(child.getNodeInfo().getNumberOfTuples());
		// ni.setTupleLength(child.getNodeInfo().getTupleLength());
		ni.setTupleLength(resultRel.getTupleLength());
		// System.out.println("is this correct?");
		ni.setResultRel(resultRel);
		n.setNodeInfo(ni);
	}

	public void estimateUnion(Node n) {
		Node unionOp = n.getChildAt(0);
		List<Node> children = unionOp.getChildren();
		double numOfTuples = 0;
		double tupleLength = children.get(0).getNodeInfo().getTupleLength();

		for (Node cn : children) {
			numOfTuples += cn.getNodeInfo().getNumberOfTuples();
		}
		NodeInfo ni = new NodeInfo();
		// TODO: fix nodeInfo
		ni.setNumberOfTuples(numOfTuples);
		ni.setTupleLength(tupleLength);
		n.setNodeInfo(ni);
	}

	public void estimateBase(Node n) {
		NodeInfo pi = new NodeInfo();
		String tableAlias = ((Table) n.getObject()).getName();
		RelInfo rel = this.schema.getTableIndex().get(tableAlias);
		// RelInfo rel = this.planInfo.get(n.getHashId()).getResultRel();

		// System.out.println(rel);
		RelInfo resultRel = new RelInfo(rel);

		// TODO: fix nodeInfo
		pi.setNumberOfTuples(rel.getNumberOfTuples());
		pi.setTupleLength(rel.getTupleLength());
		pi.setResultRel(resultRel);
		n.setNodeInfo(pi);
	}

	/* private-util methods */
	public static double hashString(String str) {
		if (str == null)
			return 0;
		double hashStringVal = 0.0;
		if (str.length() >= HASH_STRING_CHARS) {
			char[] hashChars = new char[HASH_STRING_CHARS];

			for (int i = 0; i < HASH_STRING_CHARS; i++) {
				hashChars[i] = str.charAt(i);
			}

			for (int i = 0; i < HASH_STRING_CHARS; i++) {
				hashStringVal += (double) ((int) hashChars[i])
						* Math.pow((double) HASH_STRING_BASE, (double) (HASH_STRING_CHARS - i));
			}
			return hashStringVal;
		}

		else {
			char[] hashChars = new char[str.length()];

			for (int i = 0; i < str.length(); i++)
				hashChars[i] = str.charAt(i);

			for (int i = 0; i < str.length(); i++) {
				hashStringVal += (double) ((int) hashChars[i])
						* Math.pow((double) HASH_STRING_BASE, (double) (HASH_STRING_CHARS - i));
			}

			return hashStringVal;
		}

	}
}
