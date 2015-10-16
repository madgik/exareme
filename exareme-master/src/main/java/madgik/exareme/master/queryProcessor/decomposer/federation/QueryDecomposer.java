/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.DecomposerUtils;
import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.dag.NodeHashValues;
import madgik.exareme.master.queryProcessor.decomposer.dag.PartitionCols;
import madgik.exareme.master.queryProcessor.decomposer.query.*;
import madgik.exareme.master.queryProcessor.decomposer.util.Pair;
import madgik.exareme.master.queryProcessor.decomposer.util.Util;
import madgik.exareme.master.queryProcessor.estimator.NodeCostEstimator;
import madgik.exareme.master.queryProcessor.estimator.NodeSelectivityEstimator;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

//import di.madgik.statistics.tools.OverlapAnalyzer;


/**
 * @author dimitris
 */
public class QueryDecomposer {

    private static final Logger log = Logger.getLogger(QueryDecomposer.class);
    private SQLQuery initialQuery;
    ArrayList<SQLQuery> result;
    private final int noOfparts;
    Node root;
    Node union;
    private NodeHashValues hashes;
    private final boolean multiOpt;
    private final boolean centralizedExecution;
    private NamesToAliases n2a;
    private boolean addNotNulls;
    private boolean projectRefCols;
    private String db;
    private Memo memo;
    private Map<String, Set<String>> refCols;
    private NodeSelectivityEstimator nse;
    private Map<Node, Double> limits;
    private boolean addAliases;
    private boolean importExternal;

    public QueryDecomposer(SQLQuery initial) throws ClassNotFoundException {
        this(initial, ".", 1, null);
    }

    public QueryDecomposer(SQLQuery initial, String database, int noOfPartitions,
        NodeSelectivityEstimator nse) {
        result = new ArrayList<SQLQuery>();
        this.initialQuery = initial;
        this.noOfparts = noOfPartitions;
        try {
            // read dbinfo from properties file
            DBInfoReaderDB.read(database);
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(QueryDecomposer.class.getName())
                .log(Level.SEVERE, null, ex);
        }
        this.db = database;
        // DBInfoReader.read("./conf/dbinfo.properties");
        union = new Node(Node.AND);
        if (initialQuery.isIsUnionAll()) {
            union.setObject(("UNIONALL"));
            union.setOperator(Node.UNIONALL);
        } else {
            union.setObject(("UNION"));
            union.setOperator(Node.UNION);
        }

        root = new Node(Node.OR);
        root.setObject(new Table("table" + Util.createUniqueId(), null));
        root.addChild(union);
        this.nse = nse;
        hashes = new NodeHashValues();
        hashes.setSelectivityEstimator(nse);
        this.projectRefCols = DecomposerUtils.PROJECT_REF_COLS;
        multiOpt = DecomposerUtils.MULTI;
        centralizedExecution = DecomposerUtils.CENTRALIZED;
        this.addAliases = DecomposerUtils.ADD_ALIASES;
        this.n2a = new NamesToAliases();
        this.addNotNulls = DecomposerUtils.ADD_NOT_NULLS;
        this.memo = new Memo();
        this.limits = new HashMap<Node, Double>();
        this.importExternal = DecomposerUtils.IMPORT_EXTERNAL;
        if (projectRefCols) {
            refCols = new HashMap<String, Set<String>>();
            initial.generateRefCols(refCols);
      /*
       * for (Table t : initial.getAllReferencedTables()) { Set<String>
			 * colsForT = new HashSet<String>();
			 * if(refCols.containsKey(t.getName())){
			 * colsForT=refCols.get(t.getName()); } for (Column c :
			 * initial.getAllReferencedColumns()) { if
			 * (t.getAlias().equals(c.tableAlias)) { colsForT.add(c.columnName);
			 * } } refCols.put(t.getName(), colsForT); }
			 */
        }
    }

    public ArrayList<SQLQuery> getSubqueries() {
        initialQuery.normalizeWhereConditions();
        if (initialQuery.hasNestedSuqueriesOrLeftJoin()) {
            initialQuery.setTemporary(false);
            decomposeSubquery(initialQuery);
        } else {
            ConjunctiveQueryDecomposer d =
                new ConjunctiveQueryDecomposer(initialQuery, centralizedExecution, addNotNulls);
            List<SQLQuery> subqueries = d.getSubqueries();

            if (!subqueries.isEmpty()) {
                for (SQLQuery sub : subqueries) {
                    decomposeSubquery(sub);
                }
            }
        }

        for (SQLQuery q : result) {
            q.setNumberOfPartitions(this.noOfparts);
            if (q.getPartitioningColumn() == null) {
                log.debug("null partitioning column for " + q.getResultTableName());
                if (!q.getOutputs().isEmpty()) {
                    q.setPartitioningOnColum(new Column("", q.getOutputs().get(0).getOutputName()));
                } else {
                    q.setNumberOfPartitions(1);
                }
            }
        }

        // root.setIsCentralised(union.isCentralised());
        root.setPartitionedOn(union.isPartitionedOn());

        return result;
    }

    public List<SQLQuery> getSubqueries2() throws Exception {
        initialQuery.normalizeWhereConditions();
        if (initialQuery.hasNestedSuqueries()) {
            initialQuery.setTemporary(false);
            decomposeSubquery2(initialQuery);
        } else {
            if (this.multiOpt) {
                List<List<String>> aliases = initialQuery.getListOfAliases(n2a);
                // for(List<String> aliases:initialQuery.getListOfAliases(n2a)){
                List<String> firstAliases = aliases.get(0);
                initialQuery.renameTables(firstAliases);
                ConjunctiveQueryDecomposer d =
                    new ConjunctiveQueryDecomposer(initialQuery, centralizedExecution, addNotNulls);
                Node topSubquery = d.addCQToDAG(union, hashes);
                // String u=union.dotPrint();
                if (addAliases) {
                    for (int i = 1; i < aliases.size(); i++) {
                        List<String> nextAliases = aliases.get(i);
                        topSubquery.addChild(
                            addAliasesToDAG(topSubquery, firstAliases, nextAliases, hashes));
                    }
                }
                // }
                // initialQuery.renameAliases(n2a);

            } else {
                ConjunctiveQueryDecomposer d =
                    new ConjunctiveQueryDecomposer(initialQuery, centralizedExecution, addNotNulls);
                d.addCQToDAG(union, hashes);
            }
        }

        // root.setIsCentralised(union.isCentralised());
        // root.setPartitionedOn(union.isPartitionedOn());
        List<SQLQuery> res = getPlan();
        for (int i = 0; i < res.size(); i++) {
            SQLQuery s = res.get(i);
            s.refactorForFederation();
            if (i == res.size() - 1) {
                s.setTemporary(false);
            }
        }
        if (importExternal) {
            // create dir to store dbs if not exists
            File theDir = new File(this.db + "import/");

            // if the directory does not exist, create it
            if (!theDir.exists()) {
                log.debug("creating directory: " + this.db + "import/");
                boolean result = false;

                try {
                    theDir.mkdir();
                    result = true;
                } catch (SecurityException se) {
                    log.error("Could not create dir to store external imports:" + se.getMessage());
                }
                if (result) {
                    log.debug("Created dir to store external imports");
                }
            }
            ExecutorService es = Executors.newCachedThreadPool();
            for (int i = 0; i < res.size(); i++) {
                SQLQuery s = res.get(i);
                if (s.isFederated()) {
                    es.execute(new DataImporter(s, this.db));
                }
            }
            es.shutdown();
            boolean finished = es.awaitTermination(30, TimeUnit.MINUTES);

        }
        return res;
        // return null;
    }

    public List<SQLQuery> getPlan() {
        //String dot0 = root.dotPrint();
        if (projectRefCols) {
            createProjections(root);
        }

        //
        // String a = root.dotPrint();
        expandDAG(root);
        //String a = root.dotPrint();
        //
        // for (Node n : hashes.values()) {
        // System.out.println(n.getObject().toString());
        // }
        // addRepartitionToPhysicalDAG(root, new HashMap<Pair<Node, Column>,
        // Node>());
        // System.out.println(root.dotPrint());
    /*
     * computePruningPercentage(root, new HashSet<Node>());
		 * System.out.println("total:"); System.out.println(total);
		 * System.out.println("pruned:"); System.out.println(pruned);
		 */
        // Node rootRep = createRepartitionDAG2(root, new HashMap<Pair<Node,
        // Column>, Node>(), null, true, true, true);
        // Node rootRep = getBestPlan(root, null, new Memo(), Double.MAX_VALUE,
        // Double.MAX_VALUE);
        // String a = rootRep.dotPrint();
        // int cost = Integer.MAX_VALUE;
        // HashMap<Pair<Node, Column>, Pair<Node, Plan>> memo = new
        // HashMap<Pair<Node, Column>, Pair<Node, Plan>>();
        long t1 = System.currentTimeMillis();
        SinglePlan best;
        if (noOfparts == 1) {
            best = getBestPlanCentralized(root, Double.MAX_VALUE);
        } else {
            best = getBestPlanPruned(root, null, Double.MAX_VALUE, Double.MAX_VALUE,
                new EquivalentColumnClasses(), new ArrayList<MemoKey>());
        }

        // Plan best = addRepartitionAndComputeBestPlan(root, cost, memo, cel,
        // null);
        System.out.println(System.currentTimeMillis() - t1);
        System.out.println("best cost:" + best.getCost());
        // System.out.println(memo.size());
        // String dot2 = root.dotPrint();
        // System.out.println(t1);
        //
        // Plan best = findBestPlan(root, cost, memo, new HashSet<Node>(), cel);
        // System.out.println(best.getPath().toString());
        SinlgePlanDFLGenerator dsql = new SinlgePlanDFLGenerator(root, best, noOfparts, memo);
        return (List<SQLQuery>) dsql.generate2();
        // return null;
    }

    private void decomposeSubquery2(SQLQuery s) throws Exception {
        // s.normalizeWhereConditions();
        for (SQLQuery u : s.getUnionqueries()) {

            // push limit
            if (s.getLimit() > -1) {
                if (u.getLimit() == -1) {
                    u.setLimit(s.getLimit());
                } else {
                    if (s.getLimit() < u.getLimit()) {
                        u.setLimit(s.getLimit());
                    }
                }
            }
            u.normalizeWhereConditions();
            if (u.hasNestedSuqueries()) {
                decomposeSubquery2(u);
            } else {

                if (this.multiOpt) {
					/*
					 * for (List<String> aliases : u.getListOfAliases(n2a)) {
					 * u.renameTables(aliases); ConjunctiveQueryDecomposer d =
					 * new ConjunctiveQueryDecomposer(u, centralizedExecution,
					 * addNotNulls); d.addCQToDAG(union, hashes); }
					 */
                    List<List<String>> aliases = u.getListOfAliases(n2a);
                    // for(List<String>
                    // aliases:initialQuery.getListOfAliases(n2a)){
                    List<String> firstAliases = aliases.get(0);
                    u.renameTables(firstAliases);
                    ConjunctiveQueryDecomposer d =
                        new ConjunctiveQueryDecomposer(u, centralizedExecution, addNotNulls);
                    Node topSubquery = d.addCQToDAG(union, hashes);
                    // String u=union.dotPrint();
                    if (addAliases) {
                        for (int i = 1; i < aliases.size(); i++) {
                            List<String> nextAliases = aliases.get(i);
                            topSubquery.addChild(
                                addAliasesToDAG(topSubquery, firstAliases, nextAliases, hashes));
                        }
                    }

                } else {
                    ConjunctiveQueryDecomposer d =
                        new ConjunctiveQueryDecomposer(u, centralizedExecution, addNotNulls);

                    d.addCQToDAG(union, hashes);
                }
            }

        }
        // Collection<SQLQuery> nestedSubs=s.getNestedSubqueries();
        if (!s.getNestedSubqueries().isEmpty()) {
            for (SQLQuery nested : s.getNestedSubqueries()) {
                nested.normalizeWhereConditions();
                if (nested.hasNestedSuqueries()) {
                    decomposeSubquery2(nested);
                } else {
                    Node nestedNode = new Node(Node.OR);
                    if (this.multiOpt) {
						/*
						 * for (List<String> aliases :
						 * nested.getListOfAliases(n2a)) {
						 * nested.renameTables(aliases);
						 * ConjunctiveQueryDecomposer d = new
						 * ConjunctiveQueryDecomposer(nested,
						 * centralizedExecution, addNotNulls);
						 * d.addCQToDAG(union, hashes); }
						 */
                        List<List<String>> aliases = nested.getListOfAliases(n2a);
                        // for(List<String>
                        // aliases:initialQuery.getListOfAliases(n2a)){
                        List<String> firstAliases = aliases.get(0);
                        nested.renameTables(firstAliases);
                        ConjunctiveQueryDecomposer d =
                            new ConjunctiveQueryDecomposer(nested, centralizedExecution,
                                addNotNulls);
                        Node topSubquery = d.addCQToDAG(nestedNode, hashes);
                        // String u=union.dotPrint();
                        if (addAliases) {
                            for (int i = 1; i < aliases.size(); i++) {
                                List<String> nextAliases = aliases.get(i);
                                topSubquery.addChild(
                                    addAliasesToDAG(topSubquery, firstAliases, nextAliases,
                                        hashes));
                            }
                        }
                    } else {
                        ConjunctiveQueryDecomposer d =
                            new ConjunctiveQueryDecomposer(nested, centralizedExecution,
                                addNotNulls);
                        d.addCQToDAG(nestedNode, hashes);
                    }

                    nested.putNestedNode(nestedNode.getChildAt(0));
                    nestedNode.removeAllChildren();

                }
            }

            // if s is an "empty" select * do not add it and rename the nested
            // with the s table name??
            if (s.isSelectAll() && s.getBinaryWhereConditions().isEmpty() && s
                .getUnaryWhereConditions().isEmpty() && s.getGroupBy().isEmpty() && s.getOrderBy()
                .isEmpty() && s.getNestedSelectSubqueries().size() == 1) {
                union.addChild(
                    s.getNestedSelectSubqueries().keySet().iterator().next().getNestedNode());
            } else {
                //decompose s changing the nested from tables
                if (this.multiOpt) {

                    List<List<String>> aliases = s.getListOfAliases(n2a);
                    // for(List<String>
                    // aliases:initialQuery.getListOfAliases(n2a)){
                    List<String> firstAliases = new ArrayList<String>();
                    if (!aliases.isEmpty()) {
                        firstAliases = aliases.get(0);
                        s.renameTables(firstAliases);
                    }
                    ConjunctiveQueryDecomposer d =
                        new ConjunctiveQueryDecomposer(s, centralizedExecution, addNotNulls);
                    Node topSubquery = d.addCQToDAG(union, hashes);
                    // String u=union.dotPrint();
                    if (addAliases) {
                        for (int i = 1; i < aliases.size(); i++) {
                            List<String> nextAliases = aliases.get(i);
                            topSubquery.addChild(
                                addAliasesToDAG(topSubquery, firstAliases, nextAliases, hashes));
                        }
                    }
                } else {
                    ConjunctiveQueryDecomposer d =
                        new ConjunctiveQueryDecomposer(s, centralizedExecution, addNotNulls);
                    d.addCQToDAG(union, hashes);
                }
            }



        }

    }

