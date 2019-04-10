/**
 * Copyright MaDgIK Group 2010 - 2012.
 */
package madgik.exareme.utils.embedded.db;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author herald
 * @author Christoforos Svingos
 */
public class EmbeddedDB implements SQLDatabase {

    private static Logger log = Logger.getLogger(EmbeddedDB.class);
    private Connection conn = null;
    private Statement stmt = null;

    public EmbeddedDB(Connection conn) throws SQLException {
        this.conn = conn;
        this.stmt = conn.createStatement();
    }

    @Override
    public void execute(String createScript) throws SQLException {
        log.trace("Executing script ... ");
        stmt.executeUpdate(createScript);
    }

    @Override
    public SQLQueryInfo getQueryInfo(String query) throws SQLException {
        SQLQueryInfo info = new SQLQueryInfo(query);
        List<String[]> explain = execAndGetResults(".queryplan " + query);
        // Input tables
        List<String> inputTables = getInputTables(query, explain);
        for (String input : inputTables) {
            info.addInputTable(input);
        }
        // Used columns
        ArrayList<String> tokens = tokenize(query);
        Map<String, Set<String>> usedColumns = getUsedColumns(query, explain);
        for (String table : usedColumns.keySet()) {
            Set<String> columns = usedColumns.get(table);
            for (String c : columns) {
                info.addUsedColumn(table, c, isFiltered(table, c, tokens, usedColumns),
                        isJoined(table, c, tokens, usedColumns));
            }
        }
        return info;
    }

    private ArrayList<String> tokenize(String query) {
        ArrayList<String> tokens = new ArrayList<String>();
        String[] initialTokens = query.split("[ ,.,=,>,<,>=,<=,(,),\\n,\\r]");
        for (String t : initialTokens) {
            String tok = t.trim().toLowerCase();
            if (tok.isEmpty() == false) {
                tokens.add(tok);
            }
        }
        return tokens;
    }

