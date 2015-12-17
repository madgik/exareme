/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.parser;

import com.google.gson.Gson;
import madgik.exareme.common.app.engine.DMQuery;
import madgik.exareme.common.schema.*;
import madgik.exareme.common.schema.expression.*;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.embedded.db.*;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author herald
 * @author Christoforos Svingos
 */
public class AdpDBParser {
    private static final String engine = System.getProperty("EXAREME_MADIS");
    private static Logger log = Logger.getLogger(AdpDBParser.class);
    private AdpDBClientProperties properties;

    private int id = 0;
    private SQLScript sqlScript = null;

    public AdpDBParser(AdpDBClientProperties properties) {
        this.properties = properties;
    }

    public AdpDBParser(String database) {
        this.properties = new AdpDBClientProperties(database);
    }

    public QueryScript parse(String queryScript, Registry registry) throws RemoteException {

        long start = System.currentTimeMillis();

        QueryScript script = new QueryScript(properties.getDatabase(), registry.getMappings());
        // Parse distributed query script
        sqlScript = null;
        id = 0;
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(queryScript.getBytes());
            AdpDBQueryParser parser = new AdpDBQueryParser(stream);
            sqlScript = parser.parseScript();
        } catch (ParseException e) {
            throw new ServerException("Cannot parse script", e);
        }

        try {
            addQueries(registry, script, sqlScript);
            addDMQueries(registry, script, sqlScript);
        } catch (Exception e) {
            throw new SemanticException("Cannot validate script", e);
        }
        long end = System.currentTimeMillis();

        log.debug("Parsed in " + String.valueOf(end - start) + " ms");

