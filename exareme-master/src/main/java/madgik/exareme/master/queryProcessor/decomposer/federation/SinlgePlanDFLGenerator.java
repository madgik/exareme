/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.DecomposerUtils;
import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.dag.ResultList;
import madgik.exareme.master.queryProcessor.decomposer.query.*;
import madgik.exareme.utils.properties.AdpDBProperties;

import java.util.*;

import com.google.common.hash.HashCode;

/**
 * @author dimitris
 */
public class SinlgePlanDFLGenerator {

	private Node root;
	private int partitionNo;
	private Memo memo;
	private Map<HashCode, madgik.exareme.common.schema.Table> registry;
	private final boolean useCache = AdpDBProperties.getAdpDBProps()
			.getBoolean("db.cache");

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(SinlgePlanDFLGenerator.class);

	SinlgePlanDFLGenerator(Node n, int partNo, Memo m,
			Map<HashCode, madgik.exareme.common.schema.Table> r) {
		this.root = n;
		this.partitionNo = partNo;
		this.memo = m;
		this.registry = r;
	}

	public ResultList generate() {
		ResultList qs = new ResultList();
		MemoKey rootkey = new MemoKey(root, null);

		// printPlan(rootkey);
		qs.setCurrent(new SQLQuery());
		if (partitionNo == 1) {
			combineOperatorsAndOutputQueriesCentralized(rootkey, qs,
					new HashMap<MemoKey, SQLQuery>());
		} else if(DecomposerUtils.PUSH_PROCESSING){
			combineOperatorsAndOutputQueriesPush(rootkey, qs,
					new HashMap<MemoKey, SQLQuery>());
		}
		else{
			combineOperatorsAndOutputQueries(rootkey, qs,
					new HashMap<MemoKey, SQLQuery>());
		}

		// for (SQLQuery q : qs) {
		// System.out.println(q.toDistSQL() + "\n");
		// }

		if (qs.isEmpty()) {
			if (useCache && registry.containsKey(rootkey.getNode().getHashId())) {
				String tableInCache = registry.get(
						rootkey.getNode().getHashId()).getName();
				SQLQuery res = new SQLQuery();
				res.addInputTable(new Table(tableInCache, tableInCache));
				res.setExistsInCache(true);
				qs.add(res);
			} else {
				log.error("Decomposer produced empty result");
			}
		} /*
		 * else {
		 * 
		 * for (int i = 0; i < qs.size(); i++) { if (qs.get(i).existsInCache())
		 * { qs.remove(i); i--; } } }
		 */

		/*
		 * for (int i = 0; i < qs.size(); i++) { SQLQuery q = qs.get(i); if
		 * (q.getOutputs().isEmpty()) { for (Table t : q.getInputTables()) {
		 * boolean found = false; for (int j = 0; j < i; j++) { SQLQuery q2 =
		 * qs.get(j); if (q2.getTemporaryTableName().equals(t.getName())) { for
		 * (Output o2 : q2.getOutputs()) { q.addOutput(t.getAlias(),
		 * o2.getOutputName()); } found = true; break; } } // find Outputs from
		 * registry if (!found) { for (madgik.exareme.common.schema.Table table
		 * : registry.values()) { if (table.getName().equals(t.getName())) {
		 * String schema = table.getSqlDefinition(); schema =
		 * schema.substring(schema.indexOf("(")); schema = schema.replace(")",
		 * ""); String[] outputs = schema.split(" "); for (int k = 0; i <
		 * outputs.length; i++) { String output = outputs[k]; if
		 * (!output.equals(" ") && !output.toUpperCase().equals("INTEGER") &&
		 * !output.toUpperCase().equals("INT") &&
		 * !output.toUpperCase().equals("NUM") &&
		 * !output.toUpperCase().equals("REAL") &&
		 * !output.toUpperCase().equals("BLOB") &&
		 * !output.toUpperCase().equals("NULL") &&
		 * !output.toUpperCase().equals("NUMERIC") &&
		 * !output.toUpperCase().equals("TEXT")) { q.addOutput(t.getAlias(),
		 * output); } } found = true; break; } } } if(!found){
		 * log.error("Input table not found:"+t.getName()); }
		 * 
		 * } } }
		 */

		// remove not needed columns from nested subqueries
		
		if (DecomposerUtils.REMOVE_OUTPUTS) {
			for (int i = 0; i < qs.size(); i++) {
				SQLQuery q = qs.get(i);
				if (q.isNested() && qs.size() > 1) {
					List<Column> outputs = new ArrayList<Column>();
					for (Output o : q.getOutputs()) {
						outputs.add(new Column(q.getTemporaryTableName(), o
								.getOutputName()));
					}
					for (int j = i + 1; j < qs.size(); j++) {
						List<Column> cols = qs.get(j).getAllColumns();
						for (Column c2 : cols) {
							outputs.remove(new Column(c2.getAlias(), c2
									.getBaseTable() + "_" + c2.getName()));
						}
						if (outputs.isEmpty()) {
							break;
						}
					}
					for (Column c3 : outputs) {
						for (int k = 0; k < q.getOutputs().size(); k++) {
							Output o = q.getOutputs().get(k);
							if (o.getOutputName().equals(c3.getName())) {
								q.getOutputs().remove(k);
								q.setHashId(null);
								k--;
							}
						}
					}

				}
			}
		}
		return qs;
	}

