/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.DecomposerUtils;
import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.dag.NodeHashValues;
import madgik.exareme.master.queryProcessor.decomposer.query.*;
import madgik.exareme.master.queryProcessor.decomposer.util.Util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


//import di.madgik.statistics.planner.JoinExecutionAdvice;
//import di.madgik.statistics.planner.StarPlanner;
import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.google.common.hash.HashCode;

/**
 * @author dimitris
 */
public class ConjunctiveQueryDecomposer {

	private SQLQuery initialQuery;
	private List<String> dbs;
	private List<NonUnaryWhereCondition> remainingWhereConditions;
	private ColumnsToTableNames<String> c2t;
	private ColumnsToTableNames<Node> c2n;
	private ArrayList<SQLQuery> result;
	// private boolean singleTable = false;
	private ArrayList<SQLQuery> nestedSubqueries;
	HashSet<Join> lj;
	private boolean centralizedExecution;
	// private boolean mergeSelections;
	// private static int counter=0;
	private static final Logger log = Logger
			.getLogger(ConjunctiveQueryDecomposer.class);

	public ConjunctiveQueryDecomposer(SQLQuery initial, boolean centralized,
			boolean addRedundantIsNotNull) {
		this.initialQuery = initial;
		this.dbs = new ArrayList<String>();
		this.lj = new HashSet<Join>();
		this.remainingWhereConditions = new ArrayList<NonUnaryWhereCondition>();
		this.nestedSubqueries = new ArrayList<SQLQuery>();
		/*
		 * for (Table t : initialQuery.getInputTables()) { if (t.isFederated())
		 * { this.initialQuery.setFederated(true); if
		 * (!dbs.contains(t.getDBName())) { dbs.add(t.getDBName()); } } }
		 */
		for (NonUnaryWhereCondition bwc : initialQuery
				.getBinaryWhereConditions()) {
			this.remainingWhereConditions.add(bwc);
		}
		this.centralizedExecution = centralized;
		if (addRedundantIsNotNull) {
			for (int i = 0; i < this.initialQuery.getUnaryWhereConditions()
					.size(); i++) {
				UnaryWhereCondition uwc = this.initialQuery
						.getUnaryWhereConditions().get(i);
				if (uwc.getNot()
						&& uwc.getType() == UnaryWhereCondition.IS_NULL) {
					Column c = uwc.getAllColumnRefs().get(0);
					for (NonUnaryWhereCondition nuwc : this.initialQuery
							.getBinaryWhereConditions()) {
						if (nuwc.getOperator().equals("=")) {
							Column other = null;
							if (nuwc.getLeftOp().equals(c)
									&& nuwc.getRightOp() instanceof Column) {
								other = (Column) nuwc.getRightOp();
							}
							if (nuwc.getRightOp().equals(c)
									&& nuwc.getLeftOp() instanceof Column) {
								other = (Column) nuwc.getLeftOp();
							}
							if (other != null) {
								try {
									UnaryWhereCondition toAdd = new UnaryWhereCondition(
											UnaryWhereCondition.IS_NULL,
											other.clone(), true);
									if (!this.initialQuery
											.getUnaryWhereConditions()
											.contains(toAdd)) {
										this.initialQuery
												.getUnaryWhereConditions().add(
														toAdd);
									}
								} catch (CloneNotSupportedException ex) {
									log.error(ex.getMessage());
								}
							}
						}
					}
				}
			}
		}

	}