    private void decomposeSubquery(SQLQuery s) {
        s.normalizeWhereConditions();
        for (SQLQuery u : s.getUnionqueries()) {

            // push limit
            if (s.getLimit() > -1) {
                if (u.getLimit() == -1) {
                    u.setLimit(s.getLimit());
                } else {
                    if (s.getLimit() < u.getLimit()) {
                        u.setLimit(s.getLimit());
                    }
                }
            }

            ConjunctiveQueryDecomposer d =
                new ConjunctiveQueryDecomposer(u, centralizedExecution, addNotNulls);
            List<SQLQuery> subqueries = d.getSubqueries();

            if (!subqueries.isEmpty()) {
                for (SQLQuery sub : subqueries) {
                    if (!sub.isTemporary()) {
                        // sub is the final result of the decomposition of this
                        // subquery inside union.
                        // Set its table name as the table name of the select *
                        // from ... UNION ...
                        // and make it temporary, as it is not the final result
                        // of the initial query
                        u.setResultTableName(sub.getResultTableName());
                        sub.setTemporary(true);
                    }
                    decomposeSubquery(sub);
                }
            } else {
                // there are no subqueries, the initial query has select from
                // union

                decomposeSubquery(u);
                // String uName=u.getTemporaryTableName();
            }
        }
        // Collection<SQLQuery> nestedSubs=s.getNestedSubqueries();
        // if (!nestedSubs.isEmpty()) {
        for (SQLQuery nested : s.getNestedSubqueries()) {
            nested.normalizeWhereConditions();
            if (!nested.hasNestedSuqueriesOrLeftJoin()) {
                if (this.multiOpt) {
                    nested.renameAliases(n2a);
                }
                ConjunctiveQueryDecomposer d =
                    new ConjunctiveQueryDecomposer(nested, centralizedExecution, addNotNulls);
                List<SQLQuery> subqueries = d.getSubqueries();

                if (!subqueries.isEmpty()) {
                    boolean singleSubqueryAlreadyExisting = false;
                    for (SQLQuery sub : subqueries) {
                        if (!sub.isTemporary()) {
                            // sub is the final result of the decomposition of
                            // this subquery inside nested select.
                            // Set its table name as the table name of the
                            // select * from ... select ...
                            // and make it temporary, as it is not the final
                            // result of the initial query
                            nested.setResultTableName(sub.getResultTableName());
                            sub.setTemporary(true);
                            // push limit
                            if (s.getLimit() > -1) {
                                sub.pushLimit(s.getLimit());
                            }
                        }
                        // look if it is already there?
                        if (subqueries.size() == 1) {
                            for (SQLQuery q : this.result) {
                                if (q.hasTheSameDistSQL(sub)) {
                                    nested.setTemporaryTableName(q.getTemporaryTableName());
                                    singleSubqueryAlreadyExisting = true;
                                }
                            }
                        }
                        if (!singleSubqueryAlreadyExisting) {
                            decomposeSubquery(sub);
                        }
                    }
                    // System.out.println(s.nestedSelectSubquery.toDistSQL());
                } else {
                    // there are no subqueries, the initial query has select
                    // from union or nested select
                    decomposeSubquery(nested);
                    // s.nestedSelectSubquery.setTemporary(true);
                }
            } else {
                // there are no subqueries, the initial query has select from
                // union or nested select
                decomposeSubquery(nested);
                // s.nestedSelectSubquery.setTemporary(true);
            }
        }
        // }
        if (s.getJoinType() != null) {
            JoinQueryDecomposer d =
                new JoinQueryDecomposer(s, db, noOfparts, centralizedExecution, multiOpt,
                    addNotNulls, projectRefCols, nse);
            ArrayList<SQLQuery> subqueries = d.getSubqueries();
            // d.addCQToDAG(union, hashes);
            for (SQLQuery q : subqueries) {
                result.add(q);
            }
        }

        if (s.getNestedSubqueries().size() > 0 && (!s.getBinaryWhereConditions().isEmpty() || !s
            .getUnaryWhereConditions().isEmpty())) {
            ConjunctiveQueryDecomposer d =
                new ConjunctiveQueryDecomposer(s, centralizedExecution, addNotNulls);
            List<SQLQuery> subqueries = d.getSubqueries();

            if (!subqueries.isEmpty()) {
                for (SQLQuery sub : subqueries) {

                    decomposeSubquery(sub);
                    if (!sub.isTemporary()) {
                        for (SQLQuery q : s.getNestedSubqueries()) {
                            sub.addInputTableIfNotExists(
                                new Table(q.getTemporaryTableName(), s.getNestedSubqueryAlias(q)));

                            for (Column c : sub.getAllColumns()) {
                                c.tableAlias = q.getTemporaryTableName();
                            }
                        }
                    }
                }
            } else {
                result.add(s);
            }
        } else {
            // push limit
            if (s.getLimit() > -1) {
                for (SQLQuery nested : s.getNestedSubqueries()) {
                    nested.pushLimit(s.getLimit());

                }
            }

            // if s is an "empty" select * do not add it and rename the nested
            // with the s table name??
            if (s.isSelectAll() && s.getBinaryWhereConditions().isEmpty() && s
                .getUnaryWhereConditions().isEmpty() && s.getGroupBy().isEmpty() && s.getOrderBy()
                .isEmpty() && s.getNestedSelectSubqueries().size() == 1) {

                SQLQuery nested = s.getNestedSelectSubqueries().keySet().iterator().next();
                nested.setTemporaryTableName(s.getTemporaryTableName());
                nested.setTemporary(s.isTemporary());
            } else {
                result.add(s);
            }
        }

        // System.out.println(s.toDistSQL());
    }