        return script;
    }

    private void addQueries(Registry registry, QueryScript script, SQLScript sQLScript) throws Exception {
        HashMap<String, Table> tables = new HashMap<String, Table>();
        log.debug("Adding queries ...");
        for (SQLSelect q : sQLScript.getQueries()) {
            if (tables.containsKey(q.getResultTable()) || registry
                .containsPhysicalTable(q.getResultTable())) {
                throw new SemanticException("Table already exists: " + q.getResultTable());
            }
            Table out = new Table(q.getResultTable());
            out.setTemp(q.isTemporary());
            tables.put(out.getName(), out);
            log.debug("Creating view on table '" + out.getName() + "' ...");
            TableView outView = new TableView(out);
            outView.setPattern(q.getOutputDataPattern());
            outView.setNumOfPartitions(q.getNumberOfOutputPartitions());
            for (String column : q.getPartitionColumns()) {
                outView.addPatternColumn(column);
            }

            Select query = new Select(id, q, outView);
            addRunOnParts(out.getName(), q, query);
            id++;
            script.addQuery(query);
        }
        // Do not validate the query
        // TODO(herald):
        if (properties.isValidationEnabled() == false) {
            // Do a simple check
            for (Select q : script.getSelectQueries()) {
                if (q.getParsedSqlQuery().getInputDataPattern() == DataPattern.external) {
                    if (q.getParsedSqlQuery().getUsingTBLs() == null
                        || q.getParsedSqlQuery().getUsingTBLs().size() == 0) {
                        log.debug("Skipping for queries with external pattern.");
                        continue;
                    }
                }
                if (q.getParsedSqlQuery().getInputDataPattern() == DataPattern.remote) {
                    if (q.getParsedSqlQuery().getUsingTBLs() == null
                        || q.getParsedSqlQuery().getUsingTBLs().size() == 0) {
                        log.debug("Skipping for queries with remote pattern.");
                        continue;
                    }
                }

                HashSet<String> inputTables = new HashSet<String>();
                for (String tbl : q.getParsedSqlQuery().getUsingTBLs()) {
                    inputTables.add(tbl);
                }

                for (PhysicalTable pTable : registry.getPhysicalTables()) {
                    // TODO Contains is not enough cause table may be a substring of another table.
                    if (q.getParsedSqlQuery().getSql().contains(pTable.getName())) {
                        inputTables.add(pTable.getName());
                    }
                }
                for (String tName : tables.keySet()) {
                    if (q.getParsedSqlQuery().getSql().contains(tName)) {
                        inputTables.add(tName);
                    }
                }
                for (String tName : inputTables) {
                    Table table = tables.get(tName);
                    if (table == null) {
                        PhysicalTable pTable = registry.getPhysicalTable(tName);
                        if (pTable != null) {
                            table = pTable.getTable();
                        }
                    }
                    if (table == null) {
                        throw new SemanticException("Table not exists: " + tName);
                    }
                    TableView inView = new TableView(table);
                    inView.setPattern(q.getParsedSqlQuery().getInputDataPattern());
                    q.addInput(inView);
                }
            }
            return;
        }

        log.debug("Creating in-memory database ...");
        SQLDatabase imdb = null;

        imdb = DBUtils.createEmbeddedMadisDB(engine);
        log.debug("Using embedded sql db implementation.");

        log.debug("Creating schema ...");
        for (String def : registry.getTableDefinitions()) {
            log.trace("Table def : " + def);
            if (def != null)
                imdb.execute(def);
            else
                log.trace("No registry def");
        }

        log.debug("Checking queries for semantic errors ...");
        for (Select q : script.getSelectQueries()) {
            if (registry.getPhysicalTable(q.getOutputTable().getName()) != null) {
                throw new SemanticException(
                    "Table exists: " + registry.getPhysicalTable(q.getOutputTable().getName()));
            }
            if (q.getParsedSqlQuery().getInputDataPattern() == DataPattern.external) {
                log.debug("Skipping for queries with external pattern.");
                continue;
            }
            if (q.getParsedSqlQuery().getInputDataPattern() == DataPattern.remote) {
                log.debug("Skipping for queries with remote pattern.");
                continue;
            }
            if (q.getParsedSqlQuery().getInputDataPattern() == DataPattern.virtual) {
                log.debug("Skipping for queries with virtual pattern.");
                continue;
            }
            SQLQueryInfo sQLQueryInfo;
            Pattern madisCreateStmtPattern = Pattern.compile(
                "(?i)\\s*create\\s+(temp|temporary)\\s+(view|table)\\s+(\\w+)\\s+as\\s+(.*)");
            try {
                if (q.getParsedSqlQuery().isScript()) {
                    sQLQueryInfo = new SQLQueryInfo(q.getQuery());
                    ResultSet rs = imdb.executeAndGetResults(q.getQuery());
                    while (rs.next()) {
                        //            String queryStatement = rs.getString(1).trim().toLowerCase();
                        String queryStatement = rs.getString(1).trim();
                        queryStatement =
                            (queryStatement.substring(queryStatement.length() - 1).equals(";")) ?
                                queryStatement.substring(0, queryStatement.length() - 1) :
                                queryStatement;
                        q.addQueryStatement(queryStatement);
                    }
                    rs.close();

                    for (String queryStatement : q.getQueryStatements()) {
                        Matcher createStmtMatcher = madisCreateStmtPattern.matcher(queryStatement);
                        if (createStmtMatcher.find()) {
                            for (String tableName : imdb.getQueryInfo(createStmtMatcher.group(4))
                                .getInputTables()) {
                                sQLQueryInfo.addInputTable(tableName);
                            }
                            imdb.execute(queryStatement.replaceFirst("(?i)\\s+temp\\s+view\\s+", " temp table "));
                        } else if (queryStatement.startsWith("select")) {
                            for (String tableName : imdb.getQueryInfo(queryStatement)
                                .getInputTables()) {
                                sQLQueryInfo.addInputTable(tableName);
                            }
                        }
                    }
                } else {
                    sQLQueryInfo = imdb.getQueryInfo(q.getQuery());
                    q.addQueryStatement(q.getQuery());
                }

                for (String tableName : q.getParsedSqlQuery().getUsingTBLs()) {
                    sQLQueryInfo.addInputTable(tableName);
                }
            } catch (SQLException e) {
                log.info("Got exception while execution  query: " + e.toString(), e);
                if (e.getMessage().contains("GOT EMPTY INPUT")) {
                    log.info("Set virtual pattern to the query");
                    q.getParsedSqlQuery().setInputDataPattern(DataPattern.virtual);
                    continue;
                } else {
                    throw e;
                }
            }

            Map<String, Set<ColumnInfo>> inputTables = sQLQueryInfo.getUsedColumns();
            log.debug("Add input tables ...");
            for (String tName : inputTables.keySet()) {
                log.debug("Input: " + tName);
                // Do not check virtual tables
                if (tName.matches("vt_[0-9]+")) {
                    continue;
                }
                Table table = tables.get(tName);
                if (table == null) {
                    PhysicalTable pTable = registry.getPhysicalTable(tName);
                    if (pTable != null) {
                        table = pTable.getTable();
                    }
                }
                if (table == null) {
                    throw new SemanticException("Table not exists: " + tName);
                }
                TableView inView = new TableView(table);
                inView.setPattern(q.getParsedSqlQuery().getInputDataPattern());
                Set<ColumnInfo> usedColumns = inputTables.get(tName);
                log.debug("Add used columns ...");
                for (ColumnInfo col : usedColumns) {
                    log.debug("\t Column: " + col);
                    inView.addUsedColumn(col.getColumnName());
                }
                q.addInput(inView);
            }
            // If the input is only from virtual tables
            if (q.getInputTables().isEmpty()) {
                q.getParsedSqlQuery().setInputDataPattern(DataPattern.virtual);
            }
            log.debug("Create the output table ...");
            String outTableName = q.getOutputTable().getTable().getName();
            imdb.execute(
                "create table " + outTableName + " as " + q.getSelectQueryStatement() + ";\n");
            TableInfo tableInfo = imdb.getTableInfo(outTableName);
            tables.get(outTableName).setSqlDefinition(tableInfo.getSQLDefinition());
        }
        imdb.close();
    }

    private void addDMQueries(Registry registry, QueryScript script, SQLScript sQLScript)
        throws Exception {
        log.debug("Adding build indexes ...");
        for (SQLBuildIndex bi : sQLScript.getBuildIndexes()) {
            BuildIndex biq = new BuildIndex(id, bi);
            script.addBuildIndex(biq);
            addRunOnParts(bi.getTable(), bi, biq);
            id++;
        }
        log.debug("Adding drop indexes ...");
        for (SQLDropIndex di : sQLScript.getDropIndexes()) {
            DropIndex diq = new DropIndex(id, di);
            script.addDropIndex(diq);
            addRunOnParts(di.getTable(), di, diq);
            id++;
        }
        log.debug("Adding drop tables ...");
        for (SQLDropTable dt : sQLScript.getDropTables()) {
            DropTable qtq = new DropTable(id, dt);
            script.addDropTable(qtq);
            addRunOnParts(dt.getTable(), dt, qtq);
            id++;
        }
        log.debug("Checking build index for semantic errors ...");
        for (DMQuery dm : script.getDMQueries()) {
            log.debug("Checking query \n" + dm.getQuery());
            if (registry.containsPhysicalTable(dm.getTable()) == false) {
                throw new SemanticException("Table not exists: " + dm.getTable());
            }
            // TODO(herald): check if the index exists.
        }
    }

    private void addRunOnParts(String table, SQLQuery sqlQ, Query q) {
        log.debug("Computing parts to run of table '" + table + "' ...");
        String partsDefn = sqlQ.getPartsDefn();
        if (partsDefn == null) {
            log.debug("No paritions defined!");
            return;
        }
        Gson gson = new Gson();
        int[] parts = gson.fromJson(partsDefn, new int[] {}.getClass());
        log.debug("Parts of '" + table + "': " + parts.length);
        for (int pNum : parts) {
            q.addRunOnPart(pNum);
            log.debug("\t " + pNum);
        }
    }

}