	public Node addCQToDAG(Node root, NodeHashValues hashes) {

		// columnsToSubqueries tracks from which temporary table we take each
		// column of the initial query
		c2n = new ColumnsToTableNames<Node>();

		// else we have only one DB, for each table make a subquery
		// else {
		Node last = null;
		for (Table t : this.initialQuery.getInputTables()) {
			Node table = new Node(Node.OR);
			table.addDescendantBaseTable(t.getAlias());

			table.setObject(t);
			if (!hashes.containsKey(table.getHashId())) {

				// table.setHashID(Objects.hash(t.getName()));
				// table.setIsBaseTable(true);
				hashes.put(table.getHashId(), table);
			} else {
				table = hashes.get(table.getHashId());
			}
			last = table;
			ArrayList<String> dummy = new ArrayList<String>();
			dummy.add(t.getAlias());
			SQLQuery s = createSubqueriesForTables(dummy, "");

			Node selection = new Node(Node.AND, Node.SELECT);
			Selection sel = new Selection();
			for (UnaryWhereCondition uwc : s.getUnaryWhereConditions()) {

				sel.addOperand(uwc);
			}
			for (NonUnaryWhereCondition bwc : s.getBinaryWhereConditions()) {

				sel.addOperand(bwc);
			}
			if (sel.getOperands().size() > 0) {

				selection.addChild(table);
				// selection.setPartitionRecord(table.getPartitionRecord());
				// selection.setLastPartition(table.getLastPartition());
				selection.setObject(sel);
				if (!hashes.containsKey(selection.getHashId())) {
					hashes.put(selection.getHashId(), selection);
					selection.addDescendantBaseTable(t.getAlias());
					// selection.addChild(table);

				} else {
					selection = hashes.get(selection.getHashId());
				}

				Node selTable = new Node(Node.OR);

				Table selt = new Table("table" + Util.createUniqueId(), null);
				selTable.setObject(selt);
				selTable.addChild(selection);

				if (!hashes.containsKey(selTable.getHashId())) {
					selTable.addDescendantBaseTable(t.getAlias());
					hashes.put(selTable.getHashId(), selTable);
					// selection.addChild(table);

				} else {
					selTable = hashes.get(selTable.getHashId());

				}
				last = selTable;

				for (Column cl : s.getAllColumns()) {
					c2n.putColumnInTable(cl, selTable);
					// add projections

				}
			} else {
				for (Column cl : s.getAllColumns()) {
					c2n.putColumnInTable(cl, table);
					// add projections

				}
			}

		}

		for (SQLQuery nested : this.initialQuery.getNestedSelectSubqueries()
				.keySet()) {
			String alias = this.initialQuery.getNestedSubqueryAlias(nested);
			Node table = nested.getNestedNode();
			last = table;
			ArrayList<String> dummy = new ArrayList<String>();
			dummy.add(alias);
			SQLQuery s = createSubqueriesForTables(dummy, "");

			Node selection = new Node(Node.AND, Node.SELECT);
			Selection sel = new Selection();
			for (UnaryWhereCondition uwc : s.getUnaryWhereConditions()) {

				sel.addOperand(uwc);
			}
			for (NonUnaryWhereCondition bwc : s.getBinaryWhereConditions()) {

				sel.addOperand(bwc);
			}

			if (sel.getOperands().size() > 0) {

				selection.addChild(table);
				// selection.setPartitionRecord(table.getPartitionRecord());
				// selection.setLastPartition(table.getLastPartition());
				selection.setObject(sel);
				if (!hashes.containsKey(selection.getHashId())) {
					hashes.put(selection.getHashId(), selection);
					selection.addDescendantBaseTable(alias);
					// selection.addChild(table);

				} else {
					selection = hashes.get(selection.getHashId());
				}

				Node selTable = new Node(Node.OR);

				Table selt = new Table("table" + Util.createUniqueId(), null);
				selTable.setObject(selt);
				selTable.addChild(selection);
				selTable.setPartitionRecord(selection.getPartitionRecord());
				// selTable.setLastPartition(selection.getLastPartition());
				// selTable.setIsCentralised(table.isCentralised());
				// selTable.setPartitionedOn(table.isPartitionedOn());
				if (!hashes.containsKey(selTable.getHashId())) {
					selTable.addDescendantBaseTable(alias);
					hashes.put(selTable.getHashId(), selTable);
					// selection.addChild(table);

				} else {
					selTable = hashes.get(selTable.getHashId());
				}
				last = selTable;

				for (Column cl : s.getAllColumns()) {
					c2n.putColumnInTable(cl, selTable);
					// add projections

				}
			} else {
				for (Column cl : s.getAllColumns()) {
					c2n.putColumnInTable(cl, table);
					// add projections

				}
			}
		}

		List<Set<String>> joinSets = new ArrayList<Set<String>>();
		for (Table t : this.getInputTables()) {
			Set<String> tableSet = new HashSet<String>();
			tableSet.add(t.getAlias());
			joinSets.add(tableSet);
		}
		for (String n : this.initialQuery.getNestedSelectSubqueries().values()) {
			Set<String> tableSet = new HashSet<String>();
			tableSet.add(n);
			joinSets.add(tableSet);
		}
		if (remainingWhereConditions.isEmpty()) {

			if (joinSets.size() > 1) {
				NonUnaryWhereCondition toAdd = addConditionsFromConstants();
				if (toAdd == null) {
					log.error("Input query contains cartesian product. Currently not supported");

				} else {
					this.initialQuery.getBinaryWhereConditions().add(toAdd);
					remainingWhereConditions.add(toAdd);
				}
			} else {
				if (last == null) {
					return root;
				}
				Node tempParent = makeNodeFinal(last, hashes);
				root.addChild(tempParent);
				return tempParent;
			}

		}

		while (!this.remainingWhereConditions.isEmpty()) {
			NonUnaryWhereCondition bwc = this.remainingWhereConditions.get(0);
			mergeJoinSets(joinSets, bwc);
			Node join = new Node(Node.AND, Node.JOIN);
			join.setObject(bwc);

			if (bwc.getLeftOp() instanceof Column
					&& bwc.getRightOp() instanceof Column) {
				Node lchild = c2n.getTablenameForColumn((Column) bwc
						.getLeftOp());
				join.addChild(lchild);
				join.addAllDescendantBaseTables(lchild
						.getDescendantBaseTables());

				Node rchild = c2n.getTablenameForColumn((Column) bwc
						.getRightOp());
				join.addChild(rchild);
				join.addAllDescendantBaseTables(rchild
						.getDescendantBaseTables());

			} else {

				log.error(bwc.toString() + ":operand not Column");

			}
			HashCode hc=join.getHashId();
			if (!hashes.containsKey(hc)) {
				hashes.put(join.getHashId(), join);
			} else {
				join.removeAllChildren();
				join = hashes.get(hc);
			}

			Node table = new Node(Node.OR);
			Table t = new Table("table" + Util.createUniqueId(), null);
			table.setObject(t);
			table.addChild(join);
			// table.setIsCentralised(tableIsCentralised);
			hc=table.getHashId();
			if (!hashes.containsKey(hc)) {
				hashes.put(table.getHashId(), table);
				table.addAllDescendantBaseTables(join.getDescendantBaseTables());
			} else {
				table.removeAllChildren();
				table = hashes.get(hc);
			}

			// change columns according to columnsToSubqueries
			// ConcurrentHashMap<Column, Column> changePairs = new
			// ConcurrentHashMap();

			// for (Node toChange : join.getChildren()) {
			c2n.changeColumns(
					c2n.getTablenameForColumn((Column) bwc.getLeftOp()), table);
			c2n.changeColumns(
					c2n.getTablenameForColumn((Column) bwc.getRightOp()), table);
			// }

			if (remainingWhereConditions.size() == 1) {
				if (joinSets.size() > 1) {
					NonUnaryWhereCondition toAdd = addConditionsFromConstants();
					if (toAdd == null) {
						log.error("Input query contains cartesian product. Currently not supported");

					} else {
						this.initialQuery.getBinaryWhereConditions().add(toAdd);
						remainingWhereConditions.add(toAdd);
					}
				} else {
					Node tempParent = makeNodeFinal(table, hashes);
					root.addChild(tempParent);
					return tempParent;
				}

			}

			this.remainingWhereConditions.remove(0);

		}

		return null;
		// return result;
	}