    // private void computeJoinSimilarities() {
    // try {
    // OverlapAnalyzer.overlapDetection(this.joinLists);
    // } catch (Exception ex) {
    // java.util.logging.Logger.getLogger(QueryDecomposer.class.getName()).log(Level.SEVERE,
    // null, ex);
    // }
    // }
    private void expandDAG(Node eq) {

        // System.out.println(eq.getChildren().size() +
        // eq.getObject().toString());
        // int noOfChildren=eq.getChildren().size();
        for (int i = 0; i < eq.getChildren().size(); i++) {
            Node op = eq.getChildAt(i);
            if (!op.isExpanded()) {
                for (int x = 0; x < op.getChildren().size(); x++) {
                    Node inpEq = op.getChildAt(x);
                    // System.out.println(i);
                    // root.dotPrint();
                    expandDAG(inpEq);

                }

                // String a=op.getChildAt(0).dotPrint();
                // aplly all possible transfromations to op

                // join commutativity a join b -> b join a
                // This never adds a node because of hashing!!!!!!
                if (op.getObject() instanceof NonUnaryWhereCondition) {
                    NonUnaryWhereCondition bwc = (NonUnaryWhereCondition) op.getObject();
                    if (bwc.getOperator().equals("=")) {
                        Node commutativity = new Node(Node.AND, Node.JOIN);
                        NonUnaryWhereCondition newBwc = new NonUnaryWhereCondition();
                        newBwc.setOperator("=");
                        newBwc.setLeftOp(bwc.getRightOp());
                        newBwc.setRightOp(bwc.getLeftOp());
                        commutativity.setObject(newBwc);
                        if (op.getChildren().size() > 1) {
                            commutativity.addChild(op.getChildAt(1));
                        }
                        commutativity.addChild(op.getChildAt(0));

                        if (!hashes.containsKey(commutativity.getHashId())) {
                            hashes.put(commutativity.getHashId(), commutativity);
                            hashes.remove(eq.getHashId());
                            eq.addChild(commutativity);

                            hashes.put(eq.getHashId(), eq);
                            commutativity.addAllDescendantBaseTables(op.getDescendantBaseTables());

                        } else {
                            unify(eq, hashes.get(commutativity.getHashId()).getFirstParent());
                            commutativity.removeAllChildren();

                        }

                    }
                }

                // join left associativity: a join (b join c) -> (a join b) join
                // c
                // or (a join c) join b
                if (op.getObject() instanceof NonUnaryWhereCondition) {
                    NonUnaryWhereCondition bwc = (NonUnaryWhereCondition) op.getObject();
                    if (bwc.getOperator().equals("=")) {
                        // for (Node c2 : op.getChildren()) {
                        if (op.getChildren().size() > 1) {
                            Node c2 = op.getChildAt(1);
                            for (Node c3 : c2.getChildren()) {
                                // if (c2.getChildren().size() > 0) {
                                // Node c3 = c2.getChildAt(0);
                                if (c3.getObject() instanceof NonUnaryWhereCondition
                                    && c3.getChildren().size() > 1) {
                                    NonUnaryWhereCondition bwc2 =
                                        (NonUnaryWhereCondition) c3.getObject();
                                    if (bwc2.getOperator().equals("=")) {
                                        boolean comesFromLeftOp = c3.getChildAt(0)
                                            .isDescendantOfBaseTable(
                                                bwc.getRightOp().getAllColumnRefs()
                                                    .get(0).tableAlias);
                                        Node associativity = new Node(Node.AND, Node.JOIN);
                                        NonUnaryWhereCondition newBwc =
                                            new NonUnaryWhereCondition();
                                        newBwc.setOperator("=");
										/*
										 * if (comesFromLeftOp) {
										 * newBwc.setRightOp(bwc2.getLeftOp());
										 * } else {
										 * newBwc.setRightOp(bwc2.getRightOp());
										 * }
										 */
                                        newBwc.setRightOp(bwc.getRightOp());
                                        newBwc.setLeftOp(bwc.getLeftOp());
                                        associativity.setObject(newBwc);
                                        associativity.addChild(op.getChildAt(0));

                                        if (comesFromLeftOp) {
                                            associativity.addChild(c3.getChildAt(0));

                                        } else {
                                            associativity.addChild(c3.getChildAt(1));

                                        }
                                        Node table = new Node(Node.OR);
                                        table.setObject(
                                            new Table("table" + Util.createUniqueId(), null));
                                        if (hashes.containsKey(associativity.getHashId())) {
                                            Node assocInHashes =
                                                hashes.get(associativity.getHashId());
                                            table = assocInHashes.getFirstParent();

                                            associativity.removeAllChildren();
                                            // associativity = assocInHashes;

                                        } else {
                                            hashes.put(associativity.getHashId(), associativity);
                                            table.addChild(associativity);

                                            // table.setPartitionedOn(new
                                            // PartitionCols(newBwc.getAllColumnRefs()));
                                            hashes.put(table.getHashId(), table);
                                            associativity.addAllDescendantBaseTables(
                                                op.getChildAt(0).getDescendantBaseTables());
                                            if (comesFromLeftOp) {
                                                associativity.addAllDescendantBaseTables(
                                                    c3.getChildAt(0).getDescendantBaseTables());

                                            } else {
                                                associativity.addAllDescendantBaseTables(
                                                    c3.getChildAt(1).getDescendantBaseTables());

                                            }
                                            table.addAllDescendantBaseTables(
                                                associativity.getDescendantBaseTables());
                                        }
                                        // table.setPartitionedOn(new
                                        // PartitionCols(newBwc.getAllColumnRefs()));

                                        // table.setIsCentralised(c3.getChildAt(0).isCentralised()
                                        // && op.getChildAt(0).isCentralised());
                                        Node associativityTop = new Node(Node.AND, Node.JOIN);
                                        NonUnaryWhereCondition newBwc2 =
                                            new NonUnaryWhereCondition();
                                        newBwc2.setOperator("=");
                                        if (comesFromLeftOp) {
                                            newBwc2.setRightOp(bwc2.getRightOp());
                                            newBwc2.setLeftOp(bwc2.getLeftOp());
                                        } else {
                                            newBwc2.setRightOp(bwc2.getLeftOp());
                                            newBwc2.setLeftOp(bwc2.getRightOp());
                                        }
                                        // newBwc2.setLeftOp(bwc.getRightOp());
                                        associativityTop.setObject(newBwc2);
                                        associativityTop.addChild(table);

                                        if (comesFromLeftOp) {
                                            associativityTop.addChild(c3.getChildAt(1));

                                        } else {
                                            associativityTop.addChild(c3.getChildAt(0));

                                        }
                                        // System.out.println(associativityTop.getObject().toString());
                                        if (!hashes.containsKey(associativityTop.getHashId())) {
                                            hashes.put(associativityTop.getHashId(),
                                                associativityTop);
                                            // Node newTop =
                                            // hashes.checkAndPutWithChildren(associativityTop);
                                            hashes.remove(eq.getHashId());
                                            eq.addChild(associativityTop);
                                            associativityTop.addAllDescendantBaseTables(
                                                op.getDescendantBaseTables());
                                            // noOfChildren++;
                                            // eq.setPartitionedOn(new
                                            // PartitionCols(newBwc.getAllColumnRefs()));
                                            // if(!h.containsKey(eq.computeHashID())){
                                            hashes.put(eq.getHashId(), eq);
                                            // }
											/*
											 * if
											 * (!h.containsKey(associativityTop
											 * .computeHashID())){
											 * h.putWithChildren
											 * (associativityTop);
											 * h.remove(eq.computeHashID());
											 * eq.addChild(associativityTop);
											 * if(
											 * !h.containsKey(eq.computeHashID
											 * ())){ h.put(eq.computeHashID(),
											 * eq); } else{ //needs unification?
											 * } } else{ //unify
											 * //unify(associativityTop, eq, h);
											 * Node
											 * other=h.get(associativityTop.
											 * computeHashID());
											 * h.remove(eq.computeHashID());
											 * eq.addChild(other);
											 * if(!h.containsKey
											 * (eq.computeHashID())){
											 * h.put(eq.computeHashID(), eq); }
											 * else{ //needs unification? } }
											 */
                                        } else {

                                            unify(eq, hashes.get(associativityTop.getHashId())
                                                .getFirstParent());
                                            // same as unify(eq', eq)???
                                            // checking again children of eq?
                                            associativityTop.removeAllChildren();
                                            if (table.getParents().isEmpty()) {
                                                if (hashes.get(table.getHashId()) == table) {
                                                    hashes.remove(table.getHashId());
                                                }
                                                for (Node n : table.getChildren()) {
                                                    if (n.getParents().size() == 1) {
                                                        if (hashes.get(n.getHashId()) == n) {
                                                            hashes.remove(n.getHashId());
                                                        }
                                                    }
                                                }
                                                table.removeAllChildren();
                                            }
                                            if (associativity.getParents().isEmpty()) {
                                                if (hashes.get(associativity.getHashId())
                                                    == associativity) {
                                                    hashes.remove(associativity.getHashId());
                                                }
                                                associativity.removeAllChildren();
                                            }

                                            // do we need this?
											/*
											 * Node otherAssocTop =
											 * hashes.get(associativityTop
											 * .getHashId()); if
											 * (!eq.getChildren
											 * ().contains(otherAssocTop)) {
											 * hashes.remove(eq.getHashId());
											 * eq.addChild(otherAssocTop);
											 * noOfChildren++;
											 * //eq.setPartitionedOn(new
											 * PartitionCols
											 * (newBwc.getAllColumnRefs())); //
											 * if
											 * (!h.containsKey(eq.computeHashID
											 * ())){
											 * hashes.put(eq.computeHashID(),
											 * eq); }
											 */
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                // join right associativity: (a join b) join c -> a join (b join
                // c)
                // or b join (a join c)???
				/*
				 * if (op.getObject() instanceof NonUnaryWhereCondition) {
				 * NonUnaryWhereCondition bwc = (NonUnaryWhereCondition)
				 * op.getObject(); if (bwc.getOperator().equals("=")) { //for
				 * (Node c2 : op.getChildren()) { if (op.getChildren().size() >
				 * 1) { Node c2 = op.getChildAt(0); boolean now = false; if
				 * (op.getChildAt(1).getObject().toString().equals("D alias3"))
				 * { String dot = op.getChildAt(0).dotPrint();
				 * System.out.println("ddd"); } for (Node c3 : c2.getChildren())
				 * { // if (c2.getChildren().size() > 0) { // Node c3 =
				 * c2.getChildAt(0); if (c3.getObject() instanceof
				 * NonUnaryWhereCondition && c3.getChildren().size() > 1) {
				 * NonUnaryWhereCondition bwc2 = (NonUnaryWhereCondition)
				 * c3.getObject(); if (bwc2.getOperator().equals("=")) { //we
				 * have to check from which table bwc.getLeftOp() comes!!!!
				 * //and make this table the left op in newBwc
				 * //DistSQLGenerator.baseTableIsDescendantOfNode() boolean
				 * comesFromLeftOp =
				 * DistSQLGenerator.baseTableIsDescendantOfNode
				 * (c3.getChildAt(0),
				 * bwc.getLeftOp().getAllColumnRefs().get(0).tableAlias); Node
				 * associativity = new Node(Node.AND, Node.JOIN);
				 * NonUnaryWhereCondition newBwc = new NonUnaryWhereCondition();
				 * newBwc.setOperator("=");
				 * 
				 * newBwc.setLeftOp(bwc.getLeftOp());
				 * newBwc.setRightOp(bwc.getRightOp());
				 * associativity.setObject(newBwc); if (comesFromLeftOp) {
				 * associativity.addChild(c3.getChildAt(0));
				 * 
				 * associativity.setPartitionRecord(c3.getChildAt(0).
				 * getPartitionRecord()); } else {
				 * associativity.addChild(c3.getChildAt(1));
				 * 
				 * associativity.setPartitionRecord(c3.getChildAt(1).
				 * getPartitionRecord()); }
				 * associativity.addChild(op.getChildAt(1));
				 * 
				 * associativity.mergePartitionRecords(op.getChildAt(1).
				 * getPartitionRecord(), newBwc.getAllColumnRefs());
				 * associativity.checkChildrenForPruning(); Node table = new
				 * Node(Node.OR); table.setObject(new Table("table" +
				 * Util.createUniqueId().toString(), null));
				 * 
				 * if (hashes.containsKey(associativity.getHashId())) {
				 * associativity = hashes.get(associativity.getHashId()); table
				 * = associativity.getFirstParent(); } else {
				 * hashes.put(associativity.getHashId(), associativity);
				 * table.addChild(associativity);
				 * table.setPartitionRecord(associativity.getPartitionRecord());
				 * hashes.put(table.getHashId(), table); }
				 * 
				 * // table.addChild(associativity);
				 * //table.setPartitionedOn(new
				 * PartitionCols(newBwc.getAllColumnRefs()));
				 * //table.setIsCentralised(c3.getChildAt(1).isCentralised() &&
				 * op.getChildAt(1).isCentralised()); Node associativityTop =
				 * new Node(Node.AND, Node.JOIN); NonUnaryWhereCondition newBwc2
				 * = new NonUnaryWhereCondition(); newBwc2.setOperator("="); if
				 * (comesFromLeftOp) { newBwc2.setLeftOp(bwc2.getRightOp());
				 * newBwc2.setRightOp(bwc2.getLeftOp()); } else {
				 * newBwc2.setLeftOp(bwc2.getLeftOp());
				 * newBwc2.setRightOp(bwc2.getRightOp()); }
				 * 
				 * // newBwc2.setRightOp(bwc.getLeftOp());
				 * associativityTop.setObject(newBwc2); if (comesFromLeftOp) {
				 * associativityTop.addChild(c3.getChildAt(1));
				 * 
				 * associativityTop.setPartitionRecord(c3.getChildAt(1).
				 * getPartitionRecord()); } else {
				 * associativityTop.addChild(c3.getChildAt(0));
				 * 
				 * associativityTop.setPartitionRecord(c3.getChildAt(0).
				 * getPartitionRecord()); }
				 * 
				 * associativityTop.addChild(table);
				 * 
				 * associativityTop.mergePartitionRecords(table.
				 * getPartitionRecord (), newBwc2.getAllColumnRefs());
				 * associativityTop.checkChildrenForPruning(); if
				 * (!hashes.containsKey(associativityTop.getHashId())) {
				 * hashes.put(associativityTop.getHashId(), associativityTop);
				 * //Node newTop =
				 * hashes.checkAndPutWithChildren(associativityTop);
				 * hashes.remove(eq.getHashId()); eq.addChild(associativityTop);
				 * //noOfChildren++; //eq.setPartitionedOn(new
				 * PartitionCols(newBwc.getAllColumnRefs())); //
				 * if(!h.containsKey(eq.computeHashID())){
				 * hashes.put(eq.getHashId(), eq); } else { unify(eq,
				 * hashes.get(associativityTop.getHashId()).getFirstParent());
				 * //same as unify(eq', eq)??? checking again children of eq?
				 * associativityTop.removeAllChildren(); //do we need this?
				 * 
				 * } } } } } } }
				 */
                // join predicate pushdown
                // select_a(A) join B -> select_a(A join B)
				/*
				 * if (op.getObject() instanceof NonUnaryWhereCondition) {
				 * NonUnaryWhereCondition bwc = (NonUnaryWhereCondition)
				 * op.getObject(); if (bwc.getOperator().equals("=")) { //for
				 * (Node c2 : op.getChildren()) { if (op.getChildren().size() >
				 * 1) { Node c2 = op.getChildAt(0); //Node b = op.getChildAt(1);
				 * if (c2.getChildren().size() > 0) { Node c3 =
				 * c2.getChildAt(0); if (c3.getObject() instanceof Selection) {
				 * Selection s = (Selection) c3.getObject(); Node newJoin = new
				 * Node(Node.AND, Node.JOIN); newJoin.setObject(bwc);
				 * newJoin.addChild(c3.getChildAt(0));
				 * newJoin.setPartitionRecord(op.getPartitionRecord());
				 * newJoin.addChild(op.getChildAt(1));
				 * //newJoin.mergePartitionRecords
				 * (op.getChildAt(1).getPartitionRecord(),
				 * bwc.getAllColumnRefs()); Node ab = new Node(Node.OR);
				 * ab.setObject(new Table("table" +
				 * Util.createUniqueId().toString(), null)); if
				 * (hashes.containsKey(newJoin.getHashId())) {
				 * //newJoin.removeAllChildren(); newJoin =
				 * hashes.get(newJoin.getHashId()); ab =
				 * newJoin.getFirstParent(); } else {
				 * hashes.put(newJoin.getHashId(), newJoin);
				 * ab.addChild(newJoin);
				 * ab.setPartitionRecord(newJoin.getPartitionRecord()); } Node
				 * sel = new Node(Node.AND, Node.SELECT); sel.setObject(s);
				 * sel.addChild(ab);
				 * sel.setPartitionRecord(ab.getPartitionRecord()); if
				 * (hashes.containsKey(sel.getHashId())) { unify(eq,
				 * hashes.get(sel.getHashId()).getFirstParent()); //same as
				 * unify(eq', eq)??? checking again children of eq?
				 * sel.removeAllChildren(); if (ab.getParents().isEmpty()) { if
				 * (hashes.get(ab.getHashId()) == ab) {
				 * hashes.remove(ab.getHashId()); } ab.removeAllChildren(); } if
				 * (newJoin.getParents().isEmpty()) { if
				 * (hashes.get(newJoin.getHashId()) == newJoin) {
				 * hashes.remove(newJoin.getHashId()); }
				 * newJoin.removeAllChildren(); }
				 * 
				 * } else { hashes.put(sel.getHashId(), sel);
				 * hashes.remove(eq.getHashId()); eq.addChild(sel);
				 * //noOfChildren++; } //String g = this.root.dotPrint(); //
				 * Node top = hashes.checkAndPutWithChildren(sel);
				 * 
				 * if (!hashes.containsKey(eq.getHashId())) {
				 * hashes.put(eq.getHashId(), eq); } //check if top can be
				 * unified with some other node? } }
				 * 
				 * c2 = op.getChildAt(1); //b = op.getChildAt(0); if
				 * (c2.getChildren().size() > 0) { Node c3 = c2.getChildAt(0);
				 * if (c3.getObject() instanceof Selection) { Selection s =
				 * (Selection) c3.getObject(); Node newJoin = new Node(Node.AND,
				 * Node.JOIN); newJoin.setObject(op.getObject());
				 * newJoin.addChild(op.getChildAt(0));
				 * newJoin.setPartitionRecord
				 * (op.getChildAt(0).getPartitionRecord());
				 * newJoin.addChild(c3.getChildAt(0));
				 * newJoin.setPartitionRecord(op.getPartitionRecord());
				 * //newJoin
				 * .mergePartitionRecords(c3.getChildAt(0).getPartitionRecord(),
				 * bwc.getAllColumnRefs());
				 * 
				 * Node ab = new Node(Node.OR); ab.setObject(new Table("table" +
				 * Util.createUniqueId().toString(), null)); if
				 * (hashes.containsKey(newJoin.getHashId())) {
				 * //newJoin.removeAllChildren(); newJoin =
				 * hashes.get(newJoin.getHashId()); ab =
				 * newJoin.getFirstParent(); } else {
				 * hashes.put(newJoin.getHashId(), newJoin);
				 * ab.addChild(newJoin);
				 * ab.setPartitionRecord(newJoin.getPartitionRecord()); } Node
				 * sel = new Node(Node.AND, Node.SELECT); sel.setObject(s);
				 * sel.addChild(ab);
				 * sel.setPartitionRecord(ab.getPartitionRecord()); if
				 * (hashes.containsKey(sel.getHashId())) { unify(eq,
				 * hashes.get(sel.getHashId()).getFirstParent()); //same as
				 * unify(eq', eq)??? checking again children of eq?
				 * sel.removeAllChildren(); } else { hashes.put(sel.getHashId(),
				 * sel); hashes.remove(eq.getHashId()); eq.addChild(sel);
				 * //noOfChildren++; } //String g = this.root.dotPrint(); //
				 * Node top = hashes.checkAndPutWithChildren(sel);
				 * 
				 * if (!hashes.containsKey(eq.getHashId())) {
				 * hashes.put(eq.getHashId(), eq); } //check if top can be
				 * unified with some other node? } } }
				 * 
				 * } }
				 */

                op.setExpanded(true);
            }
        }

    }

    private Node physDagGen(Node e, HashMap<Pair<Node, Column>, Node> memo, Column p) {
        if (memo.containsKey(new Pair(e, p))) {
            return memo.get(new Pair(e, p));
        }
        Node np = new Node(e.getType(), e.getOpCode());
        np.setObject(e.getObject());
        for (Node o : e.getChildren()) {
            if (p == null) {
                NonUnaryWhereCondition bwc = (NonUnaryWhereCondition) o.getObject();
                Node oa = new Node(o.getType(), o.getOpCode());
                oa.setObject(o.getObject());
                np.addChild(oa);
                for (int i = 0; i < e.getChildren().size(); i++) {
                    Node oi = e.getChildAt(i);
                    if (i == 0) {
                        oa.addChild(
                            physDagGen(oi, memo, bwc.getLeftOp().getAllColumnRefs().get(0)));
                    }
                    if (i == 1) {
                        oa.addChild(
                            physDagGen(oi, memo, bwc.getRightOp().getAllColumnRefs().get(0)));
                    }

                }
            } else if (((Operand) o.getObject()).getAllColumnRefs().contains(p)) {
                NonUnaryWhereCondition bwc = (NonUnaryWhereCondition) o.getObject();
                // !!! what about different ops???
                // we should check if oi is already ptned in p
                Node oa = new Node(o.getType(), o.getOpCode());
                oa.setObject(o.getObject());
                np.addChild(oa);
                for (int i = 0; i < e.getChildren().size(); i++) {
                    Node oi = e.getChildAt(i);
                    if (i == 0) {
                        oa.addChild(
                            physDagGen(oi, memo, bwc.getLeftOp().getAllColumnRefs().get(0)));
                    }
                    if (i == 1) {
                        oa.addChild(
                            physDagGen(oi, memo, bwc.getRightOp().getAllColumnRefs().get(0)));
                    }

                }
            }
        }
        // for every enforcer!
        if (p != null) {
            Node of = new Node(Node.OR, Node.REPARTITION);
            of.setObject(p);
            np.addChild(of);
            of.addChild(physDagGen(e, memo, null));
        }
        memo.put(new Pair(e, p), np);
        return np;
    }

    private void addRepartitionToPhysicalDAG(Node eq, HashMap<Pair<Node, Column>, Node> memo) {
        // Set<PartitionCols> partitionRecord=new HashSet<PartitionCols>();
        // HashMap<Pair<NonUnaryWhereCondition, Integer>, Node>
        // partitionedEquivalent=new HashMap<Pair<NonUnaryWhereCondition,
        // Integer>, Node>();
        for (int i = 0; i < eq.getChildren().size(); i++) {

            Node op = eq.getChildAt(i);
            // for (Node c : e.getChildren()) {
            if (op.getOpCode() == Node.JOIN) {

                NonUnaryWhereCondition bwc = (NonUnaryWhereCondition) op.getObject();
                Column leftJoinCol = bwc.getLeftOp().getAllColumnRefs().get(0);
                Column rightJoinCol = bwc.getRightOp().getAllColumnRefs().get(0);

                if (op.getChildren().size() == 1) {
                    // join in the same table! does not need repartition
                    // Node lt = op.getChildAt(0);
                    // if (memo.containsKey(lt)) {
                    // c.removeChild(lt);
                    // c.addChildAt(memo.get(lt), 0);
                    // } else {
                    addRepartitionToPhysicalDAG(op.getChildAt(0), memo);
                    eq.addToPartitionRecord(op.getChildAt(0).getPartitionRecord());
                    // }
                } else {

                    Node leftJoin = op.getChildAt(0);
                    Node rightJoin = op.getChildAt(1);
                    Pair lp = new Pair(leftJoin, leftJoinCol);
                    // why only leftJoinCol and not equivalent?
                    // because memo contains every column of a set
                    if (memo.containsKey(lp)) {
                        op.removeChild(leftJoin);
                        op.addChildAt(memo.get(lp), 0);
                        eq.addToPartitionRecord(memo.get(lp).getPartitionRecord());
                    } else {

                        // split partitioned sets to different nodes
                        if (leftJoin.isPartitionedOn().size() > 1) {
                            Node nonPartitioned = leftJoin;
                            List<Node> gchildren = leftJoin.getChildren();
                            Iterator<PartitionCols> combined =
                                combineColumns(leftJoin.isPartitionedOn());
                            // Iterator<PartitionCols>
                            // it=c.getChildAt(0).isPartitionedOn().iterator();
                            leftJoin.setPartitionedOn(combined.next());
                            leftJoin.removeAllChildren();
                            for (Node gchild : gchildren) {
                                Operand nuwc = (Operand) gchild.getObject();
                                if (leftJoin.getFirstPartitionedSet()
                                    .contains(nuwc.getAllColumnRefs().get(0))) {
                                    leftJoin.addChild(gchild);
                                }
                            }
                            // addRepartitionToNode(leftJoin, bwc.getLeftOp(),
                            // op, memo);

                            while (combined.hasNext()) {
                                Node partNode = new Node(Node.OR);
                                partNode.setObject(
                                    new Table("table" + Util.createUniqueId().toString(), null));
                                partNode.setPartitionedOn(combined.next());
                                for (Node gchild : gchildren) {
                                    Operand nuwc = (Operand) gchild.getObject();
                                    if (partNode.getFirstPartitionedSet()
                                        .contains(nuwc.getAllColumnRefs().get(0))) {
                                        partNode.addChild(gchild);
                                    }
                                }
                                boolean exists = false;
                                Node existing = null;
                                for (Column pc : partNode.getFirstPartitionedSet().getColumns()) {

                                    Pair lp2 = new Pair(nonPartitioned, pc);
                                    if (memo.containsKey(lp2)) {

                                        existing = memo.get(lp2);
                                        // partNode=existing;
                                        exists = true;
                                        break;
                                    }
                                }
                                if (exists) {
                                    for (Node exch : partNode.getChildren()) {
                                        existing.addChild(exch);
                                    }
                                    existing.getFirstPartitionedSet()
                                        .addColumns(partNode.getFirstPartitionedSet().getColumns());
                                    eq.addToPartitionRecord(existing.getPartitionRecord());
                                    // partNode = existing;
                                } else {

                                    // Node otherjoin = new Node(Node.AND,
                                    // Node.JOIN);
                                    // otherjoin.setObject(bwc);
                                    // otherjoin.addChild(partNode);
                                    // createEnforcerForNode(rightJoin,
                                    // otherjoin, memo, bwc.getRightOp());
                                    // otherjoin.addChild(rightJoin);
                                    // right join????? get from hash?
                                    // eq.addChildAt(otherjoin, 0);
                                    // i++;
                                    addRepartitionToNode(partNode, bwc.getLeftOp(), op, eq, memo);
                                    addRepartitionToNode(rightJoin, bwc.getRightOp(), op, eq, memo);
                                    for (Column pc : partNode.getFirstPartitionedSet()
                                        .getColumns()) {

                                        Pair lp2 = new Pair(nonPartitioned, pc);
                                        memo.put(lp2, partNode);
                                    }
                                }
                                // addRepartitionToPhysicalDAG(partNode, memo);

                            }
                        }
                        addRepartitionToNode(leftJoin, bwc.getLeftOp(), op, eq, memo);

                    }

                    Pair rp = new Pair(rightJoin, rightJoinCol);
                    if (memo.containsKey(rp)) {
                        op.removeChild(rightJoin);
                        op.addChildAt(memo.get(rp), 1);
                        eq.addToPartitionRecord(memo.get(rp).getPartitionRecord());
                    } else {
                        // split partitioned sets to different nodes
                        if (rightJoin.isPartitionedOn().size() > 1) {
                            Node nonPartitioned = rightJoin;
                            List<Node> gchildren = rightJoin.getChildren();
                            Iterator<PartitionCols> combined =
                                combineColumns(rightJoin.isPartitionedOn());
                            // Iterator<PartitionCols>
                            // it=c.getChildAt(0).isPartitionedOn().iterator();
                            rightJoin.setPartitionedOn(combined.next());
                            rightJoin.removeAllChildren();
                            for (Node gchild : gchildren) {
                                Operand nuwc = (Operand) gchild.getObject();
                                if (rightJoin.getFirstPartitionedSet()
                                    .contains(nuwc.getAllColumnRefs().get(0))) {
                                    rightJoin.addChild(gchild);
                                }
                            }
                            // addRepartitionToNode(rightJoin, bwc.getRightOp(),
                            // op, memo);

                            while (combined.hasNext()) {
                                Node partNode = new Node(Node.OR);
                                partNode.setObject(
                                    new Table("table" + Util.createUniqueId().toString(), null));
                                partNode.setPartitionedOn(combined.next());
                                for (Node gchild : gchildren) {
                                    Operand nuwc = (Operand) gchild.getObject();
                                    if (partNode.getFirstPartitionedSet()
                                        .contains(nuwc.getAllColumnRefs().get(0))) {
                                        partNode.addChild(gchild);
                                    }
                                }
                                boolean exists = false;
                                Node existing = null;
                                for (Column pc : partNode.getFirstPartitionedSet().getColumns()) {

                                    Pair lp2 = new Pair(nonPartitioned, pc);
                                    if (memo.containsKey(lp2)) {

                                        existing = memo.get(lp2);
                                        // partNode=existing;
                                        exists = true;
                                        break;
                                    }
                                }
                                if (exists) {
                                    for (Node exch : partNode.getChildren()) {
                                        existing.addChild(exch);
                                    }
                                    existing.getFirstPartitionedSet()
                                        .addColumns(partNode.getFirstPartitionedSet().getColumns());
                                    eq.addToPartitionRecord(existing.getPartitionRecord());
                                    // partNode = existing;
                                } else {

                                    // Node otherjoin = new Node(Node.AND,
                                    // Node.JOIN);
                                    // otherjoin.setObject(bwc);
                                    // otherjoin.addChild(leftJoin);
                                    // otherjoin.addChild(partNode);
                                    // createEnforcerForNode(leftJoin,
                                    // otherjoin, memo, bwc.getRightOp());
                                    // otherjoin.addChild(leftJoin);
                                    // eq.addChildAt(otherjoin, 0);
                                    // i++;
                                    addRepartitionToNode(leftJoin, bwc.getLeftOp(), op, eq, memo);
                                    addRepartitionToNode(partNode, bwc.getRightOp(), op, eq, memo);
                                    for (Column pc : partNode.getFirstPartitionedSet()
                                        .getColumns()) {

                                        Pair lp2 = new Pair(nonPartitioned, pc);
                                        memo.put(lp2, partNode);
                                    }
                                }
                                // addRepartitionToPhysicalDAG(partNode, memo);

                            }
                        }
                        addRepartitionToNode(rightJoin, bwc.getRightOp(), op, eq, memo);

                    }

                }
            } else {// not join
                for (Node g : op.getChildren()) {
                    addRepartitionToPhysicalDAG(g, memo);
                    eq.addToPartitionRecord(g.getPartitionRecord());

                }
            }

            // partitionRecord.add(c.getPartitionHistory());
        }
        // e.addToPartitionHistory(getUniqueColumns(partitionRecord));
    }

    private void unify(Node q, Node q2) {
        if (q == q2) {
            return;
        }

        hashes.remove(q2.getHashId());
        hashes.remove(q.getHashId());

        for (Node c : q2.getChildren()) {
            q.addChild(c);
        }
        q2.removeAllChildren();
        for (int i = 0; i < q2.getParents().size(); i++) {
            Node p = q2.getParents().get(i);
            // System.out.println(p.getHashId());
            hashes.remove(p.getHashId());
            int pos = p.removeChild(q2);
            i--;

            p.addChildAt(q, pos);
            if (hashes.containsKey(p.getHashId())) {
                // System.out.println("further unification!");

                unify(hashes.get(p.getHashId()).getFirstParent(), p.getFirstParent());
            } else {
                hashes.put(p.getHashId(), p);
            }
            // System.out.println(p.getHashId());
        }
        hashes.put(q.getHashId(), q);
    }

    private Plan findBestPlan(Node e, int limit, HashMap<Node, Plan> memo, HashSet<Node> visited,
        CostEstimatorCaller cel) {
        if (memo.containsKey(e)) {
            // reuse!!!! cost=0!!!!

            // Plan reuse = new Plan(memo.get(e).getPath(), 0);
            // return reuse;
            if (visited.contains(e)) {
                // reuse!!!! cost=0!!!!
                Plan reuse = new Plan(memo.get(e).getPath(), 0);
                return reuse;
            } else {
                visited.add(e);
                return memo.get(e);
            }
            // return memo.get(e);
        }
        Plan bestPlan = new Plan(new PlanPath(), Integer.MAX_VALUE);
        Table t = (Table) e.getObject();
        if (!t.getName().startsWith("tabl")) {
            bestPlan.setCost(0);
        }
        for (int i = 0; i < e.getChildren().size(); i++) {
            Node op = e.getChildAt(i);
            int planCost = 0;
            Plan inpBestPlan = new Plan(new PlanPath(), Integer.MAX_VALUE);
            inpBestPlan.getPath().addOption(i);
            for (Node ei : op.getChildren()) {
                Plan inpBestPlan2 = findBestPlan(ei, limit, memo, visited, cel);
                // inpBestPlan.computeCost(ei);
                planCost += inpBestPlan2.getCost();
                Iterator<Integer> it = inpBestPlan2.getPath().getPlanIterator();
                while (it.hasNext()) {
                    inpBestPlan.getPath().addOption(it.next());
                }
            }
            planCost += cel.getCostForOperator(op, inpBestPlan, e);

            // bestPlan.computeCost(op);
            if (planCost <= bestPlan.getCost()) {
                bestPlan = inpBestPlan;
                bestPlan.setCost(planCost);
            }
        }
        visited.add(e);
        memo.put(e, bestPlan);
        return bestPlan;
    }

    private Plan addRepartitionAndComputeBestPlan(Node e, int limit,
        HashMap<Pair<Node, Column>, Pair<Node, Plan>> memo, CostEstimatorCaller cel, Column c) {
        Pair ec = new Pair(e, c);
        if (memo.containsKey(ec)) {
            Pair<Node, Plan> memoEntry = memo.get(ec);
            // reuse!!!! cost=0!!!!

            // Plan reuse = new Plan(memo.get(e).getPath(), 0);
            // return reuse;
            if (memoEntry.getVar1().isMaterialised()) {
                // reuse!!!! cost=0!!!!
                Plan reuse = new Plan(memoEntry.getVar2().getPath(), 0);
                return reuse;
            } else {
                return memoEntry.getVar2();
            }
            // return memo.get(e);
        }
        Plan p = new Plan(new PlanPath(), Integer.MAX_VALUE);

        for (int i = 0; i < e.getChildren().size(); i++) {

            Node o = e.getChildAt(i);
            // if (e.prune(o)) {
            // continue;
            // }
            if (o.getOpCode() == Node.REPARTITION) {
                break;
            }
            double planCost = 0;
            Plan p2 = new Plan(new PlanPath(), Integer.MAX_VALUE);
            p2.getPath().addOption(i);
            for (int j = 0; j < o.getChildren().size(); j++) {

                Node e2 = o.getChildAt(j);
                // ignore repartitions
                if (!(e2.getObject() instanceof Table)) {
                    break;
                }

                Column c2 = null;
                if (o.getObject() instanceof NonUnaryWhereCondition) {
                    NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o.getObject();
                    c2 = nuwc.getOperands().get(j).getAllColumnRefs().get(0);
                }
                Table t = (Table) e2.getObject();
                if (!t.getName().startsWith("tabl")) {
                    if (t.getName().startsWith("btr")) {
                        Node baseTable = e2.getChildAt(0).getChildAt(0);
                        Pair basec2 = new Pair(baseTable, c2);
                        planCost += memo.get(basec2).getVar2().getCost();
                        // Iterator<Integer> it =
                        // memo.get(e2c2).getVar2().getPath().getPlanIterator();
                        // p2.getPath().removeLastOption();
                        // while (it.hasNext()) {
                        p2.getPath().addOption(0);
                    } // what to do when c2 is null?
                    else if (c2 != null) {
                        Pair e2c2 = new Pair(e2, c2);
                        if (memo.containsKey(e2c2)) {
                            planCost += memo.get(e2c2).getVar2().getCost();
                            // Iterator<Integer> it =
                            // memo.get(e2c2).getVar2().getPath().getPlanIterator();
                            // p2.getPath().removeLastOption();
                            // while (it.hasNext()) {
                            p2.getPath().addOption(0);
                            // }
                            o.removeChildAt(j);
                            o.addChildAt(memo.get(e2c2).getVar1(), j);
                            // p2.getPath().addOption(0, 0);
                        } else {
                            Node enforcer = new Node(Node.AND);
                            enforcer.setOperator(Node.REPARTITION);
                            // c.addToPartitionHistory(bwc.getLeftOp().getAllColumnRefs());
                            enforcer.setObject(c2);
                            PlanPath pp = new PlanPath();

                            Plan baseTablePlan =
                                new Plan(pp, cel.getCostForOperator(enforcer, p, e2));
                            planCost += baseTablePlan.getCost();
                            Node enforcerParent = new Node(Node.OR);
                            enforcerParent.setObject(new Table("btr", "btr"));
                            enforcer.addChild(e2);
                            enforcerParent.addChild(enforcer);
                            o.removeChild(e2);
                            o.addChildAt(enforcerParent, j);
                            // enforcer.addChild(e2);
                            // enforcer.addChild(enforcerChild);
                            // enforcerChild.setObject(t);
                            // p2.getPath().removeOption(0);

                            // pp.addOption(o.getChildren().size()-1);
                            pp.addOption(0);
                            // p2.getPath().addOption(o.getChildren().size()-1,
                            // 0);
                            p2.getPath().addOption(0);

                            memo.put(e2c2, new Pair(enforcerParent, baseTablePlan));
                        }
                    }
                } else {
                    Plan inpBestPlan2 = addRepartitionAndComputeBestPlan(e2, limit, memo, cel, c2);
                    // inpBestPlan.computeCost(ei);
                    planCost += inpBestPlan2.getCost();
                    Iterator<Integer> it = inpBestPlan2.getPath().getPlanIterator();
                    while (it.hasNext()) {
                        p2.getPath().addOption(it.next());
                    }
                }
            }

            planCost += cel.getCostForOperator(o, p2, e);
            if (c != null && !nodeQuaranteesPartitioning(o, c)) {
                Node enforcer = new Node(Node.AND);
                enforcer.setOperator(Node.REPARTITION);
                // c.addToPartitionHistory(bwc.getLeftOp().getAllColumnRefs());
                enforcer.setObject(c);
                planCost += cel.getCostForOperator(enforcer, p2, e);
                Node enforcerChild = new Node(Node.OR);
                enforcer.addChild(enforcerChild);
                e.addChild(enforcer);
                // enforcer.addChild(enforcerChild);
                enforcerChild.addChild(o);
                p2.getPath().removeOption(0);
                p2.getPath().addOption(0, 0);
                p2.getPath().addOption(e.getChildren().size() - 1, 0);

            }
            // bestPlan.computeCost(op);
            if (planCost <= p.getCost()) {
                p = p2;
                p.setCost(planCost);
            }
            // else remove enforcer?
        }
        if (!e.getParents().isEmpty() && e.getParents().get(0).getOpCode() == Node.UNION) {
            // String g = e.dotPrint();
            e.setPlanMaterialized(p.getPath().getPlanIterator());
        }
        memo.put(ec, new Pair(e, p));
        return p;
    }

    private Iterator<PartitionCols> combineColumns(Set<PartitionCols> partitionedOn) {
        Iterator<PartitionCols> it = partitionedOn.iterator();
        List<PartitionCols> resultCols = new ArrayList<PartitionCols>();
        resultCols.add(it.next());
        while (it.hasNext()) {
            PartitionCols pc = it.next();
            // Iterator<PartitionCols> it2 = resultCols.iterator();
            for (int i = 0; i < resultCols.size(); i++) {
                PartitionCols pc2 = resultCols.get(i);
                // }){
                // while (it2.hasNext()) {
                // PartitionCols pc2 = it2.next();
                boolean toAdd = true;
                for (Column c : pc.getColumns()) {
                    for (Column c2 : pc2.getColumns()) {
                        if (c2.equals(c)) {
                            pc2.addColumns(pc.getColumns());
                            toAdd = false;
                            break;
                        }
                    }
                }
                if (toAdd && !resultCols.contains(pc)) {
                    resultCols.add(pc);
                }
            }
        }
        return resultCols.iterator();
    }

    private PartitionCols getUniqueColumns(Set<PartitionCols> partitionHistories) {
        PartitionCols unique = new PartitionCols();
        Iterator<PartitionCols> it = partitionHistories.iterator();
        PartitionCols first = it.next();
        unique.addColumns(first.getColumns());
        while (it.hasNext()) {
            PartitionCols nextPcs = it.next();
            // Iterator<PartitionCols> pcs2=nextPcs.iterator();
            unique.retainAll(nextPcs);

        }
        return unique;
    }

    private void addRepartitionToNode(Node n, Operand joinOperand, Node parent, Node gParent,
        HashMap<Pair<Node, Column>, Node> memo) {
        for (Column pc : n.getFirstPartitionedSet().getColumns()) {
            Pair lp2 = new Pair(n, pc);

            memo.put(lp2, n);
        }
        PartitionCols ptned = new PartitionCols();
        Node lEnforcerParent = new Node(Node.OR);
        if (!(n.isCentralised() || n.getFirstPartitionedSet()
            .contains((joinOperand.getAllColumnRefs().get(0))))) {
            Node lEnforcer = new Node(Node.AND);

            lEnforcer.setOperator(Node.REPARTITION);

            // c.addToPartitionHistory(bwc.getLeftOp().getAllColumnRefs());
            lEnforcer.setObject(joinOperand.getAllColumnRefs().get(0));
            Pair lp = new Pair(n, joinOperand.getAllColumnRefs().get(0));
            ptned.addColumns(joinOperand.getAllColumnRefs());
            if (memo.containsKey(lp)) {
                lEnforcerParent = memo.get(lp);
            } else {

                lEnforcerParent.setObject(new Table("table" + Util.createUniqueId(), null));
                lEnforcerParent.setPartitionedOn(ptned);

                memo.put(lp, lEnforcerParent);
            }
            lEnforcerParent.addToPartitionRecord(ptned);
            lEnforcerParent.addChild(lEnforcer);
            lEnforcer.addChild(n);
            int childNo = parent.getFirstIndexOfChild(n);
            parent.removeChild(n);
            parent.addChildAt(lEnforcerParent, childNo);
        }
        addRepartitionToPhysicalDAG(n, memo);
        gParent.addToPartitionRecord(ptned);
        gParent.addToPartitionRecord(n.getPartitionRecord());
        lEnforcerParent.addToPartitionRecord(n.getPartitionRecord());
        if (n.getPartitionRecord().contains(ptned)) {
            // do not increade counter of lEnforcerParent, only of nodes below
            // it
            n.increasePruningCounter();
            increasePrunningCounter(n, ptned);
        }
    }

    private void increasePrunningCounter(Node eq, PartitionCols ptned) {
        for (Node op : eq.getChildren()) {

            for (Node eq2 : op.getChildren()) {
                if (eq2.partitionRecordContains(ptned)) {
                    eq2.increasePruningCounter();
                    PartitionCols newCols = new PartitionCols();
                    newCols.addColumns(ptned.getColumns());
                    if (!Collections.disjoint(eq2.getFirstPartitionedSet().getColumns(),
                        newCols.getColumns())) {
                        // if there is a common column, add all eq2 partition
                        // columns to newCols
                        newCols.addColumns(eq2.getFirstPartitionedSet().getColumns());
                    }
                    increasePrunningCounter(eq2, newCols);
                }
            }
        }
    }

    public void createEnforcerForNode(Node n, Node parent, HashMap<Pair<Node, Column>, Node> memo,
        Operand joinOperand) {
        PartitionCols ptned = new PartitionCols();
        Node lEnforcerParent = new Node(Node.OR);
        if (!(n.isCentralised() || n.getFirstPartitionedSet()
            .contains((joinOperand.getAllColumnRefs().get(0))))) {
            Node lEnforcer = new Node(Node.AND);

            lEnforcer.setOperator(Node.REPARTITION);

            // c.addToPartitionHistory(bwc.getLeftOp().getAllColumnRefs());
            lEnforcer.setObject(joinOperand.getAllColumnRefs().get(0));
            Pair lp = new Pair(n, joinOperand.getAllColumnRefs().get(0));

            if (memo.containsKey(lp)) {
                lEnforcerParent = memo.get(lp);
            } else {

                ptned.addColumns(joinOperand.getAllColumnRefs());
                lEnforcerParent.setObject(new Table("table" + Util.createUniqueId(), null));
                lEnforcerParent.setPartitionedOn(ptned);
                lEnforcerParent.addToPartitionRecord(ptned);
                memo.put(lp, lEnforcerParent);
            }
            lEnforcerParent.addChild(lEnforcer);
            lEnforcer.addChild(n);
            parent.removeChild(n);
            parent.addChild(lEnforcerParent);
        }
    }

    private Node addAliasesToDAG(Node parent, List<String> firstAliases, List<String> nextAliases,
        NodeHashValues h) {
        // for(int i=0;i<parent.getChildren().size();i++){
        Node opNode = parent.getChildAt(0);

        List<Node> newChidlren = new ArrayList<Node>();
        for (Node inpEq : opNode.getChildren()) {
            Table t = (Table) inpEq.getObject();
            if (!t.getName().startsWith("table")) {
                Node newBaseTable = new Node(Node.OR);
                Table t2 =
                    new Table(t.getName(), nextAliases.get(firstAliases.indexOf(t.getAlias())));
                newBaseTable.setObject(t2);
                if (!h.containsKey(newBaseTable.getHashId())) {
                    h.put(newBaseTable.getHashId(), newBaseTable);
                    newBaseTable.addDescendantBaseTable(t2.getAlias());
                }
                newChidlren.add(h.get(newBaseTable.getHashId()));
            } else {
                Node newEqNode = new Node(Node.OR);
                newEqNode.setObject(new Table("table" + Util.createUniqueId(), null));
                newEqNode.addChild(addAliasesToDAG(inpEq, firstAliases, nextAliases, h));
                if (!h.containsKey(newEqNode.getHashId())) {
                    h.put(newEqNode.getHashId(), newEqNode);
                    for (Node n : newEqNode.getChildren()) {
                        newEqNode.addAllDescendantBaseTables(n.getDescendantBaseTables());
                    }
                } else {
                    newEqNode = h.get(newEqNode.getHashId());
                    // System.out.println("what?");
                }
                newChidlren.add(newEqNode);
            }
        }

        Operand op = (Operand) opNode.getObject();
        Node newOpNode = new Node(Node.AND, opNode.getOpCode());
        for (Node c : newChidlren) {
            newOpNode.addChild(c);
            newOpNode.addAllDescendantBaseTables(c.getDescendantBaseTables());
        }
        // newOpNode.addChild(newEqNode);
        Operand cloned = null;
        try {
            cloned = op.clone();
        } catch (CloneNotSupportedException ex) {
            java.util.logging.Logger.getLogger(QueryDecomposer.class.getName())
                .log(Level.SEVERE, null, ex);
        }
        newOpNode.setObject(cloned);
        for (Column c : cloned.getAllColumnRefs()) {
            for (int j = 0; j < firstAliases.size(); j++) {
                if (c.tableAlias.equals(firstAliases.get(j))) {
                    c.tableAlias = nextAliases.get(j);
                    break;
                }
            }
        }
        if (h.containsKey(newOpNode.getHashId())) {
            return h.get(newOpNode.getHashId());
        } else {
            h.put(newOpNode.getHashId(), newOpNode);

            return newOpNode;
        }
        // }
    }

    int total = 0;
    int pruned = 0;

    private void computePruningPercentage(Node n, HashSet<Node> visited) {

        for (Node n2 : n.getChildren()) {
            if (!visited.contains(n2)) {
                if (n2.getType() == Node.OR) {
                    total++;
                    if (n2.prune()) {
                        pruned++;
                    }
                }

                computePruningPercentage(n2, visited);
                visited.add(n2);
            }
        }

    }

    private boolean nodeQuaranteesPartitioning(Node o, Column c) {
		/*
		 * if(o.getLastPartition().contains(c)){ return true; }
		 */
        if (o.getObject() instanceof NonUnaryWhereCondition) {
            NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o.getObject();
            if (nuwc.getAllColumnRefs().contains(c)) {
                return true;
            }
        }
        return false;
    }

    private Node createRepartitionDAG2(Node e, HashMap<Pair<Node, Column>, Node> memo, Column c,
        boolean addRJoin, boolean addLBJoin, boolean addRBJoin) {
        boolean addNextR = true;
        boolean addNextLB = true;
        boolean addNextRB = true;
        Pair ec = new Pair(e, c);
        if (memo.containsKey(ec)) {
            return memo.get(ec);
        }
        PartitionCols e2partCols = new PartitionCols();
        Node np = new Node(Node.OR);
        np.setPartitionedOn(e2partCols);
        e2partCols.addColumn(c);
        // if(e.getObject().toString().startsWith("table")){
        // np.setObject(new Table("table" + Util.createUniqueId(), null));}
        // else{
        np.setObject(e.getObject());
        // }
        memo.put(ec, np);
        for (Node o : e.getChildren()) {
            if (o.getObject() instanceof NonUnaryWhereCondition) {
                NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o.getObject();
                PartitionCols oapartCols = new PartitionCols();
                if (addRJoin) {
                    Node oa = new Node(Node.AND, Node.REPARTITIONJOIN);
                    oa.setObject(nuwc);

                    oapartCols.addColumn(nuwc.getLeftOp().getAllColumnRefs().get(0));

                    oapartCols.addColumn(nuwc.getRightOp().getAllColumnRefs().get(0));
                    if (c == null || oapartCols.contains(c)) {

                        for (int i = 0; i < o.getChildren().size(); i++) {
                            Node e2 = o.getChildAt(i);
                            Column c2 = (Column) nuwc.getOp(i);
                            Node ne2 = createRepartitionDAG2(e2, memo, c2, true, true, true);
                            // PartitionCols oapartCols =
                            // ne2.getFirstPartitionedSet();
                            oa.addChild(ne2);
                            np.addChild(oa);
                            addNextR = false;
                            // to change and return also broadcast info
                        }
                    }
                }

                if (addLBJoin) {
                    oapartCols = new PartitionCols();
                    Node oa2 = new Node(Node.AND, Node.LEFTBROADCASTJOIN);
                    oa2.setObject(nuwc);
                    for (int i = 0; i < o.getChildren().size(); i++) {
                        Node e2 = o.getChildAt(i);
                        Node ne2 = null;
                        if (i == 0) {
                            ne2 = createRepartitionDAG2(e2, memo,
                                new Column("broadcast", "broadcast"), true, true, true);
                        } else {
                            ne2 = createRepartitionDAG2(e2, memo, null, true, true, true);
                            oapartCols = ne2.getFirstPartitionedSet();
                        }

                        if (c == null || oapartCols.contains(c) || oapartCols
                            .contains(new Column("broadcast", "broadcast"))) {
                            oa2.addChild(ne2);
                            np.addChild(oa2);
                            addNextLB = false;
                        }
                        // to change and return also broadcast info
                    }
                }
                oapartCols = new PartitionCols();
                if (addRBJoin) {
                    Node oa3 = new Node(Node.AND, Node.RIGHTBROADCASTJOIN);
                    oa3.setObject(nuwc);
                    for (int i = 0; i < o.getChildren().size(); i++) {
                        Node e2 = o.getChildAt(i);
                        Node ne2 = null;
                        if (i == 0) {
                            ne2 = createRepartitionDAG2(e2, memo, null, true, true, true);
                            oapartCols = ne2.getFirstPartitionedSet();
                        } else {
                            ne2 = createRepartitionDAG2(e2, memo,
                                new Column("broadcast", "broadcast"), true, true, true);
                        }
                        if (c == null || oapartCols.contains(c) || oapartCols
                            .contains(new Column("broadcast", "broadcast"))) {
                            oa3.addChild(ne2);
                            np.addChild(oa3);
                            addNextRB = false;
                        }
                        // to change and return also broadcast info
                    }
                }

            } else {
                Node oa = new Node(Node.AND, o.getOpCode());
                oa.setObject(o.getObject());

                for (int i = 0; i < o.getChildren().size(); i++) {
                    Node e2 = o.getChildAt(i);
                    Column c2 = null;
                    Node ne2 = createRepartitionDAG2(e2, memo, c2, true, true, true);
                    PartitionCols oapartCols = ne2.getFirstPartitionedSet();
                    if (c == null || oapartCols.contains(c) || oapartCols
                        .contains(new Column("broadcast", "broadcast"))) {
                        oa.addChild(ne2);
                        np.addChild(oa);
                    }
                    // to change and return also broadcast info
                }
            }
        }
        // enforcers
        if (c != null) {
            if (c.equals(new Column("broadcast", "broadcast"))) {
                // create broadcast op
                Node of2 = new Node(Node.AND, Node.BROADCAST);
                of2.setObject("broadcast");
                np.addChild(of2);
                of2.addChild(createRepartitionDAG2(e, memo, null, addNextR, addNextLB, addNextRB));
            } else {
                // create repartition op
                Node of = new Node(Node.AND, Node.REPARTITION);
                of.setObject(c);
                np.addChild(of);
                of.addChild(createRepartitionDAG2(e, memo, null, addNextR, addNextLB, addNextRB));
            }

        }
        return np;
    }

    private SinglePlan getBestPlan(Node e, Column c, double limit, double repCost,
        EquivalentColumnClasses partitionRecord, List<MemoKey> toMaterialize) {
        MemoKey ec = new MemoKey(e, c);
        SinglePlan resultPlan;
        if (memo.containsMemoKey(ec) && memo.getMemoValue(ec).isMaterialised()) {
            // check on c!
            resultPlan = new SinglePlan(0.0, null);
            PartitionedMemoValue pmv = (PartitionedMemoValue) memo.getMemoValue(ec);
            partitionRecord.setLastPartitioned(pmv.getDlvdPart());
        } else if (memo.containsMemoKey(ec)) {
            resultPlan = memo.getMemoValue(ec).getPlan();
            PartitionedMemoValue pmv = (PartitionedMemoValue) memo.getMemoValue(ec);
            partitionRecord.setLastPartitioned(pmv.getDlvdPart());
        } else {
            resultPlan = searchForBestPlan(e, c, limit, repCost, partitionRecord, toMaterialize);
        }
        if (resultPlan != null && resultPlan.getCost() < limit) {
            return resultPlan;
        } else {
            return null;
        }
    }

    private SinglePlan searchForBestPlan(Node e, Column c, double limit, double repCost,
        EquivalentColumnClasses partitionRecord, List<MemoKey> toMaterialize) {

        if (!e.getObject().toString().startsWith("table")) {
            // base table
            SinglePlan r = new SinglePlan(0);
            memo.put(e, r, c, repCost, true, null);
            return r;
        }

        SinglePlan resultPlan = new SinglePlan(Integer.MAX_VALUE);
        double repartitionCost = 0;
        if (c != null) {
            repartitionCost = NodeCostEstimator.estimateRepartition(e, c);
        }
		/*
		 * PartitionCols e2partCols = new PartitionCols(); Node np = new
		 * Node(Node.OR); np.setPartitionedOn(e2partCols);
		 * e2partCols.addColumn(c); //
		 * if(e.getObject().toString().startsWith("table")){ // np.setObject(new
		 * Table("table" + Util.createUniqueId(), null));} // else{
		 * np.setObject(e.getObject()); // }
		 */
        // memo.put(ec, np);
        // e2Plan;
        // for (Node o : e.getChildren()) {
        for (int k = 0; k < e.getChildren().size(); k++) {
            EquivalentColumnClasses e2RecordCloned = partitionRecord.shallowCopy();
            Node o = e.getChildAt(k);
            SinglePlan e2Plan = new SinglePlan(Integer.MAX_VALUE);
            Double opCost = NodeCostEstimator.getCostForOperator(o, e);
            if (o.getOpCode() == Node.JOIN) {
                NonUnaryWhereCondition join = (NonUnaryWhereCondition) o.getObject();
                e2RecordCloned.mergePartitionRecords(join);
            }
            List<MemoKey> toMatE2 = new ArrayList<MemoKey>();
            // this must go after algorithmic implementation
            limit -= opCost;
            double algLimit;
            EquivalentColumnClasses algRecordCloned;
            int cComesFromChildNo = -1;
            for (int m = 0; m < o.getAlgorithmicImplementations().length; m++) {

                int a = o.getAlgorithmicImplementations()[m];

                int retainsPartition = -1;
                retainsPartition = getRetainsPartition(a);
                PartitionCols returnedPt = null;
                algRecordCloned = e2RecordCloned.shallowCopy();
                algLimit = limit;
                List<MemoKey> toMatAlg = new ArrayList<MemoKey>();

                SinglePlan algPlan = new SinglePlan(opCost);
                algPlan.setChoice(k);
                if (c != null && guaranteesResultPtnedOn(a, o, c)) {
                    algRecordCloned.setLastPartitioned(algRecordCloned.getClassForColumn(c));
                }
                for (int i = 0; i < o.getChildren().size(); i++) {
                    EquivalentColumnClasses oRecord = algRecordCloned.shallowCopy();
                    Node e2 = o.getChildAt(i);
                    if (m == 0 && c != null && cComesFromChildNo < 0) {
                        if (e2.isDescendantOfBaseTable(c.tableAlias)) {
                            cComesFromChildNo = i;
                        }
                    }

                    // double minRepCost = repCost < repartitionCost ?
                    // repCost:repCost;
                    Column c2 = getPartitionRequired(a, o, i);
                    Double c2RepCost = 0.0;
                    if (c2 != null) {
                        NodeCostEstimator.estimateRepartition(e2, c2);
                    }

                    if (c == null || cComesFromChildNo != i || guaranteesResultPtnedOn(a, o, c)) {
                        // algPlan.append(getBestPlan(e2, c2, memo, algLimit,
                        // c2RepCost, cel, partitionRecord, toMatAlg));
                        SinglePlan t = getBestPlan(e2, c2, algLimit, c2RepCost, oRecord, toMatAlg);
                        algPlan.addInputPlan(e2, c2);
                        algPlan.increaseCost(t.getCost());
                    } else if (guaranteesResultNotPtnedOn(a, o, c)) {

                        if (repartitionCost < repCost) {
                            algPlan.addRepartitionBeforeOp(c);
                            // algPlan.getPath().addOption(-1);
                            oRecord.setClassRepartitioned(c, true);
                            toMatAlg.add(new MemoKey(e, c));
                            algLimit -= repartitionCost;
                        } else {
                            oRecord.setClassRepartitioned(c, false);
                        }
                        // algPlan.append(getBestPlan(e2, c2, memo, algLimit,
                        // c2RepCost, cel, partitionRecord, toMatAlg));
                        SinglePlan t = getBestPlan(e2, c2, algLimit, c2RepCost, oRecord, toMatAlg);
                        algPlan.addInputPlan(e2, c2);
                        algPlan.increaseCost(t.getCost());
                    } else {
                        // algPlan.append(getBestPlan(e2, c, memo, algLimit,
                        // c2RepCost, cel, partitionRecord, toMatAlg));
                        SinglePlan t = getBestPlan(e2, c, algLimit, c2RepCost, oRecord, toMatAlg);
                        algPlan.addInputPlan(e2, c);
                        algPlan.increaseCost(t.getCost());
                        if (oRecord.getLast() == null || (!oRecord.getLast().contains(c)
                            && repartitionCost < repCost)) {
                            algPlan.addRepartitionBeforeOp(c);
                            // algPlan.getPath().addOption(-1);
                            oRecord.setClassRepartitioned(c, true);
                            toMatAlg.add(new MemoKey(e, c));
                            algLimit -= repartitionCost;
                        }
                        algLimit -= algPlan.getCost();
                    }
                    if (c2 != null) {
                        if (oRecord.getLast() == null || !oRecord.getLast().contains(c2)) {
                            // algPlan.getPath().addOption(-1);
                            algPlan.addRepartitionAfterOp(i, c2);
                            if (algPlan.getRepartitionBeforeOp() != null) {
                                oRecord.setClassRepartitioned(c2, false);
                            } else {
                                oRecord.setClassRepartitioned(c2, true);
                            }
                            toMatAlg.add(new MemoKey(e2, c2));
                            algLimit -= c2RepCost;
                        }
                    }
                    double e2PlanCost = algPlan.getCost();
                    if (c != null) {
                        if (oRecord.getLast() == null || !oRecord.getLast().contains(c)) {
                            e2PlanCost += repartitionCost;
                        }
                    }
                    algLimit -= e2PlanCost;
                    // algRecordCloned.addClassesFrom(oRecord);
                    if (i == retainsPartition) {
                        returnedPt = oRecord.getLast();
                    }
                }
                if (returnedPt != null) {
                    algRecordCloned.setLastPartitioned(returnedPt);
                }
                if (algPlan.getCost() < e2Plan.getCost()) {
                    e2Plan = algPlan;
                    toMatE2.addAll(toMatAlg);
                    e2RecordCloned = algRecordCloned;
                }
            }
            if (e2Plan.getCost() < resultPlan.getCost()) {
                resultPlan = e2Plan;
                toMaterialize.addAll(toMatE2);
                partitionRecord.copyFrom(e2RecordCloned);
                memo.put(e, resultPlan, c, repCost, e2RecordCloned.getLast());
            }
        }
        if (!e.getParents().isEmpty() && e.getParents().get(0).getOpCode() == Node.UNION) {
            // String g = e.dotPrint();
            for (MemoKey mv : toMaterialize) {
                memo.getMemoValue(mv).setMaterialized(true);
            }
            toMaterialize.clear();
            // e.setPlanMaterialized(resultPlan.getPath().getPlanIterator());
        }
        return resultPlan;

    }

    private SinglePlan getBestPlanPruned(Node e, Column c, double limit, double repCost,
        EquivalentColumnClasses partitionRecord, List<MemoKey> toMaterialize) {
        if (limits.containsKey(e)) {
            if (limits.get(e) > limit - repCost) {
                return null;
            }
        }
        MemoKey ec = new MemoKey(e, c);
        SinglePlan resultPlan;
        if (memo.containsMemoKey(ec) && memo.getMemoValue(ec).isMaterialised()) {
            // check on c!
            resultPlan = new SinglePlan(0.0, null);
            PartitionedMemoValue pmv = (PartitionedMemoValue) memo.getMemoValue(ec);
            partitionRecord.setLastPartitioned(pmv.getDlvdPart());
        } else if (memo.containsMemoKey(ec)) {
            resultPlan = memo.getMemoValue(ec).getPlan();
            PartitionedMemoValue pmv = (PartitionedMemoValue) memo.getMemoValue(ec);
            partitionRecord.setLastPartitioned(pmv.getDlvdPart());
        } else {
            resultPlan =
                searchForBestPlanPruned(e, c, limit, repCost, partitionRecord, toMaterialize);
        }
        // if (resultPlan != null && resultPlan.getCost() < limit) {
        return resultPlan;
        // } else {
        // return null;
        // }
    }

    private SinglePlan searchForBestPlanPruned(Node e, Column c, double limit, double repCost,
        EquivalentColumnClasses partitionRecord, List<MemoKey> toMaterialize) {

        if (!e.getObject().toString().startsWith("table")) {
            // base table
            SinglePlan r = new SinglePlan(0);
            memo.put(e, r, c, repCost, true, null);
            return r;
        }

        SinglePlan resultPlan = null;
        double repartitionCost = 0;
        if (c != null) {
            repartitionCost = NodeCostEstimator.estimateRepartition(e, c);
        }
		/*
		 * PartitionCols e2partCols = new PartitionCols(); Node np = new
		 * Node(Node.OR); np.setPartitionedOn(e2partCols);
		 * e2partCols.addColumn(c); //
		 * if(e.getObject().toString().startsWith("table")){ // np.setObject(new
		 * Table("table" + Util.createUniqueId(), null));} // else{
		 * np.setObject(e.getObject()); // }
		 */
        // memo.put(ec, np);
        // e2Plan;
        // for (Node o : e.getChildren()) {
        for (int k = 0; k < e.getChildren().size(); k++) {
            EquivalentColumnClasses e2RecordCloned = partitionRecord.shallowCopy();
            Node o = e.getChildAt(k);

            Double opCost = NodeCostEstimator.getCostForOperator(o, e);
            SinglePlan e2Plan = null;
            // this must go after algorithmic implementation
            double newLimit = limit - opCost;
            if (newLimit < 0) {
                continue;
            }
            if (o.getOpCode() == Node.JOIN) {
                NonUnaryWhereCondition join = (NonUnaryWhereCondition) o.getObject();
                e2RecordCloned.mergePartitionRecords(join);
            }
            List<MemoKey> toMatE2 = new ArrayList<MemoKey>();

            double algLimit;
            EquivalentColumnClasses algRecordCloned;
            int cComesFromChildNo = -1;
            for (int m = 0; m < o.getAlgorithmicImplementations().length; m++) {

                int a = o.getAlgorithmicImplementations()[m];

                int retainsPartition = -1;
                retainsPartition = getRetainsPartition(a);
                PartitionCols returnedPt = null;
                algRecordCloned = e2RecordCloned.shallowCopy();
                algLimit = newLimit;
                List<MemoKey> toMatAlg = new ArrayList<MemoKey>();

                SinglePlan algPlan = new SinglePlan(opCost);
                algPlan.setChoice(k);
                if (c != null && guaranteesResultPtnedOn(a, o, c)) {
                    algRecordCloned.setLastPartitioned(algRecordCloned.getClassForColumn(c));
                }
                for (int i = 0; i < o.getChildren().size(); i++) {
                    EquivalentColumnClasses oRecord = algRecordCloned.shallowCopy();
                    Node e2 = o.getChildAt(i);
                    if (m == 0 && c != null && cComesFromChildNo < 0) {
                        if (e2.isDescendantOfBaseTable(c.tableAlias)) {
                            cComesFromChildNo = i;
                        }
                    }

                    // double minRepCost = repCost < repartitionCost ?
                    // repCost:repCost;
                    Column c2 = getPartitionRequired(a, o, i);
                    Double c2RepCost = 0.0;
                    if (c2 != null) {
                        c2RepCost = NodeCostEstimator.estimateRepartition(e2, c2);
                    }

                    if (c == null || cComesFromChildNo != i || guaranteesResultPtnedOn(a, o, c)) {
                        // algPlan.append(getBestPlan(e2, c2, memo, algLimit,
                        // c2RepCost, cel, partitionRecord, toMatAlg));
                        SinglePlan t =
                            getBestPlanPruned(e2, c2, algLimit, c2RepCost, oRecord, toMatAlg);
                        if (t == null) {
                            continue;
                        }
                        algPlan.addInputPlan(e2, c2);
                        algPlan.increaseCost(t.getCost());
                        algLimit -= t.getCost();
                    } else if (guaranteesResultNotPtnedOn(a, o, c)) {

                        if (repartitionCost < repCost) {
                            algPlan.addRepartitionBeforeOp(c);
                            algLimit -= repartitionCost;

                            if (algLimit < 0) {
                                continue;
                            }
                            algPlan.increaseCost(repartitionCost);
                            // algPlan.getPath().addOption(-1);
                            oRecord.setClassRepartitioned(c, true);
                            toMatAlg.add(new MemoKey(e, c));

                        } else {
                            algLimit -= repCost;
                            if (algLimit < 0) {
                                continue;
                            }
                            algPlan.increaseCost(repCost);
                            oRecord.setClassRepartitioned(c, false);
                        }
                        // algPlan.append(getBestPlan(e2, c2, memo, algLimit,
                        // c2RepCost, cel, partitionRecord, toMatAlg));
                        SinglePlan t =
                            getBestPlanPruned(e2, c2, algLimit, c2RepCost, oRecord, toMatAlg);
                        if (t == null) {
                            continue;
                        }
                        algPlan.addInputPlan(e2, c2);
                        algPlan.increaseCost(t.getCost());
                        algLimit -= t.getCost();
                    } else {
                        // algPlan.append(getBestPlan(e2, c, memo, algLimit,
                        // c2RepCost, cel, partitionRecord, toMatAlg));
                        SinglePlan t =
                            getBestPlanPruned(e2, c, algLimit, c2RepCost, oRecord, toMatAlg);
                        if (t == null) {
                            continue;
                        }
                        algPlan.addInputPlan(e2, c);
                        algPlan.increaseCost(t.getCost());
                        algLimit -= t.getCost();
                        if (oRecord.getLast() == null || (!oRecord.getLast().contains(c)
                            && repartitionCost < repCost)) {

                            // here do not add repCost. it has been added before
                            algPlan.addRepartitionBeforeOp(c);
                            // algPlan.getPath().addOption(-1);
                            oRecord.setClassRepartitioned(c, true);
                            toMatAlg.add(new MemoKey(e, c));
                            // algLimit -= repartitionCost;
                        }
                        // algLimit -= algPlan.getCost();
                    }
                    if (c2 != null) {
                        if (oRecord.getLast() == null || !oRecord.getLast().contains(c2)) {
                            // algPlan.getPath().addOption(-1);
                            algPlan.addRepartitionAfterOp(i, c2);
                            if (algPlan.getRepartitionBeforeOp() != null) {
                                oRecord.setClassRepartitioned(c2, false);
                            } else {
                                oRecord.setClassRepartitioned(c2, true);
                            }
                            toMatAlg.add(new MemoKey(e2, c2));
                            algLimit -= c2RepCost;
                            if (algLimit < 0) {
                                continue;
                            }
                            algPlan.increaseCost(c2RepCost);
                        }
                    }
                    // double e2PlanCost = algPlan.getCost();
                    if (c != null) {
                        if (oRecord.getLast() == null || !oRecord.getLast().contains(c)) {
                            algLimit -= repartitionCost;
                            if (algLimit < 0) {
                                continue;
                            }
                            // e2PlanCost += repartitionCost;
                        }
                    }
                    // algLimit -= e2PlanCost;
                    // algRecordCloned.addClassesFrom(oRecord);
                    if (i == retainsPartition) {
                        returnedPt = oRecord.getLast();
                    }
                }
                if (returnedPt != null) {
                    algRecordCloned.setLastPartitioned(returnedPt);
                }
                if (e2Plan == null || algPlan.getCost() < e2Plan.getCost()) {
                    e2Plan = algPlan;
                    // toMatE2.clear();
                    toMatE2.addAll(toMatAlg);
                    e2RecordCloned = algRecordCloned;
                }
            }
            if (resultPlan == null || e2Plan.getCost() < resultPlan.getCost()) {
                resultPlan = e2Plan;
                // toMaterialize.clear();
                toMaterialize.addAll(toMatE2);
                partitionRecord.copyFrom(e2RecordCloned);
                memo.put(e, resultPlan, c, repCost, e2RecordCloned.getLast());
                if (!e.getParents().isEmpty() && e.getParents().get(0).getOpCode() == Node.UNION) {

                    limit = resultPlan.getCost();
                    System.out.println("prune: " + e.getObject() + "with limit:" + limit);
                }
            }
        }
        if (!e.getParents().isEmpty() && e.getParents().get(0).getOpCode() == Node.UNION) {
            // what about other union (alias?)
            // String g = e.dotPrint();
            for (MemoKey mv : toMaterialize) {
                memo.getMemoValue(mv).setMaterialized(true);
            }
            toMaterialize.clear();
            //
            // e.setPlanMaterialized(resultPlan.getPath().getPlanIterator());
        }
        if (resultPlan == null) {
            System.out.println("pruned!!!");
            limits.put(e, limit - repCost);
        }
        return resultPlan;

    }

    private SinglePlan getBestPlanCentralized(Node e, double limit) {
        MemoKey ec = new MemoKey(e, null);
        SinglePlan resultPlan;
        if (memo.containsMemoKey(ec) && memo.getMemoValue(ec).isMaterialised()) {
            // check on c!
            resultPlan = new SinglePlan(0.0, null);
        } else if (memo.containsMemoKey(ec)) {
            CentralizedMemoValue cmv = (CentralizedMemoValue) memo.getMemoValue(ec);
            if (cmv.isUsed()) {
                // used for second time, consider materialised
                cmv.setMaterialized(true);
                resultPlan = new SinglePlan(0.0, null);
            } else {
                resultPlan = memo.getMemoValue(ec).getPlan();
            }
        } else {
            resultPlan = searchForBestPlanCentralized(e, limit);
        }
        if (resultPlan != null && resultPlan.getCost() < limit) {
            return resultPlan;
        } else {
            return null;
        }
    }

    private SinglePlan searchForBestPlanCentralized(Node e, double limit) {
        boolean fed = false;
        if (!e.getObject().toString().startsWith("table")) {
            // base table
            Table t = (Table) e.getObject();
            SinglePlan r = new SinglePlan(0);
            if (t.isFederated()) {
                memo.put(e, r, true, true, true);
            } else {
                memo.put(e, r, true, true, false);
            }

            return r;
        }

        SinglePlan resultPlan = new SinglePlan(Integer.MAX_VALUE);

        for (int k = 0; k < e.getChildren().size(); k++) {
            Node o = e.getChildAt(k);
            SinglePlan e2Plan = new SinglePlan(Integer.MAX_VALUE);
            Double opCost = NodeCostEstimator.getCostForOperator(o, e);

            // this must go after algorithmic implementation
            limit -= opCost;
            double algLimit;
            // int cComesFromChildNo = -1;
            // for (int m = 0; m < o.getAlgorithmicImplementations().length;
            // m++) {

            // int a = o.getAlgorithmicImplementations()[m];
            algLimit = limit;

            SinglePlan algPlan = new SinglePlan(opCost);
            algPlan.setChoice(k);
            for (int i = 0; i < o.getChildren().size(); i++) {
                Node e2 = o.getChildAt(i);

                // algPlan.append(getBestPlan(e2, c2, memo, algLimit, c2RepCost,
                // cel, partitionRecord, toMatAlg));
                SinglePlan t = getBestPlanCentralized(e2, algLimit);
                algPlan.addInputPlan(e2, null);
                algPlan.increaseCost(t.getCost());
                CentralizedMemoValue cmv =
                    (CentralizedMemoValue) memo.getMemoValue(new MemoKey(e2, null));
                if (cmv.isFederated()) {
                    if (o.getOpCode() == Node.JOIN) {
                        cmv.setMaterialized(true);
                    } else {
                        fed = true;
                    }
                }

                algLimit -= algPlan.getCost();

            }

            if (algPlan.getCost() < e2Plan.getCost()) {
                e2Plan = algPlan;
            }

            // }
            if (e2Plan.getCost() < resultPlan.getCost()) {
                resultPlan = e2Plan;
                memo.put(e, resultPlan, false, fed);

            }
        }
        if (!e.getParents().isEmpty() && e.getParents().get(0).getOpCode() == Node.UNION) {
            // String g = e.dotPrint();
            memo.setPlanUsed(new MemoKey(e, null));
            // e.setPlanMaterialized(resultPlan.getPath().getPlanIterator());
        }
        return resultPlan;

    }

    private Node createRepartitionDAG(Node e, HashMap<Pair<Node, Column>, Node> memo, Column c,
        HashMap<Node, Node> broadcast) {
        Pair ec = new Pair(e, c);
        if (memo.containsKey(ec)) {
            return memo.get(ec);
        }
        Node ne = new Node(Node.OR);
        ne.setObject(e.getObject());
        PartitionCols e2partCols = new PartitionCols();
        // ne.setPartitionedOn(new PartitionCols(c));
        memo.put(ec, ne);
        if (e.getChildren().isEmpty()) {
            // base table
            return baseTableNode(c, memo, broadcast, e);

        }
        for (Node o : e.getChildren()) {
            Node no = new Node(Node.AND, o.getOpCode());
            no.setObject(o.getObject());
            if (!(o.getObject() instanceof NonUnaryWhereCondition)) {

                for (int i = 0; i < o.getChildren().size(); i++) {
                    Node e2 = o.getChildAt(i);
                    Column c2 = null;
                    Node ne2 = createRepartitionDAG(e2, memo, null, broadcast);
                    no.addChild(ne2);
                    e2partCols = ne2.getFirstPartitionedSet();
                    // to change and return also broadcast info
                }
                createEnforcers(c, memo, broadcast, e2partCols, ne, no, e);
            } else {

                for (int i = 0; i < o.getChildren().size(); i++) {
                    Node e2 = o.getChildAt(i);
                    Column c2 = null;
                    NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o.getObject();
                    if (i == 0) {
                        e2partCols.addColumn(nuwc.getLeftOp().getAllColumnRefs().get(0));
                        c2 = nuwc.getLeftOp().getAllColumnRefs().get(0);
                    } else if (i == 1) {
                        e2partCols.addColumn(nuwc.getRightOp().getAllColumnRefs().get(0));
                        c2 = nuwc.getRightOp().getAllColumnRefs().get(0);
                    }
                    Node ne2 = createRepartitionDAG(e2, memo, c2, broadcast);
                    no.addChild(ne2);
                }
                createEnforcers(c, memo, broadcast, e2partCols, ne, no, e);
            }

        }
        ne.setPartitionedOn(e2partCols);
        for (Column pc : e2partCols.getColumns()) {
            memo.put(new Pair(e, pc), ne);
        }

        return ne;
    }

    private Node baseTableNode(Column c, HashMap<Pair<Node, Column>, Node> memo,
        HashMap<Node, Node> broadcast, Node e) {
        Node repartition = new Node(Node.AND, Node.REPARTITION);
        repartition.setObject(c);
        Node repParent = new Node(Node.OR);
        repParent.addChild(repartition);
        repartition.addChild(e);
        memo.put(new Pair(e, c), repParent);
        Node broadcastNode = new Node(Node.AND, Node.BROADCAST);
        broadcastNode.setObject("broadcast");
        Node boradCastParent = new Node(Node.OR);
        boradCastParent.addChild(broadcastNode);
        if (broadcast.containsKey(e)) {
            boradCastParent = broadcast.get(e);
        } else {
            broadcast.put(e, boradCastParent);
            broadcastNode.addChild(e);
        }
        Node noOp = new Node(Node.AND, Node.NO_OP);
        noOp.setObject("no_op");
        noOp.addChild(boradCastParent);
        repParent.addChild(noOp);
        return repParent;
    }

    private void createEnforcers(Column c, HashMap<Pair<Node, Column>, Node> memo,
        HashMap<Node, Node> broadcast, PartitionCols e2partCols, Node ne, Node no, Node e) {
        if (c != null && !e2partCols.contains(c)) {
            // also check for dependencies
            // what about rep history
            // create enforcers
            Node repartition = new Node(Node.AND, Node.REPARTITION);
            repartition.setObject(c);
            Node repChild = new Node(Node.OR);
            repChild.addChild(no);
            if (memo.containsKey(new Pair(e, e2partCols.getFirstCol()))) {
                repChild = memo.get(new Pair(e, e2partCols.getFirstCol()));
            }

            repartition.addChild(repChild);
            ne.addChild(repartition);
            Node broadcastNode = new Node(Node.AND, Node.BROADCAST);
            broadcastNode.setObject("broadcast");
            Node boradCastParent = new Node(Node.OR);
            boradCastParent.addChild(broadcastNode);
            if (broadcast.containsKey(repChild)) {
                boradCastParent = broadcast.get(repChild);
            } else {
                broadcast.put(repChild, boradCastParent);
                broadcastNode.addChild(repChild);
            }
            Node noOp = new Node(Node.AND, Node.NO_OP);
            noOp.setObject("no_op");
            noOp.addChild(boradCastParent);
            ne.addChild(noOp);
            // ne.addChild(broadcastNode);

        } else {
            ne.addChild(no);

        }
    }

    private Column getPartitionRequired(int a, Node o, int i) {
        if (o.getOpCode() == Node.JOIN) {
            NonUnaryWhereCondition join = (NonUnaryWhereCondition) o.getObject();
            if (o.getChildren().size() == 1) {
                // filter join
                return null;
            }
            if (a == Node.REPARTITIONJOIN) {
                return join.getOp(i).getAllColumnRefs().get(0);
            } else if (a == Node.LEFTBROADCASTJOIN) {
                if (i == 0) {
                    return join.getLeftOp().getAllColumnRefs().get(0);
                } else {
                    return null;
                }
            } else {
                if (i == 1) {
                    return join.getRightOp().getAllColumnRefs().get(0);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private boolean guaranteesResultPtnedOn(int a, Node o, Column c) {
        if (o.getOpCode() == Node.JOIN) {

            if (a == Node.REPARTITIONJOIN) {
                NonUnaryWhereCondition join = (NonUnaryWhereCondition) o.getObject();
                return c.equals(join.getOp(0).getAllColumnRefs().get(0)) || c
                    .equals(join.getOp(1).getAllColumnRefs().get(0));
            } else {
                return false;
            }
			/*
			 * else if(a==Node.LEFTBROADCASTJOIN){ return
			 * c.equals(join.getOp(0).getAllColumnRefs().get(0)); } else{ return
			 * c.equals(join.getOp(1).getAllColumnRefs().get(0)); }
			 */
        } else {
            return false;
        }
    }

    private boolean guaranteesResultNotPtnedOn(int a, Node o, Column c) {
        if (o.getOpCode() == Node.JOIN) {

            if (a == Node.REPARTITIONJOIN) {
                NonUnaryWhereCondition join = (NonUnaryWhereCondition) o.getObject();
                return !(c.equals(join.getOp(0).getAllColumnRefs().get(0)) || c
                    .equals(join.getOp(1).getAllColumnRefs().get(0)));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private int getRetainsPartition(int a) {
        if (a == Node.PROJECT || a == Node.SELECT) {
            return 0;
        } else {
            return -1;
        }

    }

    private void createProjections(Node e) {
        for (String t : this.refCols.keySet()) {
            for (String alias : n2a.getAllAliasesForBaseTable(t)) {
                Node table = new Node(Node.OR);
                table.setObject(new Table(t, alias));
                Node tableInHashes = hashes.get(table.getHashId());
                if (tableInHashes == null) {
                    System.out.println("not found");
                } else {
                    Node project;
                    Node orNode;
                    if (tableInHashes.getParents().size() == 1
                        && tableInHashes.getFirstParent().getOpCode() == Node.PROJECT) {
                        project = tableInHashes.getFirstParent();
                        orNode = project.getFirstParent();
                        Projection prj = (Projection) project.getObject();
                        hashes.remove(project.getHashId());
                        hashes.remove(project.getHashId());
                        for (String c : refCols.get(t)) {
                            Column toAdd = new Column(alias, c);
                            if (!prj.getAllColumnRefs().contains(toAdd))
                                prj.addOperand(new Output(c, toAdd));
                        }

                    } else {
                        orNode = new Node(Node.OR);
                        orNode.setObject(new Table("table" + Util.createUniqueId(), null));
                        project = new Node(Node.AND, Node.PROJECT);
                        orNode.addChild(project);
                        Projection prj = new Projection();
                        for (String c : refCols.get(t)) {
                            prj.addOperand(new Output(c, new Column(alias, c)));
                        }
                        project.setObject(prj);
                        while (!tableInHashes.getParents().isEmpty()) {
                            Node p = tableInHashes.getFirstParent();
                            tableInHashes.getParents().remove(0);
                            int childNo = p.getChildren().indexOf(tableInHashes);
                            this.hashes.remove(p.getHashId());
                            p.removeChild(tableInHashes);
                            p.addChildAt(orNode, childNo);
                            this.hashes.put(p.getHashId(), p);
                        }
                        project.addChild(tableInHashes);
                    }
                    this.hashes.put(project.getHashId(), project);
                    this.hashes.put(orNode.getHashId(), orNode);
                    project.addDescendantBaseTable(alias);
                    orNode.addDescendantBaseTable(alias);

                }
            }
        }
    }

    private SinglePlan getBestPlanPrunedNoMat(Node e, Column c, double limit, double repCost,
        EquivalentColumnClasses partitionRecord) {
        if (limits.containsKey(e)) {
            if (limits.get(e) > limit - repCost) {
                return null;
            }
        }
        MemoKey ec = new MemoKey(e, c);
        SinglePlan resultPlan;
        if (memo.containsMemoKey(ec)) {
            PartitionedMemoValue pmv = (PartitionedMemoValue) memo.getMemoValue(ec);
            if (pmv.getRepCost() > repCost) {
                resultPlan = searchForBestPlanPrunedNoMat(e, c, limit, repCost, partitionRecord);
            } else {
                resultPlan = memo.getMemoValue(ec).getPlan();
                partitionRecord.setLastPartitioned(pmv.getDlvdPart());
            }

        } else {
            resultPlan = searchForBestPlanPrunedNoMat(e, c, limit, repCost, partitionRecord);
        }
        if (resultPlan != null && resultPlan.getCost() < limit) {
            return resultPlan;
        } else {
            return null;
        }
    }

    private SinglePlan searchForBestPlanPrunedNoMat(Node e, Column c, double limit, double repCost,
        EquivalentColumnClasses partitionRecord) {

        if (!e.getObject().toString().startsWith("table")) {
            // base table
            SinglePlan r = new SinglePlan(0);
            memo.put(e, r, c, repCost, true, null);
            return r;
        }

        SinglePlan resultPlan = null;
        double repartitionCost = 0;
        if (c != null) {
            repartitionCost = NodeCostEstimator.estimateRepartition(e, c);
        }
		/*
		 * PartitionCols e2partCols = new PartitionCols(); Node np = new
		 * Node(Node.OR); np.setPartitionedOn(e2partCols);
		 * e2partCols.addColumn(c); //
		 * if(e.getObject().toString().startsWith("table")){ // np.setObject(new
		 * Table("table" + Util.createUniqueId(), null));} // else{
		 * np.setObject(e.getObject()); // }
		 */
        // memo.put(ec, np);
        // e2Plan;
        // for (Node o : e.getChildren()) {
        for (int k = 0; k < e.getChildren().size(); k++) {
            EquivalentColumnClasses e2RecordCloned = partitionRecord.shallowCopy();
            Node o = e.getChildAt(k);

            Double opCost = NodeCostEstimator.getCostForOperator(o, e);
            SinglePlan e2Plan = null;
            // this must go after algorithmic implementation
            double newLimit = limit - opCost;
            if (newLimit < 0) {
                continue;
            }
            if (o.getOpCode() == Node.JOIN) {
                NonUnaryWhereCondition join = (NonUnaryWhereCondition) o.getObject();
                e2RecordCloned.mergePartitionRecords(join);
            }

            double algLimit;
            EquivalentColumnClasses algRecordCloned;
            int cComesFromChildNo = -1;
            for (int m = 0; m < o.getAlgorithmicImplementations().length; m++) {

                int a = o.getAlgorithmicImplementations()[m];

                int retainsPartition = -1;
                retainsPartition = getRetainsPartition(a);
                PartitionCols returnedPt = null;
                algRecordCloned = e2RecordCloned.shallowCopy();
                algLimit = newLimit;

                SinglePlan algPlan = new SinglePlan(opCost);
                algPlan.setChoice(k);
                if (c != null && guaranteesResultPtnedOn(a, o, c)) {
                    algRecordCloned.setLastPartitioned(algRecordCloned.getClassForColumn(c));
                }
                for (int i = 0; i < o.getChildren().size(); i++) {
                    EquivalentColumnClasses oRecord = algRecordCloned.shallowCopy();
                    Node e2 = o.getChildAt(i);
                    if (m == 0 && c != null && cComesFromChildNo < 0) {
                        if (e2.isDescendantOfBaseTable(c.tableAlias)) {
                            cComesFromChildNo = i;
                        }
                    }

                    // double minRepCost = repCost < repartitionCost ?
                    // repCost:repCost;
                    Column c2 = getPartitionRequired(a, o, i);
                    Double c2RepCost = 0.0;
                    if (c2 != null) {
                        c2RepCost = NodeCostEstimator.estimateRepartition(e2, c2);
                    }

                    if (c == null || cComesFromChildNo != i || guaranteesResultPtnedOn(a, o, c)) {
                        // algPlan.append(getBestPlan(e2, c2, memo, algLimit,
                        // c2RepCost, cel, partitionRecord, toMatAlg));
                        SinglePlan t = getBestPlanPrunedNoMat(e2, c2, algLimit, c2RepCost, oRecord);
                        if (t == null) {
                            continue;
                        }
                        algPlan.addInputPlan(e2, c2);
                        algPlan.increaseCost(t.getCost());
                        algLimit -= t.getCost();
                    } else if (guaranteesResultNotPtnedOn(a, o, c)) {

                        if (repartitionCost < repCost) {
                            algPlan.addRepartitionBeforeOp(c);
                            algLimit -= repartitionCost;

                            if (algLimit < 0) {
                                continue;
                            }
                            algPlan.increaseCost(repartitionCost);
                            // algPlan.getPath().addOption(-1);
                            oRecord.setClassRepartitioned(c, true);

                        } else {
                            algLimit -= repCost;
                            if (algLimit < 0) {
                                continue;
                            }
                            algPlan.increaseCost(repCost);
                            oRecord.setClassRepartitioned(c, false);
                        }
                        // algPlan.append(getBestPlan(e2, c2, memo, algLimit,
                        // c2RepCost, cel, partitionRecord, toMatAlg));
                        SinglePlan t = getBestPlanPrunedNoMat(e2, c2, algLimit, c2RepCost, oRecord);
                        if (t == null) {
                            continue;
                        }
                        algPlan.addInputPlan(e2, c2);
                        algPlan.increaseCost(t.getCost());
                        algLimit -= t.getCost();
                    } else {
                        // algPlan.append(getBestPlan(e2, c, memo, algLimit,
                        // c2RepCost, cel, partitionRecord, toMatAlg));
                        SinglePlan t = getBestPlanPrunedNoMat(e2, c, algLimit, c2RepCost, oRecord);
                        if (t == null) {
                            continue;
                        }
                        algPlan.addInputPlan(e2, c);
                        algPlan.increaseCost(t.getCost());
                        algLimit -= t.getCost();
                        if (oRecord.getLast() == null || (!oRecord.getLast().contains(c)
                            && repartitionCost < repCost)) {

                            // here do not add repCost. it has been added before
                            algPlan.addRepartitionBeforeOp(c);
                            // algPlan.getPath().addOption(-1);
                            oRecord.setClassRepartitioned(c, true);
                            // algLimit -= repartitionCost;
                        }
                        // algLimit -= algPlan.getCost();
                    }
                    if (c2 != null) {
                        if (oRecord.getLast() == null || !oRecord.getLast().contains(c2)) {
                            // algPlan.getPath().addOption(-1);
                            algPlan.addRepartitionAfterOp(i, c2);
                            if (algPlan.getRepartitionBeforeOp() != null) {
                                oRecord.setClassRepartitioned(c2, false);
                            } else {
                                oRecord.setClassRepartitioned(c2, true);
                            }
                            algLimit -= c2RepCost;
                            if (algLimit < 0) {
                                continue;
                            }
                            algPlan.increaseCost(c2RepCost);
                        }
                    }
                    // double e2PlanCost = algPlan.getCost();
                    if (c != null) {
                        if (oRecord.getLast() == null || !oRecord.getLast().contains(c)) {
                            algLimit -= repartitionCost;
                            if (algLimit < 0) {
                                continue;
                            }
                            // e2PlanCost += repartitionCost;
                        }
                    }
                    // algLimit -= e2PlanCost;
                    // algRecordCloned.addClassesFrom(oRecord);
                    if (i == retainsPartition) {
                        returnedPt = oRecord.getLast();
                    }
                }
                if (returnedPt != null) {
                    algRecordCloned.setLastPartitioned(returnedPt);
                }
                if (e2Plan == null || algPlan.getCost() < e2Plan.getCost()) {
                    e2Plan = algPlan;
                    // toMatE2.clear();
                    e2RecordCloned = algRecordCloned;
                }
            }
            if (resultPlan == null || e2Plan.getCost() < resultPlan.getCost()) {
                resultPlan = e2Plan;
                // toMaterialize.clear();
                partitionRecord.copyFrom(e2RecordCloned);
                memo.put(e, resultPlan, c, repCost, e2RecordCloned.getLast());
                if (!e.getParents().isEmpty() && e.getParents().get(0).getOpCode() == Node.UNION) {

                    limit = resultPlan.getCost();
                    System.out.println("prune: " + e.getObject() + "with limit:" + limit);
                }
            }
        }
        if (resultPlan == null) {
            System.out.println("pruned!!!");
            limits.put(e, limit - repCost);
        }
        return resultPlan;

    }

    public void setImportExternal(boolean b) {
        this.importExternal = b;
    }

}