    private boolean isTableColumn(String token, Map<String, Set<String>> usedColumns) {
        for (String table : usedColumns.keySet()) {
            Set<String> columns = usedColumns.get(table);
            if (columns.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFiltered(String table, String column, ArrayList<String> tokens,
                               Map<String, Set<String>> usedColumns) {
        boolean whereFound = false;
        for (int i = 0; i < tokens.size(); ++i) {
            String t = tokens.get(i);
            if (whereFound == false) {
                if (t.equalsIgnoreCase("where")) {
                    whereFound = true;
                }
                continue;
            }

            if (t.equals(column)) {
                if (tokens.get(i - 1).equals("and") || tokens.get(i - 1).equals("or") || tokens
                        .get(i - 1).equals("where")) {
                    if (tokens.size() > i + 1) {
                        if (isTableColumn(tokens.get(i + 1), usedColumns) == false) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isJoined(String table, String column, ArrayList<String> tokens,
                             Map<String, Set<String>> usedColumns) {
        boolean whereFound = false;
        for (int i = 0; i < tokens.size(); ++i) {
            String t = tokens.get(i);
            if (whereFound == false) {
                if (t.equalsIgnoreCase("where")) {
                    whereFound = true;
                }
                continue;
            }

            if (t.equals(column)) {
                if (isTableColumn(tokens.get(i - 1), usedColumns)) {
                    return true;
                }

                if (tokens.size() > i + 1) {
                    if (isTableColumn(tokens.get(i + 1), usedColumns)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Map<String, Set<String>> getUsedColumns(String query, List<String[]> explain) throws SQLException {
        Map<String, Set<String>> tableColums = new HashMap<String, Set<String>>();

        for (String[] exprParts : explain) {
            String tableName = exprParts[1].toLowerCase();
            if (exprParts[3].toLowerCase().equals("main")) {
                String columnName = exprParts[2].toLowerCase();
//                System.out.println(columnName);
                Set<String> columnsSet = tableColums.get(tableName);
                if (columnsSet == null) {
                    columnsSet = new HashSet<String>();
                    tableColums.put(tableName, columnsSet);
                }
                columnsSet.add(columnName);
            }
        }
//        System.out.println("\n\n\n\n\n\n\n");

        //    List<String> tables = getInputTables(query);
        //    if (tables.size() != tableColums.size()) {
        //      throw new SQLException("Internal error: number of tables do not match: "
        //              + tables.size() + " != " + tableColums.size());
        //    }
        return tableColums;
    }

    //  private Map<String, Set<String>> getUsedColumns(String query) throws SQLException {
    //    Map<String, Set<String>> tableColums = new HashMap<String, Set<String>>();
    //    List<String[]> explain = execAndGetResults("queryplan " + query);
    //    for (String[] exprParts : explain) {
    //      String tableName = exprParts[1].toLowerCase();
    //      if (tableName.equals("null")) {
    //        continue;
    //      }
    //      String columnName = exprParts[2].toLowerCase();
    //      Set<String> columnsSet = tableColums.get(tableName);
    //      if (columnsSet == null) {
    //        columnsSet = new HashSet<String>();
    //        tableColums.put(tableName, columnsSet);
    //      }
    //      columnsSet.add(columnName);
    //    }
    //
    ////    List<String> tables = getInputTables(query);
    ////    if (tables.size() != tableColums.size()) {
    ////      throw new SQLException("Internal error: number of tables do not match: "
    ////              + tables.size() + " != " + tableColums.size());
    ////    }
    //    return tableColums;
    //  }

    @Override
    public TableInfo getTableInfo(String tableName) throws SQLException {
        TableInfo info = new TableInfo(tableName);
        info.setSqlDefinition(getTableDefinition(tableName));
        return info;
    }

    @Override
    public void close() throws SQLException {
        stmt.close();
        conn.close();
    }

    private List<String> getInputTables(String query, List<String[]> explain) throws SQLException {
        HashSet<String> tables = new HashSet<String>();

        for (String[] tableParts : explain) {
            String table = tableParts[1].toLowerCase();
            if (tableParts[3].toLowerCase().equals("main")) {
                tables.add(table);
            }
        }

        ArrayList<String> inputTables = new ArrayList<String>();
        inputTables.addAll(tables);

        return inputTables;
    }

    //  private List<String> getInputTables(String query) throws SQLException {
    //    HashSet<String> tables = new HashSet<String>();
    //    List<String[]> explain = execAndGetResults("explain query plan " + query);
    //    for (String[] tableParts : explain) {
    //      String[] details = tableParts[3].split(" ");
    //      if (details[1].startsWith("TABLE") == false) {
    //        continue;
    //      }
    //      tables.add(details[2].toLowerCase());
    //    }
    //    ArrayList<String> inputTables = new ArrayList<String>();
    //    inputTables.addAll(tables);
    //    return inputTables;
    //  }

    private List<String[]> execAndGetResults(String query) throws SQLException {
        log.trace("Executing query ... ");
        List<String[]> result = new ArrayList<String[]>();
        ResultSet rs = stmt.executeQuery(query + ";");
        int numColumns = rs.getMetaData().getColumnCount();
        log.debug("Number of columns:" + numColumns);
        while (rs.next()) {
            String[] next = new String[numColumns];
            for (int c = 0; c < next.length; ++c) {
                next[c] = rs.getString(c + 1);
            }
            result.add(next);
        }
        rs.close();
        return result;
    }

    private String getTableDefinition(String tableName) throws SQLException {
        log.trace("Getting definition of table: " + tableName);
        ResultSet rs = stmt.executeQuery(
                "SELECT sql FROM sqlite_master WHERE type='table' and name = '" + tableName + "';");
        String sql = null;
        while (rs.next()) {
            sql = rs.getString(1);
            break;
        }
        rs.close();
        return sql;
    }

    @Override
    public ResultSet executeAndGetResults(String query) throws SQLException {
        return stmt.executeQuery(query);
    }
}