	private NonUnaryWhereCondition addConditionsFromConstants() {
		NonUnaryWhereCondition result = null;
		for (int i = 0; i < this.initialQuery.getBinaryWhereConditions().size(); i++) {
			NonUnaryWhereCondition nuwc = this.initialQuery
					.getBinaryWhereConditions().get(i);
			if (nuwc.getOperator().equals("=")
					&& (nuwc.getLeftOp() instanceof Constant || nuwc
							.getRightOp() instanceof Constant)) {
				Constant c = nuwc.getLeftOp() instanceof Constant ? (Constant) nuwc
						.getLeftOp() : (Constant) nuwc.getRightOp();
				Operand o = nuwc.getLeftOp();
				if (o.equals(c)) {
					o = nuwc.getRightOp();
				}
				for (int j = i + 1; j < this.initialQuery
						.getBinaryWhereConditions().size(); j++) {
					NonUnaryWhereCondition nuwc2 = this.initialQuery
							.getBinaryWhereConditions().get(j);
					if (nuwc2.getOperator().equals("=")
							&& (nuwc2.getLeftOp().equals(c) || nuwc2
									.getRightOp().equals(c))) {
						Operand o2 = nuwc2.getLeftOp();
						if (o2.equals(c)) {
							o2 = nuwc2.getRightOp();
						}
						result = new NonUnaryWhereCondition();
						result.addOperand(o);
						result.addOperand(o2);
						result.setOperator("=");
						NonUnaryWhereCondition reverse = new NonUnaryWhereCondition();
						reverse.addOperand(o2);
						reverse.addOperand(o);
						reverse.setOperator("=");
						if (!this.initialQuery.getBinaryWhereConditions()
								.contains(result)
								&& !this.initialQuery
										.getBinaryWhereConditions().contains(
												reverse)) {
							return result;
						}
					}
				}
			}
		}
		return null;
	}

	private void mergeJoinSets(List<Set<String>> joinSets,
			NonUnaryWhereCondition bwc) {
		String l = bwc.getLeftOp().getAllColumnRefs().get(0).getAlias();
		String r = bwc.getRightOp().getAllColumnRefs().get(0).getAlias();
		;
		for (int i = 0; i < joinSets.size(); i++) {
			Set<String> tables = joinSets.get(i);
			if (tables.contains(l) && tables.contains(r)) {
				return;
			} else if (tables.contains(r)) {
				for (int j = 0; j < joinSets.size(); j++) {
					Set<String> tables2 = joinSets.get(j);
					if (tables2.contains(l)) {
						for (String t : tables2) {
							tables.add(t);
						}
						joinSets.remove(tables2);
						return;
					}
				}
			} else if (tables.contains(l)) {
				for (int j = 0; j < joinSets.size(); j++) {
					Set<String> tables2 = joinSets.get(j);
					if (tables2.contains(r)) {
						for (String t : tables2) {
							tables.add(t);
						}
						joinSets.remove(tables2);
						return;
					}
				}
			}
		}
	}