	private void printPlan(MemoKey key) {
		if (this.memo.containsMemoKey(key)) {
			System.out.println("memo key: " + key);

			System.out.println("mater::"
					+ memo.getMemoValue(key).isMaterialised());
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
				System.out.println("repAfter:"
						+ memo.getMemoValue(key).getPlan()
								.getRepartitionAfterOp(i));
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

	private void combineOperatorsAndOutputQueries(MemoKey k,
			ResultList tempResult, HashMap<MemoKey, SQLQuery> visited) {

		SQLQuery current = tempResult.getCurrent();
		MemoValue v = memo.getMemoValue(k);
		SinglePlan p = v.getPlan();
		Node e = k.getNode();

		if (useCache && registry.containsKey(e.getHashId())
				&& e.getHashId() != null) {
			String tableInRegistry = registry.get(e.getHashId()).getName();
			Table t = new Table(tableInRegistry, tableInRegistry);
			tempResult.setLastTable(t);
			// tempResult.trackBaseTableFromQuery(t.getAlias(), t.getAlias());
			SQLQuery cached = new SQLQuery();
			cached.setTemporaryTableName(tableInRegistry);
			visited.put(k, cached);
			cached.setHashId(e.getHashId());
			// current.setTemporaryTableName(tableInRegistry);
			current.setExistsInCache(true);

			for (String alias : e.getDescendantBaseTables()) {
				tempResult.trackBaseTableFromQuery(alias, tableInRegistry);
			}

			return;
		}

		if (visited.containsKey(k) && visited.get(k).isMaterialised()) {
			tempResult.setLastTable(visited.get(k));

			tempResult.remove(current);
			for (String alias : e.getDescendantBaseTables()) {
				tempResult.trackBaseTableFromQuery(alias, tempResult
						.getLastTable().getAlias());
			}
			return;
		}

		if (!e.getObject().toString().startsWith("table")) {
			Table t = (Table) k.getNode().getObject();
			tempResult.setLastTable(t);
			visited.put(k, current);
			
			if (memo.getMemoValue(k).isMaterialised()) {
				current.setMaterialised(true);
				current.setHashId(e.getHashId());
			}
			tempResult.trackBaseTableFromQuery(t.getAlias(), t.getAlias());
			return;
		}
		Column repBefore=null;
		if(p.getRepartitionBeforeOp()!=null){
			repBefore = new Column(null, p.getRepartitionBeforeOp().getName(), p.getRepartitionBeforeOp().getAlias());
		}
		Node op = e.getChildAt(p.getChoice());
		SQLQuery old = current;
		if (memo.getMemoValue(k).isMaterialised()) {

			SQLQuery q2 = new SQLQuery();
			q2.setHashId(e.getHashId());
			tempResult.setCurrent(q2);
			current = q2;
			current.setMaterialised(true);
			visited.put(k, current);
		}

		if (repBefore != null) {
			current.setRepartition(repBefore, partitionNo);
			current.setPartition(repBefore);
		}

		if (op.getOpCode() == Node.PROJECT
				|| op.getOpCode() == Node.BASEPROJECT) {
			String inputName;

			Projection prj = (Projection) op.getObject();
			// current = q2;
			// q2.setPartition(repAfter, partitionNo);

			for (Output o : prj.getOperands()) {
				try {
					current.getOutputs().add(o.clone());
				} catch (CloneNotSupportedException e1) {
					log.warn("could not clone output", e1);
				}
			}
			current.setOutputColumnsDinstict(prj.isDistinct());
			// current.getOutputs().addAll(prj.getOperands());

			combineOperatorsAndOutputQueries(p.getInputPlan(0), tempResult,
					visited);
			// visited.put(p.getInputPlan(j), q2);

			if (op.getOpCode() == Node.PROJECT) {
				// if we have a final result, remove previous projections
				for (int l = 0; l < current.getOutputs().size(); l++) {
					Output o = current.getOutputs().get(l);
					boolean exists = false;
					for (Output pr : prj.getOperands()) {
						if (pr.getOutputName().equals(o.getOutputName())) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						current.getOutputs().remove(o);
						l--;
					}
				}
			}
			if (tempResult.getLastTable() != null) {
				inputName = tempResult.getLastTable().getAlias();
				if (inputName != current.getTemporaryTableName()
						&& !current.getInputTables().contains(
								tempResult.getLastTable())) {
					current.addInputTable(tempResult.getLastTable());
					for (Output o : current.getOutputs()) {
						for (Column c : o.getObject().getAllColumnRefs()) {
							{
								if (op.isDescendantOfBaseTable(c.getAlias())) {
									o.getObject().changeColumn(
											c,
											new Column(tempResult
													.getQueryForBaseTable(c
															.getAlias()), c
													.getName(), c.getAlias()));
								}
							}
						}

					}
				}

			}

		} else if (op.getOpCode() == Node.JOIN) {
			NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) op
					.getObject();
			NonUnaryWhereCondition nuwcCloned = new NonUnaryWhereCondition(
					nuwc.getLeftOp(), nuwc.getRightOp(), nuwc.getOperator());
			current.addBinaryWhereCondition(nuwcCloned);
			List<String> inputNames = new ArrayList<String>();
			for (int j = 0; j < op.getChildren().size(); j++) {

				combineOperatorsAndOutputQueries(p.getInputPlan(j), tempResult,
						visited);
				inputNames.add(tempResult.getLastTable().getAlias());
				if (tempResult.getLastTable().getAlias() != current
						.getTemporaryTableName()
						&& !current.getInputTables().contains(
								tempResult.getLastTable())) {
					current.addInputTable(tempResult.getLastTable());
					addOutputs(current, tempResult.getLastTable().getAlias(),
							tempResult);
					for (Output o : current.getOutputs()) {
						for (Column c : o.getObject().getAllColumnRefs()) {
							{
								if (op.getChildAt(j).isDescendantOfBaseTable(
										c.getAlias())) {
									o.getObject().changeColumn(
											c,
											new Column(tempResult
													.getQueryForBaseTable(c
															.getAlias()), c
													.getName(), c.getAlias()));
								}
							}
						}

					}
				}
			}

			for (NonUnaryWhereCondition bwc : current
					.getBinaryWhereConditions()) {

				for (int j = 0; j < op.getChildren().size(); j++) {
					for (Operand o : bwc.getOperands()) {

						List<Column> cs = o.getAllColumnRefs();
						if (!cs.isEmpty()) {
							// not constant
							Column c = cs.get(0);
							if (op.getChildAt(j).isDescendantOfBaseTable(
									c.getAlias())) {
								bwc.changeColumn(
										c,
										new Column(tempResult
												.getQueryForBaseTable(c
														.getAlias()), c
												.getName(), c.getAlias()));
							}
						}

					}
				}
			}
			// }

			/*
			 * for (Output o : current.getOutputs()) { for (Column c :
			 * o.getObject().getAllColumnRefs()) { { for (int j = 0; j <
			 * op.getChildren().size(); j++) { Node child = op.getChildAt(j); if
			 * (child.isDescendantOfBaseTable(c.getAlias())) {
			 * o.getObject().changeColumn(c, new Column(inputNames.get(j),
			 * c.getName())); } } } // o.getObject().changeColumn(c, c); } }
			 */
			// tempResult.add(current);
		} else if (op.getOpCode() == Node.UNION
				|| op.getOpCode() == Node.UNIONALL) {
			List<SQLQuery> unions = new ArrayList<SQLQuery>();
			if (e.getParents().isEmpty()) {
				current.setHashId(e.getHashId());
			}
			boolean existsPartitioned = false;
			boolean existsNonPartitioned = false;
			for (int l = 0; l < op.getChildren().size(); l++) {
				SQLQuery u = new SQLQuery();

				// visited.put(op, current);
				tempResult.setCurrent(u);
				combineOperatorsAndOutputQueries(p.getInputPlan(l), tempResult,
						visited);
				// visited.put(p.getInputPlan(l), u);
				if (memo.getMemoValue(p.getInputPlan(l)).isMaterialised()) {
					u = tempResult.get(tempResult.getLastTable().getAlias());
					if (u == null && useCache) {
						// it's a table from cache
						u = new SQLQuery();
						u.addInputTable(tempResult.getLastTable());
						u.setExistsInCache(true);
						tempResult.add(u);
					}
				} else {
					// visited.put(p.getInputPlan(l), u);
					tempResult.add(u);
				}

				u.setHashId(p.getInputPlan(l).getNode().getHashId());
				if (useCache && u.existsInCache()) {
					String tableInCache = registry.get(u.getHashId()).getName();
					tempResult.remove(u);
					u = new SQLQuery();
					if (op.getChildren().size() == 1) {
						u.addInputTable(new Table(tableInCache, tableInCache));
						u.setExistsInCache(true);
						tempResult.add(u);
						return;
					}
					u.setTemporaryTableName(tableInCache);
					// u.addInputTable(new Table(tableInCache, tableInCache));
				}
				unions.add(u);
				if (u.getPartition() != null) {
					existsPartitioned = true;
				} else {
					existsNonPartitioned = true;
				}
			}
			if (unions.size() > 1) {
				tempResult.add(current);
				current.setUnionqueries(unions);
				if (op.getOpCode() == Node.UNIONALL) {
					current.setUnionAll(true);
				}
				visited.put(k, current);
				current.setHashId(e.getHashId());
				if (memo.getMemoValue(k).isMaterialised()) {
					current.setMaterialised(true);
				}
				if (existsNonPartitioned && existsPartitioned) {
					// make all unions have the same number of partitions
					for (SQLQuery u : unions) {
						if (u.getPartition() == null) {
							Column pt = new Column(u.getOutputs().get(0)
									.getOutputName(), u.getOutputs().get(0)
									.getOutputName());
							u.setRepartition(pt, partitionNo);
							u.setPartition(pt);
						}

					}
				}
			} else {
				visited.put(k, unions.get(0));
				unions.get(0)
						.setHashId(p.getInputPlan(0).getNode().getHashId());
				if (memo.getMemoValue(k).isMaterialised()) {
					unions.get(0).setMaterialised(true);
				}
			}
		} else if (op.getOpCode() == Node.SELECT) {
			combineOperatorsAndOutputQueries(p.getInputPlan(0), tempResult,
					visited);
			String inputName = tempResult.getLastTable().getAlias();
			Selection s = (Selection) op.getObject();
			Iterator<Operand> it = s.getOperands().iterator();
			while (it.hasNext()) {
				Operand o = it.next();
				if (o instanceof UnaryWhereCondition) {
					UnaryWhereCondition uwc = (UnaryWhereCondition) o;
					UnaryWhereCondition uwcCloned = new UnaryWhereCondition(
							uwc.getType(), uwc.getOperand(), uwc.getNot(),
							uwc.getObject());
					if (!inputName.equals(current.getTemporaryTableName())) {
						Column c = uwcCloned.getAllColumnRefs().get(0);
						uwcCloned
								.changeColumn(
										c,
										new Column(tempResult
												.getQueryForBaseTable(c
														.getAlias()), c
												.getName(), c.getAlias()));
					}
					current.addUnaryWhereCondition(uwcCloned);
				} else {
					NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o;
					NonUnaryWhereCondition nuwcCloned = new NonUnaryWhereCondition(
							nuwc.getLeftOp(), nuwc.getRightOp(),
							nuwc.getOperator());
					if (!inputName.equals(current.getTemporaryTableName())) {
						for (Column c : nuwcCloned.getAllColumnRefs()) {
							nuwcCloned.changeColumn(
									c,
									new Column(
											tempResult.getQueryForBaseTable(c
													.getAlias()), c.getName(),
											c.getAlias()));
						}
					}
					current.addBinaryWhereCondition(nuwcCloned);
				}
			}

			// visited.put(p.getInputPlan(j), q2);
			if (tempResult.getLastTable() != null) {
				inputName = tempResult.getLastTable().getAlias();
				if (inputName != current.getTemporaryTableName()
						&& !current.getInputTables().contains(
								tempResult.getLastTable())) {
					current.addInputTable(tempResult.getLastTable());
					addOutputs(current, tempResult.getLastTable().getAlias(),
							tempResult);
					for (Output o : current.getOutputs()) {
						for (Column c : o.getObject().getAllColumnRefs()) {
							{
								if (op.isDescendantOfBaseTable(c.getAlias())) {
								o.getObject().changeColumn(
										c,
										new Column(tempResult
												.getQueryForBaseTable(c
														.getAlias()), c
												.getName(), c.getAlias()));
							}
						}
						}

					}
				}

			}

		} else if (op.getOpCode() == Node.LIMIT) {
			current.setLimit(((Integer) op.getObject()).intValue());
			current.setHashId(p.getInputPlan(0).getNode().getHashId());
			combineOperatorsAndOutputQueries(p.getInputPlan(0), tempResult,
					visited);
			if (current.getInputTables().isEmpty()) {
				// limit of a query that exists in the cache
				current.addInputTable(tempResult.getLastTable());
			}
			if (tempResult.isEmpty() && e == this.root) {
				// final limit of a table tha exists in cache
				tempResult.add(current);
			}
		} else if (op.getOpCode() == Node.NESTED) {
			// nested is always materialized
			current.setNested(true);
			combineOperatorsAndOutputQueries(p.getInputPlan(0), tempResult,
					visited);

		} else {
			log.error("Unknown Operator in DAG");
		}
		current.setExistsInCache(false);
		if (memo.getMemoValue(k).isMaterialised()) {
			tempResult.add(current);
			tempResult.setLastTable(current);
			current.setHashId(e.getHashId());
			for (String alias : e.getDescendantBaseTables()) {
				tempResult.trackBaseTableFromQuery(alias, tempResult
						.getLastTable().getAlias());
			}
			tempResult.setCurrent(old);
		}
		if (old != current && old.getRepartition() == null) {
			old.setPartition(current.getPartitionColumn());
		}

	}

	private void combineOperatorsAndOutputQueriesCentralized(MemoKey k,
			ResultList tempResult, HashMap<MemoKey, SQLQuery> visited) {

		SQLQuery current = tempResult.getCurrent();
		MemoValue v = memo.getMemoValue(k);

		SinglePlan p = v.getPlan();
		Node e = k.getNode();

		if (useCache && registry.containsKey(e.getHashId())
				&& e.getHashId() != null) {
			String tableInRegistry = registry.get(e.getHashId()).getName();
			Table t = new Table(tableInRegistry, tableInRegistry);
			tempResult.setLastTable(t);
			// tempResult.trackBaseTableFromQuery(t.getAlias(), t.getAlias());
			SQLQuery cached = new SQLQuery();
			cached.setTemporaryTableName(tableInRegistry);
			visited.put(k, cached);
			cached.setHashId(e.getHashId());
			// current.setTemporaryTableName(tableInRegistry);
			current.setExistsInCache(true);

			for (String alias : e.getDescendantBaseTables()) {
				tempResult.trackBaseTableFromQuery(alias, tableInRegistry);
			}

			// tempResult.remove(current);
			// tempResult.getCurrent().setExistsInCache(true);
			// current.setHashId(e.getHashId());
			// if (memo.getMemoValue(k).isMaterialised()) {
			// current.setMaterialised(true);
			// }
			/*
			 * if(e.getFirstParent()!=null &&
			 * e.getFirstParent().getType()==Node.UNION ||
			 * e.getFirstParent().getType()==Node.UNIONALL){ SQLQuery star=new
			 * SQLQuery(); star.addInputTable(t); star.setHashId(0); }
			 */
			return;
		}

		if (visited.containsKey(k) && visited.get(k).isMaterialised()) {
			tempResult.setLastTable(visited.get(k));
			tempResult.remove(current);
			for (String alias : e.getDescendantBaseTables()) {
				tempResult.trackBaseTableFromQuery(alias, tempResult
						.getLastTable().getAlias());
			}
			return;
		}

		if (!e.getObject().toString().startsWith("table")) {
			Table t = (Table) k.getNode().getObject();
			tempResult.setLastTable(t);
			if (t.isFederated()) {
				visited.put(k, current);
				if (memo.getMemoValue(k).isMaterialised()) {
					current.setMaterialised(true);
					current.setHashId(e.getHashId());
				}
			}

			tempResult.trackBaseTableFromQuery(t.getAlias(), t.getAlias());

			return;
		}

		Node op = e.getChildAt(p.getChoice());
		SQLQuery old = current;
		if (memo.getMemoValue(k).isMaterialised()) {

			SQLQuery q2 = new SQLQuery();
			q2.setHashId(e.getHashId());

			tempResult.setCurrent(q2);
			current = q2;
			current.setMaterialised(true);
			visited.put(k, current);
		}

		if (op.getOpCode() == Node.PROJECT
				|| op.getOpCode() == Node.BASEPROJECT) {
			String inputName;

			Projection prj = (Projection) op.getObject();
			// current = q2;
			// q2.setPartition(repAfter, partitionNo);

			// }
			for (Output o : prj.getOperands()) {
				try {
					current.getOutputs().add(o.clone());
				} catch (CloneNotSupportedException e1) {
					log.warn("could not clone output", e1);
				}
			}
			current.setOutputColumnsDinstict(prj.isDistinct());
			// current.getOutputs().addAll(prj.getOperands());

			combineOperatorsAndOutputQueriesCentralized(p.getInputPlan(0),
					tempResult, visited);

			if (op.getOpCode() == Node.PROJECT) {
				// if we have a final result, remove previous projections
				for (int l = 0; l < current.getOutputs().size(); l++) {
					Output o = current.getOutputs().get(l);
					boolean exists = false;
					for (Output pr : prj.getOperands()) {
						if (pr.getOutputName().equals(o.getOutputName())) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						current.getOutputs().remove(o);
						l--;
					}
				}
			}
			// if (!current.getOutputs().isEmpty()) {
			// else do not add base table projections, c

			// visited.put(p.getInputPlan(j), q2);
			if (tempResult.getLastTable() != null) {
				inputName = tempResult.getLastTable().getAlias();
				if (inputName != current.getTemporaryTableName()
						&& !current.getInputTables().contains(
								tempResult.getLastTable())) {
					current.addInputTable(tempResult.getLastTable());

					for (Output o : current.getOutputs()) {
						for (Column c : o.getObject().getAllColumnRefs()) {
							{
								if (op.isDescendantOfBaseTable(c.getAlias())) {
									o.getObject().changeColumn(
											c,
											new Column(tempResult
													.getQueryForBaseTable(c
															.getAlias()), c
													.getName(), c.getAlias()));
								}
							}
						}

					}
				}

			}

		} else if (op.getOpCode() == Node.JOIN) {
			NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) op
					.getObject();
			NonUnaryWhereCondition nuwcCloned = new NonUnaryWhereCondition(
					nuwc.getLeftOp(), nuwc.getRightOp(), nuwc.getOperator());
			current.addBinaryWhereCondition(nuwcCloned);
			List<String> inputNames = new ArrayList<String>();
			for (int j = 0; j < op.getChildren().size(); j++) {

				combineOperatorsAndOutputQueriesCentralized(p.getInputPlan(j),
						tempResult, visited);
				inputNames.add(tempResult.getLastTable().getAlias());
				if (tempResult.getLastTable().getAlias() != current
						.getTemporaryTableName()
						&& !current.getInputTables().contains(
								tempResult.getLastTable())) {
					current.addInputTable(tempResult.getLastTable());
					addOutputs(current, tempResult.getLastTable().getAlias(),
							tempResult);
					for (Output o : current.getOutputs()) {
						for (Column c : o.getObject().getAllColumnRefs()) {
							{
								if (op.getChildAt(j).isDescendantOfBaseTable(
										c.getAlias())) {
									o.getObject().changeColumn(
											c,
											new Column(tempResult
													.getQueryForBaseTable(c
															.getAlias()), c
													.getName(), c.getAlias()));
									// addOutputIfNotExists(c, tempResult);
								}
							}
						}

					}
				}
			}

			for (NonUnaryWhereCondition bwc : current
					.getBinaryWhereConditions()) {

				for (int j = 0; j < op.getChildren().size(); j++) {
					for (Operand o : bwc.getOperands()) {

						List<Column> cs = o.getAllColumnRefs();
						if (!cs.isEmpty()) {
							// not constant
							Column c = cs.get(0);

							if (op.getChildAt(j).isDescendantOfBaseTable(
									c.getAlias())) {
								bwc.changeColumn(
										c,
										new Column(tempResult
												.getQueryForBaseTable(c
														.getAlias()), c
												.getName(), c.getAlias()));
								// addOutputIfNotExists(c, tempResult);
							}
						}

					}
				}
			}
			// }

			/*
			 * for (Output o : current.getOutputs()) { for (Column c :
			 * o.getObject().getAllColumnRefs()) { { for (int j = 0; j <
			 * op.getChildren().size(); j++) { Node child = op.getChildAt(j); if
			 * (child.isDescendantOfBaseTable(c.getAlias())) {
			 * o.getObject().changeColumn(c, new Column(inputNames.get(j),
			 * c.getName())); } } } // o.getObject().changeColumn(c, c); } }
			 */
			// tempResult.add(current);
		} else if (op.getOpCode() == Node.UNION
				|| op.getOpCode() == Node.UNIONALL) {
			List<SQLQuery> unions = new ArrayList<SQLQuery>();
			if (e.getParents().isEmpty()) {
				current.setHashId(e.getHashId());
			}
			for (int l = 0; l < op.getChildren().size(); l++) {
				SQLQuery u = new SQLQuery();

				// visited.put(op, current);
				tempResult.setCurrent(u);
				combineOperatorsAndOutputQueriesCentralized(p.getInputPlan(l),
						tempResult, visited);
				if (memo.getMemoValue(p.getInputPlan(l)).isMaterialised()) {
					u = tempResult.get(tempResult.getLastTable().getAlias());
					if (u == null && useCache) {
						// it's a table from cache
						u = new SQLQuery();
						u.addInputTable(tempResult.getLastTable());
						u.setExistsInCache(true);
						tempResult.add(u);
					}
				} else {
					// visited.put(p.getInputPlan(l), u);
					tempResult.add(u);
				}
				u.setHashId(p.getInputPlan(l).getNode().getHashId());
				if (useCache && u.existsInCache()) {
					String tableInCache = registry.get(u.getHashId()).getName();
					tempResult.remove(u);
					u = new SQLQuery();
					if (op.getChildren().size() == 1) {
						u.addInputTable(new Table(tableInCache, tableInCache));
						u.setExistsInCache(true);
						tempResult.add(u);
						return;
					}
					u.setTemporaryTableName(tableInCache);
					// u.addInputTable(new Table(tableInCache, tableInCache));
				}
				unions.add(u);

			}
			if (op.getChildren().size() > 1) {
				tempResult.add(current);
				current.setUnionqueries(unions);
				if (op.getOpCode() == Node.UNIONALL) {
					current.setUnionAll(true);
				}
				visited.put(k, current);
				current.setHashId(e.getHashId());
				if (memo.getMemoValue(k).isMaterialised()) {
					current.setMaterialised(true);
				}
			} else {
				visited.put(k, unions.get(0));
				unions.get(0)
						.setHashId(p.getInputPlan(0).getNode().getHashId());
				if (memo.getMemoValue(k).isMaterialised()) {
					unions.get(0).setMaterialised(true);
				}
			}

		} else if (op.getOpCode() == Node.SELECT) {
			combineOperatorsAndOutputQueriesCentralized(p.getInputPlan(0),
					tempResult, visited);
			String inputName = tempResult.getLastTable().getAlias();
			Selection s = (Selection) op.getObject();
			Iterator<Operand> it = s.getOperands().iterator();
			while (it.hasNext()) {
				Operand o = it.next();
				if (o instanceof UnaryWhereCondition) {
					UnaryWhereCondition uwc = (UnaryWhereCondition) o;
					UnaryWhereCondition uwcCloned = new UnaryWhereCondition(
							uwc.getType(), uwc.getOperand(), uwc.getNot(),
							uwc.getObject());
					if (!inputName.equals(current.getTemporaryTableName())) {
						Column c = uwcCloned.getAllColumnRefs().get(0);
						if (op.isDescendantOfBaseTable(c.getAlias())) {
							uwcCloned.changeColumn(
									c,
									new Column(
											tempResult.getQueryForBaseTable(c
													.getAlias()), c.getName(),
											c.getAlias()));
							// addOutputIfNotExists(c, tempResult);
						}
					}
					current.addUnaryWhereCondition(uwcCloned);
				} else {
					NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o;
					NonUnaryWhereCondition nuwcCloned = new NonUnaryWhereCondition(
							nuwc.getLeftOp(), nuwc.getRightOp(),
							nuwc.getOperator());
					if (!inputName.equals(current.getTemporaryTableName())) {
						for (Column c : nuwcCloned.getAllColumnRefs()) {
							if (op.isDescendantOfBaseTable(c.getAlias())) {
								nuwcCloned.changeColumn(
										c,
										new Column(tempResult
												.getQueryForBaseTable(c
														.getAlias()), c
												.getName(), c.getAlias()));
								// addOutputIfNotExists(c, tempResult);
							}
						}
					}
					current.addBinaryWhereCondition(nuwcCloned);
				}
			}

			// visited.put(p.getInputPlan(j), q2);
			if (tempResult.getLastTable() != null) {
				inputName = tempResult.getLastTable().getAlias();
				if (inputName != current.getTemporaryTableName()
						&& !current.getInputTables().contains(
								tempResult.getLastTable())) {
					current.addInputTable(tempResult.getLastTable());
					addOutputs(current, tempResult.getLastTable().getAlias(),
							tempResult);
					for (Output o : current.getOutputs()) {
						for (Column c : o.getObject().getAllColumnRefs()) {
							{
								if (op.isDescendantOfBaseTable(c.getAlias())) {
									o.getObject().changeColumn(
											c,
											new Column(tempResult
													.getQueryForBaseTable(c
															.getAlias()), c
													.getName(), c.getAlias()));
									// addOutputIfNotExists(c, tempResult);
								}
							}
						}

					}
				}

			}

		} else if (op.getOpCode() == Node.LIMIT) {
			current.setLimit(((Integer) op.getObject()).intValue());
			current.setHashId(p.getInputPlan(0).getNode().getHashId());
			combineOperatorsAndOutputQueriesCentralized(p.getInputPlan(0),
					tempResult, visited);
			if (current.getInputTables().isEmpty()) {
				// limit of a query that exists in the cache
				current.addInputTable(tempResult.getLastTable());
			}
			if (tempResult.isEmpty() && e == this.root) {
				// final limit of a table tha exists in cache
				tempResult.add(current);
			}
		} else if (op.getOpCode() == Node.NESTED) {
			// nested is always materialized
			current.setNested(true);
			combineOperatorsAndOutputQueriesCentralized(p.getInputPlan(0),
					tempResult, visited);

		} else {
			log.error("Unknown Operator in DAG");
		}
		current.setExistsInCache(false);
		if (memo.getMemoValue(k).isMaterialised()) {
			// if (!current.existsInCache()) {
			tempResult.add(current);
			tempResult.setLastTable(current);
			// }
			current.setHashId(e.getHashId());
			for (String alias : e.getDescendantBaseTables()) {
				tempResult.trackBaseTableFromQuery(alias, tempResult
						.getLastTable().getAlias());
			}
			tempResult.setCurrent(old);
		}

	}