	public ArrayList<SQLQuery> getSubqueries() {

		// columnsToSubqueries tracks from which temporary table we take each
		// column of the initial query
		c2t = new ColumnsToTableNames<String>();

		for (Column initialQueryColumn : this.initialQuery.getAllColumns()) {
			c2t.putColumnInTable(initialQueryColumn,
					initialQuery.getResultTableName());
		}
		// track columns from nested select subqueries
		for (SQLQuery nested : this.initialQuery.getNestedSubqueries()) {
			this.nestedSubqueries.add(nested);
			for (String alias : nested.getOutputAliases()) {
				c2t.putColumnInTable(
						new Column(this.initialQuery
								.getNestedSubqueryAlias(nested), alias), nested
								.getResultTableName());
			}
		}

		result = new ArrayList<SQLQuery>();

		if (initialQuery.getInputTables().size() == 1
				&& !initialQuery.hasNestedSuqueriesOrLeftJoin()) {
			// return initial as only subquery
			if (this.initialQuery.isFederated()) {

				for (Table t : initialQuery.getInputTables()) {

					if (!t.hasDBIdRemoved()) {
						t.setName(t.getlocalName());
						t.setDBIdRemoved();
					}
					t.removeDBIdFromAlias();

					initialQuery.setMadisFunctionString(DBInfoReaderDB.dbInfo
							.getDB(dbs.get(0)).getMadisString());
					this.initialQuery.setTemporary(false);
					if (result.isEmpty()) {
						renameOutputColumnsInNestedSubs();
					}
					result.add(this.initialQuery);

				}

			} else {
				this.initialQuery.setTemporary(false);
				if (result.isEmpty()) {
					renameOutputColumnsInNestedSubs();
				}
				result.add(initialQuery);
			}
			return result;
		} else if (dbs.size() > 0 && this.initialQuery.isFederated()) {
			// we have more than one dbs. break the query into queries that can
			// be executed in only one db
			for (String db : dbs) {
				SQLQuery s = createSubqueryFromDB(db);
				if (result.isEmpty()) {
					renameOutputColumnsInNestedSubs();
				}
				result.add(s);
				for (Column c : s.getAllOutputColumns()) {
					c2t.putColumnInTable(c, s.getResultTableName());
				}
			}

		} // else we have only one DB, for each table make a subquery
		else {
			for (Table t : this.initialQuery.getInputTables()) {
				ArrayList<String> dummy = new ArrayList<String>();
				dummy.add(t.getAlias());
				SQLQuery s = createSubqueriesForTables(dummy, "");
				if (result.isEmpty()) {
					renameOutputColumnsInNestedSubs();
				}
				result.add(s);
				for (Column c : s.getAllOutputColumns()) {
					c2t.putColumnInTable(c, s.getResultTableName());
				}
			}
		}

		if (this.remainingWhereConditions.isEmpty()) {
			if (this.result.size() > 1) {
				// CARTESIAN PRODUCT!!!!
				SQLQuery cartesianSubquery = new SQLQuery();
				for (SQLQuery q : this.result) {
					cartesianSubquery.getInputTables().add(
							new Table(q.getResultTableName(), ""));
				}
				makeSubqueryFinal(cartesianSubquery);
				for (SQLQuery previous : result) {
					previous.setTemporary(true);
				}
				result.add(cartesianSubquery);

			}
			return result;
		}

		// we have a query with more than one join, we must rearrange them using
		// the optimizer

		if (this.centralizedExecution) {
			SQLQuery joinSubquery = new SQLQuery();
			// result.add(joinSubquery);
			while (!this.remainingWhereConditions.isEmpty()) {
				NonUnaryWhereCondition bwc = this.remainingWhereConditions
						.get(0);

				// change the tables in the columns of the condition to be
				// tracked from previous subqueries
				for (Column otherColumn : c2t.getAllColumns()) {
					for (Column c : bwc.getAllColumnRefs()) {
						if (otherColumn.equals(c)) {
							SQLQuery temporarySubquery = getTemporarySubquery(c2t
									.getTablenameForColumn(otherColumn));
							if (temporarySubquery != null) {

								if (!joinSubquery.getInputTables().contains(
										new Table(temporarySubquery
												.getResultTableName(), ""))) {
									joinSubquery.getInputTables().add(
											new Table(temporarySubquery
													.getResultTableName(), ""));
									for (Output o : temporarySubquery
											.getOutputs()) {
										// if (o.getObject() instanceof Column)
										// {
										Column toadd = new Column(
												temporarySubquery
														.getResultTableName(),
												o.getOutputName());
										joinSubquery.getOutputs().add(
												new Output(o.getOutputName(),
														toadd));

									}
								}
							}
						}
					}
				}

				// change columns according to columnsToSubqueries
				// ConcurrentHashMap<Column, Column> changePairs = new
				// ConcurrentHashMap();
				List<Column> allRefs = bwc.getAllColumnRefs();
				Column[] olds = new Column[allRefs.size()];
				Column[] news = new Column[allRefs.size()];

				for (int i = 0; i < allRefs.size(); i++) {
					Column c = allRefs.get(i);
					Column toChange = new Column(c2t.getTablenameForColumn(c),
							c.getAlias() + "_" + c.getName());
					olds[i] = c;
					news[i] = toChange;
					// changePairs.put(c, toChange);
				}
				for (int j = 0; j < olds.length; j++) {
					// for (Column old : changePairs.keySet()) {
					for (int i = 0; i < bwc.getOperands().size(); i++) {
						Operand o = bwc.getOperands().get(i);
						if (o instanceof Column) {
							if (((Column) o).equals(olds[j])) {
								bwc.setOperandAt(i, news[j]);
							}
						} else {
							o.changeColumn(olds[j], news[j]);
						}
					}
				}
				joinSubquery.getBinaryWhereConditions().add(bwc);

				// change columns according to columnsToSubqueries

				if (remainingWhereConditions.size() == 1) {
					// this was the last where condition
					makeSubqueryFinal(joinSubquery);
					if (result.isEmpty()) {
						renameOutputColumnsInNestedSubs();
					}
					result.add(joinSubquery);
					return result;
				}
				this.remainingWhereConditions.remove(0);

			}

		}

		while (!this.remainingWhereConditions.isEmpty()) {
			NonUnaryWhereCondition bwc = this.remainingWhereConditions.get(0);

			SQLQuery joinSubquery = new SQLQuery();
			// change the tables in the columns of the condition to be tracked
			// from previous subqueries
			for (Column otherColumn : c2t.getAllColumns()) {
				for (Column c : bwc.getAllColumnRefs()) {
					if (otherColumn.equals(c)) {
						SQLQuery temporarySubquery = getTemporarySubquery(c2t
								.getTablenameForColumn(otherColumn));
						// temporarySubquery.setPartitioningOnColum(new
						// Column(null, c.tableAlias + "_" + c.columnName));
						// Column c2 = new
						// Column(temporarySubquery.getResultTableName(), "");
						if (!joinSubquery.getInputTables().contains(
								new Table(temporarySubquery
										.getResultTableName(), ""))) {
							joinSubquery.getInputTables().add(
									new Table(temporarySubquery
											.getResultTableName(), ""));
							for (Output o : temporarySubquery.getOutputs()) {
								// if (o.getObject() instanceof Column) {
								Column toadd = new Column(
										temporarySubquery.getResultTableName(),
										o.getOutputName());
								joinSubquery.getOutputs().add(
										new Output(o.getOutputName(), toadd));
								// } else {

								// System.out.print("OOOOOOOOOOOOO"+o.toString());
								// TODO?
								// }
								// if (o.getOutputName().equals(c.tableAlias +
								// "_" + c.columnName)) {
								// c2.columnName = o.getOutputName();
								// joinSubquery.outputs.add(new
								// Output(o.getOutputName(), c2));

								// }
							}
						}
					}
				}
			}

			// change columns according to columnsToSubqueries
			// ConcurrentHashMap<Column, Column> changePairs = new
			// ConcurrentHashMap();
			List<Column> allRefs = bwc.getAllColumnRefs();
			Column[] olds = new Column[allRefs.size()];
			Column[] news = new Column[allRefs.size()];

			for (int i = 0; i < allRefs.size(); i++) {
				Column c = allRefs.get(i);
				Column toChange = new Column(c2t.getTablenameForColumn(c),
						c.getAlias() + "_" + c.getName());
				olds[i] = c;
				news[i] = toChange;
				// changePairs.put(c, toChange);
			}
			for (int j = 0; j < olds.length; j++) {
				// for (Column old : changePairs.keySet()) {
				for (int i = 0; i < bwc.getOperands().size(); i++) {
					Operand o = bwc.getOperands().get(i);
					if (o instanceof Column) {
						if (((Column) o).equals(olds[j])) {
							bwc.setOperandAt(i, news[j]);
						}
					} else {
						o.changeColumn(olds[j], news[j]);
					}
				}
			}
			joinSubquery.getBinaryWhereConditions().add(bwc);

			// change columns according to columnsToSubqueries

			if (remainingWhereConditions.size() == 1) {
				// this was the last where condition
				makeSubqueryFinal(joinSubquery);
				result.add(joinSubquery);
				return result;
			}
			this.remainingWhereConditions.remove(0);
			// alter columns of the initial query that must be tracked from this
			// subquery to the columnsToSubqueries
			// and also forget variables that are not in initial
			// projection/functions and are not in remaining joins
			ArrayList<Output> toDelete = new ArrayList<Output>();
			for (Output o : joinSubquery.getOutputs()) {
				if (o.getObject() instanceof Column) {
					Column c = (Column) o.getObject();
					boolean needed = false;
					for (Column initial : this.initialQuery.getAllColumns()) {
						if (c.getName().startsWith(initial.getAlias() + "_")) {
							needed = true;
							c2t.putColumnInTable(initial,
									joinSubquery.getResultTableName());
							// break;
						}
					}
					// now check if we need it in remaining joins
					if (!needed) {
						for (NonUnaryWhereCondition remainingbwc : this.remainingWhereConditions) {
							for (Column rc : remainingbwc.getAllColumnRefs()) {
								if ((rc.getAlias() + "_" + rc.getName())
										.equals(c.getName())) {
									needed = true; // we need it for subsequent
													// join, do not delete
									Table t = new Table(o.getOutputName(), null);
									c2t.putColumnInTable(
											new Column(t.getDBName(), t
													.getlocalName()),
											joinSubquery.getResultTableName());
									break;
								}
							}
						}
					}

					if (!needed) {
						toDelete.add(o);
					}
				}
			}
			for (Output delete : toDelete) {
				joinSubquery.getOutputs().remove(delete);
			}

			result.add(joinSubquery);

		}

		return result;
	}

	private SQLQuery createSubqueryFromDB(String dbID) {

		ArrayList<String> tablesFromDB = new ArrayList<String>();
		for (Table t : initialQuery.getInputTables()) {
			if (t.isFederated() && t.getDBName().equals(dbID)) {
				tablesFromDB.add(t.getAlias());
			}
		}
		return createSubqueriesForTables(tablesFromDB, dbID);

	}

	private SQLQuery createSubqueriesForTables(ArrayList<String> tablesFromDB,
			String dbID) {
		SQLQuery sub = new SQLQuery();

		for (Column c : initialQuery.getAllColumns()) {
			if (tablesFromDB.contains(c.getAlias())) {
				sub.addOutputColumnIfNotExists(c.getAlias(), c.getName());
			}
		}
		/*
		 * for (Output o : initialQuery.outputs) { Operand op = o.getObject();
		 * if (o.getObject() instanceof Column) { Column c = (Column)
		 * o.getObject(); if (tablesFromDB.contains(c.tableAlias)) {
		 * //sub.outputColumns.add(c);
		 * sub.addOutputColumnIfNotExists(c.tableAlias, c.columnName); } } else
		 * { //if (o.getObject() instanceof Function) { // Function f =
		 * (Function) o.getObject(); for (Column c : op.getAllColumnRefs()) { if
		 * (tablesFromDB.contains(c.tableAlias)) {
		 * sub.addOutputColumnIfNotExists(c.tableAlias, c.columnName); } } } //}
		 * 
		 * }
		 */

		for (Table t : initialQuery.getInputTables()) {
			if (tablesFromDB.contains(t.getAlias())) {
				Table t2 = new Table();
				t2.setAlias(t.getAlias());
				if (dbID != null && !dbID.equals("")) {
					if (!t2.hasDBIdRemoved()) {
						t2.setName(t.getlocalName());
						t2.setDBIdRemoved();
					}
					sub.setFederated(true);
					sub.setMadisFunctionString(DBInfoReaderDB.dbInfo
							.getDB(dbID).getMadisString());
				} else {
					t2.setName(t.getName());
				}
				sub.getInputTables().add(t2);
			}
		}

		for (UnaryWhereCondition uwc : initialQuery.getUnaryWhereConditions()) {
			// if filter is on table of the DB, add filter to the subquery
			if (tablesFromDB.contains(uwc.getAllColumnRefs().get(0).getAlias())) {
				sub.getUnaryWhereConditions().add(uwc);
				// add temporary column if not exists
				sub.addOutputColumnIfNotExists(uwc.getAllColumnRefs().get(0)
						.getAlias(), uwc.getAllColumnRefs().get(0).getName());

			}
		}

		for (NonUnaryWhereCondition bwc : initialQuery
				.getBinaryWhereConditions()) {
			boolean allTablesBelong = true;

			// add temporary column if not exists
			for (Column c : bwc.getAllColumnRefs()) {

				if (tablesFromDB.contains(c.getAlias())) {
					sub.addOutputColumnIfNotExists(c.getAlias(), c.getName());

				} else {
					allTablesBelong = false;
				}
			}
			// if both left and right tables belong to DB add the join to
			// suquery
			if (allTablesBelong) {
				sub.getBinaryWhereConditions().add(bwc);
				this.remainingWhereConditions.remove(bwc);
			}
		}
		if (this.remainingWhereConditions.isEmpty()) {
			// we only have this subquery, add udf, groupby, orderby, limit and
			// rename output columns
			for (Output o : this.initialQuery.getOutputs()) {
				if (!(o.getObject() instanceof Column)) {
					sub.getOutputs().add(o);
				}
			}

			for (Column c : this.initialQuery.getGroupBy()) {
				sub.getGroupBy().add(c);
			}
			for (ColumnOrderBy c : this.initialQuery.getOrderBy()) {
				sub.getOrderBy().add(c);
			}
			sub.setLimit(this.initialQuery.getLimit());
			sub.setTemporary(false);

			sub.getOutputs().clear();
			for (Output initialOut : this.initialQuery.getOutputs()) {
				sub.getOutputs().add(initialOut);
			}
		}

		return sub;

	}