	private void addOutputs(SQLQuery current, String inputName, ResultList list) {
		SQLQuery q = list.get(inputName);
		log.debug("adding Output to:" + current.getTemporaryTableName());

		if (q != null) {
			log.debug("from q:" + q.getTemporaryTableName());
			for (Output o : q.getOutputs()) {
				log.debug("out alias:" + o.getOutputName());
				if (!current.getOutputAliases().contains(o.getOutputName())) {
					current.addOutput(inputName, o.getOutputName());
				}
			}
		} else {
			for (madgik.exareme.common.schema.Table table : registry.values()) {
				if (table.getName().equals(inputName)) {
					String schema = table.getSqlDefinition();
					log.debug("table in cache: " + schema);
					if (schema.contains("(")) {
						schema = schema.substring(schema.indexOf("(") + 1);
					}
					schema = schema.replace(")", "");
					schema = schema.replaceAll(",", " ");
					schema = schema.replaceAll("\n", " ");
					String[] outputs = schema.split(" ");
					for (int m = 0; m < outputs.length; m++) {
						String output = outputs[m];
						if (!output.equals(" ") && !output.isEmpty()
								&& !output.toUpperCase().equals("INTEGER")
								&& !output.toUpperCase().equals("INT")
								&& !output.toUpperCase().equals("NUM")
								&& !output.toUpperCase().equals("REAL")
								&& !output.toUpperCase().equals("BLOB")
								&& !output.toUpperCase().equals("NULL")
								&& !output.toUpperCase().equals("NUMERIC")
								&& !output.toUpperCase().equals("TEXT")) {
							if (!current.getOutputAliases().contains(output)) {
								current.addOutput(inputName, output);
							}
						}
					}
				}

			}
		}
	}
	
	
	private void combineOperatorsAndOutputQueriesPush(MemoKey k,
			ResultList tempResult, HashMap<MemoKey, SQLQuery> visited) {

		SQLQuery current = tempResult.getCurrent();
		MemoValue v = memo.getMemoValue(k);
		SinglePlan p = v.getPlan();
		Node e = k.getNode();
		
		if (useCache && registry.containsKey(e.getHashId())
				&& e.getHashId() != null) {
			String tableInRegistry = registry.get(e.getHashId()).getName();
			Table t = new Table(tableInRegistry, tableInRegistry);
			tempResult.setLastTable(t);
			// tempResult.trackBaseTableFromQuery(t.getAlias(), t.getAlias());
			SQLQuery cached = new SQLQuery();
			cached.setTemporaryTableName(tableInRegistry);
			visited.put(k, cached);
			cached.setHashId(e.getHashId());
			// current.setTemporaryTableName(tableInRegistry);
			current.setExistsInCache(true);

			for (String alias : e.getDescendantBaseTables()) {
				tempResult.trackBaseTableFromQuery(alias, tableInRegistry);
			}

			return;
		}

		if (visited.containsKey(k) && visited.get(k).isMaterialised()) {
			tempResult.setLastTable(visited.get(k));

			tempResult.remove(current);
			for (String alias : e.getDescendantBaseTables()) {
				tempResult.trackBaseTableFromQuery(alias, tempResult
						.getLastTable().getAlias());
			}
			return;
		}

		if (!e.getObject().toString().startsWith("table")) {
			Table t = (Table) k.getNode().getObject();
			tempResult.setLastTable(t);
			visited.put(k, current);
			
			if (memo.getMemoValue(k).isMaterialised()) {
				current.setMaterialised(true);
				current.setHashId(e.getHashId());
			}
			tempResult.trackBaseTableFromQuery(t.getAlias(), t.getAlias());
			return;
		}
		Column repBefore=null;
		if(p.getRepartitionBeforeOp()!=null){
			repBefore = new Column(null, p.getRepartitionBeforeOp().getName(), p.getRepartitionBeforeOp().getAlias());
		}
		Node op = e.getChildAt(p.getChoice());
		SQLQuery old = current;
		if (memo.getMemoValue(k).isMaterialised()) {

			SQLQuery q2 = new SQLQuery();
			q2.setHashId(e.getHashId());
			tempResult.setCurrent(q2);
			current = q2;
			current.setMaterialised(true);
			visited.put(k, current);
		}

		if (repBefore != null) {
			current.setRepartition(repBefore, partitionNo);
			current.setPartition(repBefore);
		}

		if (op.getOpCode() == Node.PROJECT
				|| op.getOpCode() == Node.BASEPROJECT) {
			String inputName;

			Projection prj = (Projection) op.getObject();
			// current = q2;
			// q2.setPartition(repAfter, partitionNo);

			for (Output o : prj.getOperands()) {
				try {
					current.getOutputs().add(o.clone());
				} catch (CloneNotSupportedException e1) {
					log.warn("could not clone output", e1);
				}
			}
			current.setOutputColumnsDinstict(prj.isDistinct());
			// current.getOutputs().addAll(prj.getOperands());

			combineOperatorsAndOutputQueriesPush(p.getInputPlan(0), tempResult,
					visited);
			// visited.put(p.getInputPlan(j), q2);

			if (op.getOpCode() == Node.PROJECT) {
				// if we have a final result, remove previous projections
				for (int l = 0; l < current.getOutputs().size(); l++) {
					Output o = current.getOutputs().get(l);
					boolean exists = false;
					for (Output pr : prj.getOperands()) {
						if (pr.getOutputName().equals(o.getOutputName())) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						current.getOutputs().remove(o);
						l--;
					}
				}
			}
			if (tempResult.getLastTable() != null) {
				inputName = tempResult.getLastTable().getAlias();
				if (inputName != current.getTemporaryTableName()
						&& !current.getInputTables().contains(
								tempResult.getLastTable())) {
					current.addInputTable(tempResult.getLastTable());
					for (Output o : current.getOutputs()) {
						for (Column c : o.getObject().getAllColumnRefs()) {
							{
								if (op.isDescendantOfBaseTable(c.getAlias())) {
									o.getObject().changeColumn(
											c,
											new Column(tempResult
													.getQueryForBaseTable(c
															.getAlias()), c
													.getName(), c.getAlias()));
								}
							}
						}

					}
				}

			}

		} else if (op.getOpCode() == Node.JOIN) {
			NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) op
					.getObject();
			NonUnaryWhereCondition nuwcCloned = new NonUnaryWhereCondition(
					nuwc.getLeftOp(), nuwc.getRightOp(), nuwc.getOperator());
			current.addBinaryWhereCondition(nuwcCloned);
			List<String> inputNames = new ArrayList<String>();
			for (int j = 0; j < op.getChildren().size(); j++) {

				combineOperatorsAndOutputQueriesPush(p.getInputPlan(j), tempResult,
						visited);
				inputNames.add(tempResult.getLastTable().getAlias());
				if (tempResult.getLastTable().getAlias() != current
						.getTemporaryTableName()
						&& !current.getInputTables().contains(
								tempResult.getLastTable())) {
					current.addInputTable(tempResult.getLastTable());
					addOutputs(current, tempResult.getLastTable().getAlias(),
							tempResult);
					for (Output o : current.getOutputs()) {
						for (Column c : o.getObject().getAllColumnRefs()) {
							{
								if (op.getChildAt(j).isDescendantOfBaseTable(
										c.getAlias())) {
									o.getObject().changeColumn(
											c,
											new Column(tempResult
													.getQueryForBaseTable(c
															.getAlias()), c
													.getName(), c.getAlias()));
								}
							}
						}

					}
				}
			}

			for (NonUnaryWhereCondition bwc : current
					.getBinaryWhereConditions()) {

				for (int j = 0; j < op.getChildren().size(); j++) {
					for (Operand o : bwc.getOperands()) {

						List<Column> cs = o.getAllColumnRefs();
						if (!cs.isEmpty()) {
							// not constant
							Column c = cs.get(0);
							if (op.getChildAt(j).isDescendantOfBaseTable(
									c.getAlias())) {
								bwc.changeColumn(
										c,
										new Column(tempResult
												.getQueryForBaseTable(c
														.getAlias()), c
												.getName(), c.getAlias()));
							}
						}

					}
				}
			}
			// }

			/*
			 * for (Output o : current.getOutputs()) { for (Column c :
			 * o.getObject().getAllColumnRefs()) { { for (int j = 0; j <
			 * op.getChildren().size(); j++) { Node child = op.getChildAt(j); if
			 * (child.isDescendantOfBaseTable(c.getAlias())) {
			 * o.getObject().changeColumn(c, new Column(inputNames.get(j),
			 * c.getName())); } } } // o.getObject().changeColumn(c, c); } }
			 */
			// tempResult.add(current);
		} else if (op.getOpCode() == Node.UNION
				|| op.getOpCode() == Node.UNIONALL) {
			List<SQLQuery> unions = new ArrayList<SQLQuery>();
			if (e.getParents().isEmpty()) {
				current.setHashId(e.getHashId());
			}
			boolean existsPartitioned = false;
			boolean existsNonPartitioned = false;
			for (int l = 0; l < op.getChildren().size(); l++) {
				SQLQuery u = new SQLQuery();

				// visited.put(op, current);
				tempResult.setCurrent(u);
				combineOperatorsAndOutputQueriesPush(p.getInputPlan(l), tempResult,
						visited);
				// visited.put(p.getInputPlan(l), u);
				if (memo.getMemoValue(p.getInputPlan(l)).isMaterialised()) {
					u = tempResult.get(tempResult.getLastTable().getAlias());
					if (u == null && useCache) {
						// it's a table from cache
						u = new SQLQuery();
						u.addInputTable(tempResult.getLastTable());
						u.setExistsInCache(true);
						tempResult.add(u);
					}
				} else {
					// visited.put(p.getInputPlan(l), u);
					tempResult.add(u);
				}

				u.setHashId(p.getInputPlan(l).getNode().getHashId());
				if (useCache && u.existsInCache()) {
					String tableInCache = registry.get(u.getHashId()).getName();
					tempResult.remove(u);
					u = new SQLQuery();
					if (op.getChildren().size() == 1) {
						u.addInputTable(new Table(tableInCache, tableInCache));
						u.setExistsInCache(true);
						tempResult.add(u);
						return;
					}
					u.setTemporaryTableName(tableInCache);
					// u.addInputTable(new Table(tableInCache, tableInCache));
				}
				unions.add(u);
				if (u.getPartition() != null) {
					existsPartitioned = true;
				} else {
					existsNonPartitioned = true;
				}
			}
			if (unions.size() > 1) {
				tempResult.add(current);
				current.setUnionqueries(unions);
				if (op.getOpCode() == Node.UNIONALL) {
					current.setUnionAll(true);
				}
				visited.put(k, current);
				current.setHashId(e.getHashId());
				if (memo.getMemoValue(k).isMaterialised()) {
					current.setMaterialised(true);
				}
				if (existsNonPartitioned && existsPartitioned) {
					// make all unions have the same number of partitions
					for (SQLQuery u : unions) {
						if (u.getPartition() == null) {
							Column pt = new Column(u.getOutputs().get(0)
									.getOutputName(), u.getOutputs().get(0)
									.getOutputName());
							u.setRepartition(pt, partitionNo);
							u.setPartition(pt);
						}

					}
				}
			} else {
				visited.put(k, unions.get(0));
				unions.get(0)
						.setHashId(p.getInputPlan(0).getNode().getHashId());
				if (memo.getMemoValue(k).isMaterialised()) {
					unions.get(0).setMaterialised(true);
				}
			}
		} else if (op.getOpCode() == Node.SELECT) {
			combineOperatorsAndOutputQueriesPush(p.getInputPlan(0), tempResult,
					visited);
			String inputName = tempResult.getLastTable().getAlias();
			Selection s = (Selection) op.getObject();
			Iterator<Operand> it = s.getOperands().iterator();
			while (it.hasNext()) {
				Operand o = it.next();
				if (o instanceof UnaryWhereCondition) {
					UnaryWhereCondition uwc = (UnaryWhereCondition) o;
					UnaryWhereCondition uwcCloned = new UnaryWhereCondition(
							uwc.getType(), uwc.getOperand(), uwc.getNot(),
							uwc.getObject());
					if (!inputName.equals(current.getTemporaryTableName())) {
						Column c = uwcCloned.getAllColumnRefs().get(0);
						uwcCloned
								.changeColumn(
										c,
										new Column(tempResult
												.getQueryForBaseTable(c
														.getAlias()), c
												.getName(), c.getAlias()));
					}
					current.addUnaryWhereCondition(uwcCloned);
				} else {
					NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o;
					NonUnaryWhereCondition nuwcCloned = new NonUnaryWhereCondition(
							nuwc.getLeftOp(), nuwc.getRightOp(),
							nuwc.getOperator());
					if (!inputName.equals(current.getTemporaryTableName())) {
						for (Column c : nuwcCloned.getAllColumnRefs()) {
							nuwcCloned.changeColumn(
									c,
									new Column(
											tempResult.getQueryForBaseTable(c
													.getAlias()), c.getName(),
											c.getAlias()));
						}
					}
					current.addBinaryWhereCondition(nuwcCloned);
				}
			}

			// visited.put(p.getInputPlan(j), q2);
			if (tempResult.getLastTable() != null) {
				inputName = tempResult.getLastTable().getAlias();
				if (inputName != current.getTemporaryTableName()
						&& !current.getInputTables().contains(
								tempResult.getLastTable())) {
					current.addInputTable(tempResult.getLastTable());
					addOutputs(current, tempResult.getLastTable().getAlias(),
							tempResult);
					for (Output o : current.getOutputs()) {
						for (Column c : o.getObject().getAllColumnRefs()) {
							{
								if (op.isDescendantOfBaseTable(c.getAlias())) {
								o.getObject().changeColumn(
										c,
										new Column(tempResult
												.getQueryForBaseTable(c
														.getAlias()), c
												.getName(), c.getAlias()));
							}
						}
						}

					}
				}

			}

		} else if (op.getOpCode() == Node.LIMIT) {
			current.setLimit(((Integer) op.getObject()).intValue());
			current.setHashId(p.getInputPlan(0).getNode().getHashId());
			combineOperatorsAndOutputQueriesPush(p.getInputPlan(0), tempResult,
					visited);
			if (current.getInputTables().isEmpty()) {
				// limit of a query that exists in the cache
				current.addInputTable(tempResult.getLastTable());
			}
			if (tempResult.isEmpty() && e == this.root) {
				// final limit of a table tha exists in cache
				tempResult.add(current);
			}
		} else if (op.getOpCode() == Node.NESTED) {
			// nested is always materialized
			current.setNested(true);
			combineOperatorsAndOutputQueriesPush(p.getInputPlan(0), tempResult,
					visited);

		} else {
			log.error("Unknown Operator in DAG");
		}
		current.setExistsInCache(false);
		if (memo.getMemoValue(k).isMaterialised()) {
			tempResult.add(current);
			tempResult.setLastTable(current);
			current.setHashId(e.getHashId());
			for (String alias : e.getDescendantBaseTables()) {
				tempResult.trackBaseTableFromQuery(alias, tempResult
						.getLastTable().getAlias());
			}
			tempResult.setCurrent(old);
		}
		if (old != current && old.getRepartition() == null) {
			old.setPartition(current.getPartitionColumn());
		}

	}
	
	
}