	private void makeSubqueryFinal(SQLQuery joinSubquery) {

		// add Output of the original quey and rename table aliases to be taken
		// from the temp subqueries
		joinSubquery.getOutputs().clear();
		for (Output o : this.initialQuery.getOutputs()) {
			Operand op = o.getObject();

			// rename column alias according to the initial query
			if (op instanceof Column) {
				Column initialOutCol = (Column) o.getObject();
				Column newOutput = new Column(initialOutCol.getAlias(),
						initialOutCol.getName());
				String tablename = c2t.getTablenameForColumn(newOutput);
				newOutput.setAlias(tablename);
				newOutput.setName(initialOutCol.getAlias() + "_"
						+ initialOutCol.getName());
				o.setObject(newOutput);
				joinSubquery.getOutputs().add(o);

			} else {
				// HashMap<Column, Column> columnsToChange = new HashMap<Column,
				// Column>();
				// for some strange reason program hangs in remote server when I
				// use HashMap
				List<Column> allRefs = op.getAllColumnRefs();
				Column[] olds = new Column[allRefs.size()];
				Column[] news = new Column[allRefs.size()];

				for (int i = 0; i < allRefs.size(); i++) {
					Column c = allRefs.get(i);
					// String tempTableName =
					// columnsToSubqueries.get(c).getResultTableName();
					olds[i] = c;
					news[i] = new Column(c2t.getTablenameForColumn(c),
							c.getAlias() + "_" + c.getName());
					// columnsToChange.put(c, new
					// Column(c2t.getTablenameForColumn(c), c.tableAlias + "_" +
					// c.columnName));
					// c.columnName = c.tableAlias + "_" + c.columnName;
					// c.tableAlias = tempTableName;
				}
				for (int i = 0; i < allRefs.size(); i++) {
					// Column old : columnsToChange.keySet()

					op.changeColumn(olds[i], news[i]);
				}

				joinSubquery.getOutputs().add(o);
			}
			// }

		}

		for (Column c : this.initialQuery.getGroupBy()) {
			if (c.getAlias() != null) {
				joinSubquery.getGroupBy().add(
						new Column(c2t.getTablenameForColumn(c), c.getAlias()
								+ "_" + c.getName()));
			} else {
				// table alias null
				joinSubquery.getGroupBy().add(c);
			}
		}
		for (ColumnOrderBy c : this.initialQuery.getOrderBy()) {
			if (c.getAlias() != null) {
				joinSubquery.getOrderBy().add(
						new ColumnOrderBy(c2t.getTablenameForColumn(c), c
								.getAlias() + "_" + c.getName(), c.isAsc));
			} else {
				// table alias null
				joinSubquery.getOrderBy().add(c);
			}
		}
		joinSubquery.setLimit(this.initialQuery.getLimit());
		joinSubquery.setTemporary(false);
		joinSubquery.setSelectAll(this.initialQuery.isSelectAll());
		joinSubquery.setOutputColumnsDistinct(this.initialQuery
				.getOutputColumnsDistinct());
	}

	private Node makeNodeFinal(Node n, NodeHashValues hashes) {
		Node tempParent = n;
		// boolean isCentralised = n.isCentralised();
		// Set<PartitionCols> partitioned = n.isPartitionedOn();

		if (!this.initialQuery.getGroupBy().isEmpty()) {
			if (DecomposerUtils.USE_GROUP_BY) {
				Node groupBy = new Node(Node.AND, Node.GROUPBY);
				groupBy.setObject(this.initialQuery.getGroupBy());
				Node table = new Node(Node.OR);
				Table t = new Table("table" + Util.createUniqueId(), null);
				table.setObject(t);
				table.addChild(groupBy);
				// table.setPartitionRecord(groupBy.getPartitionRecord());
				// table.setLastPartition(groupBy.getLastPartition());
				groupBy.addChild(n);
				groupBy.addAllDescendantBaseTables(n.getDescendantBaseTables());
				// groupBy.setPartitionRecord(n.getPartitionRecord());
				// groupBy.setPartitionRecord(n.getPartitionRecord());
				if (!hashes.containsKey(table.getHashId())) {
					hashes.put(table.getHashId(), table);
					table.addAllDescendantBaseTables(groupBy
							.getDescendantBaseTables());
				} else {
					table = hashes.get(table.getHashId());
				}
				tempParent = table;
			} else {
				log.error("GROUP BY not supported!");
			}
		}

		if (!this.initialQuery.getOrderBy().isEmpty()) {
			if (DecomposerUtils.USE_ORDER_BY) {
				Node orderBy = new Node(Node.AND, Node.ORDERBY);
				orderBy.setObject(this.initialQuery.getOrderBy());
				Node table = new Node(Node.OR);
				Table t = new Table("table" + Util.createUniqueId(), null);
				table.setObject(t);
				table.addChild(orderBy);
				// table.setPartitionRecord(orderBy.getPartitionRecord());
				// table.setLastPartition(orderBy.getLastPartition());
				orderBy.addChild(n);
				orderBy.addAllDescendantBaseTables(n.getDescendantBaseTables());
				// orderBy.setPartitionRecord(n.getPartitionRecord());
				// orderBy.setLastPartition(n.getLastPartition());
				if (!hashes.containsKey(table.getHashId())) {
					hashes.put(table.getHashId(), table);
					table.addAllDescendantBaseTables(orderBy
							.getDescendantBaseTables());
				} else {
					table = hashes.get(table.getHashId());
				}
				tempParent = table;
			} else {
				log.error("GROUP BY not supported!");
			}
		}
		if (!this.initialQuery.isSelectAll()) {
			Node projection = new Node(Node.AND, Node.PROJECT);
			Projection p = new Projection();
			for (Output o : this.initialQuery.getOutputs()) {
				// c2n.putColumnInTable(c, projection);
				// add projections
				p.addOperand(o);

			}
			if (p.getOperands().size() > 0) {
				p.setDistinct(initialQuery.isOutputColumnsDinstict());
				projection.setObject(p);
				projection.addChild(tempParent);
				// projection.setPartitionRecord(tempParent.getPartitionRecord());
				// projection.setLastPartition(tempParent.getLastPartition());
				if (!hashes.containsKey(projection.getHashId())) {
					hashes.put(projection.getHashId(), projection);
					projection.addAllDescendantBaseTables(tempParent
							.getDescendantBaseTables());
				} else {
					projection = hashes.get(projection.getHashId());
				}
				tempParent = projection;
			}

			/*
			 * else { Node n2 = hashes.get(projection.computeHashID());
			 * n2.addChild(tempParent); tempParent = n2; }
			 */
			Node projTable = new Node(Node.OR);
			projTable
					.setObject(new Table("table" + Util.createUniqueId(), null));
			projTable.addChild(tempParent);

			if (!hashes.containsKey(projTable.getHashId())) {
				hashes.put(projTable.getHashId(), projTable);
				projTable.addAllDescendantBaseTables(tempParent
						.getDescendantBaseTables());
			} else {
				projTable = hashes.get(projTable.getHashId());
			}
			tempParent = projTable;

		}

		if (this.initialQuery.getLimit() > -1) {
			Node limit = new Node(Node.AND, Node.LIMIT);
			limit.setObject(new Integer(this.initialQuery.getLimit()));
			limit.addChild(tempParent);
			// projection.setPartitionRecord(tempParent.getPartitionRecord());
			// projection.setLastPartition(tempParent.getLastPartition());
			if (!hashes.containsKey(limit.getHashId())) {
				hashes.put(limit.getHashId(), limit);
				limit.addAllDescendantBaseTables(tempParent
						.getDescendantBaseTables());
			} else {
				limit = hashes.get(limit.getHashId());
			}

			// isCentralised = tempParent.isCentralised();
			// partitioned = tempParent.isPartitionedOn();
			tempParent = limit;

			Node limitTable = new Node(Node.OR);
			limitTable.setObject(new Table("table" + Util.createUniqueId(),
					null));
			limitTable.addChild(tempParent);

			if (!hashes.containsKey(limitTable.getHashId())) {
				hashes.put(limitTable.getHashId(), limitTable);
				limitTable.addAllDescendantBaseTables(tempParent
						.getDescendantBaseTables());
			} else {
				limitTable = hashes.get(limitTable.getHashId());
			}
			tempParent = limitTable;
		}

		return tempParent;

		// joinSubquery.setLimit(this.initialQuery.getLimit());
		// joinSubquery.setTemporary(false);
		// joinSubquery.setSelectAll(this.initialQuery.isSelectAll());
		// joinSubquery.setOutputColumnsDistinct(this.initialQuery.getOutputColumnsDistinct());
	}

	private SQLQuery getTemporarySubquery(String tablename) {
		for (SQLQuery q : this.result) {
			if (q.getResultTableName().equals(tablename)) {
				return q;
			}
		}
		for (SQLQuery q : this.nestedSubqueries) {
			if (q.getResultTableName().equals(tablename)) {
				return q;
			}
		}
		return null;
	}

	// void addNestedSubquery(SQLQuery s) {
	// this.nestedSubqueries.add(s);
	// }
	private void renameOutputColumnsInNestedSubs() {
		for (SQLQuery next : this.initialQuery.getNestedSubqueries()) {
			for (Output o : next.getOutputs()) {
				// change output in order by and group by
				for (Column ob : next.getOrderBy()) {
					if (ob.getName().equals(o.getOutputName())) {
						ob.changeColumn(
								ob,
								new Column(null, this.initialQuery
										.getNestedSubqueryAlias(next)
										+ "_"
										+ o.getOutputName()));
					}
				}
				for (Column ob : next.getGroupBy()) {
					if (ob.getName().equals(o.getOutputName())) {
						ob.setName(this.initialQuery
								.getNestedSubqueryAlias(next)
								+ "_"
								+ o.getOutputName());
					}
				}
				o.setOutputName(this.initialQuery.getNestedSubqueryAlias(next)
						+ "_" + o.getOutputName());
			}
		}
	}

	Set<Join> getJoins() {
		return lj;
	}

	List<Table> getInputTables() {
		return this.initialQuery.getInputTables();
	}
}
