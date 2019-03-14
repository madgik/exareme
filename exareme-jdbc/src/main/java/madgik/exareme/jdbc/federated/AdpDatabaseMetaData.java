/**
 * Copyright MaDgIK Group 2010 - 2013.
 */
package madgik.exareme.jdbc.federated;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.sql.*;
import java.util.*;

/**
 * @author dimitris
 */
public class AdpDatabaseMetaData implements java.sql.DatabaseMetaData {

    private AdpConnection con;

    public AdpDatabaseMetaData(AdpConnection connection) {
        this.con = connection;
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getURL() throws SQLException {
        return this.con.getURL().toString();
    }

    @Override
    public String getUserName() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return ("ADP");
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return "ADP Federated Version";
    }

    @Override
    public String getDriverName() throws SQLException {
        return ("AdpDriver");
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return "1.0";
    }

    @Override
    public int getDriverMajorVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDriverMinorVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        //return the quote string of the 1st endpoint
        //What to do if endpoints have different quote strings????
        FederatedConnections cons = con.getFederatedConnections();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }
        String result = cons.getFirstConnection().getMetaData().getIdentifierQuoteString();
        return result;
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        FederatedConnections cons = con.getFederatedConnections();
        List<String> total = new ArrayList<>();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }
        total.add("distributed");
        total.add("on");
        total.add("direct");
        total.add("external");
        total.add("virtual");
        for (Connection conn : cons.getDistinctDBConnections()) {
            String[] nextKeys = conn.getMetaData().getSQLKeywords().replaceAll(" ", "").split(",");
            for (int i = 0; i < nextKeys.length; i++) {
                if (!total.contains(nextKeys[i])) {
                    total.add(nextKeys[i]);
                }
            }
        }
        StringBuilder result = new StringBuilder();
        String separator = "";
        for (String key : total) {
            result.append(separator);
            result.append(key);
            separator = ",";
        }
        return result.toString();
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getStringFunctions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        FederatedConnections cons = con.getFederatedConnections();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }

        String result = "";

        for (Connection conn : cons.getDistinctDBConnections()) {
            String next = conn.getMetaData().getExtraNameCharacters();
            for (int i = 0; i < next.length(); i++) {
                if (result.indexOf((next.charAt(i))) != -1) {
                    result += next.charAt(i);
                }
            }
        }
        return result;
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxConnections() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxStatements() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern,
                                   String procedureNamePattern) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern,
                                         String procedureNamePattern,
                                         String columnNamePattern) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern,
                               String tableNamePattern, String[] types) throws
            SQLException {
        //tableNamePattern cannot be null!
        FederatedConnections cons = con.getFederatedConnections();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }

        String dbID = "";
        Iterator<Schema> endpoints = cons.getSchemas().iterator();
        while (endpoints.hasNext()) {
            Schema nextEndpoint = endpoints.next();
            dbID = nextEndpoint.getId();
            if (tableNamePattern.toUpperCase().startsWith(dbID.toUpperCase() + "_")) {
                tableNamePattern = tableNamePattern.substring(dbID.length() + 1);
                Connection c = cons.getConnection(nextEndpoint);
                String schemaPatt = nextEndpoint.getSchema();
                if (c.getClass().getName().contains("postgresql")) {
                    schemaPatt = null;
                }
                ResultSet first = cons.getConnection(nextEndpoint).getMetaData().getTables(catalog, schemaPatt, tableNamePattern, types);

                ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
                //   ArrayList<String> typenames=new ArrayList<String>();
                //   ArrayList<String> names=new ArrayList<String>();
                for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                    ArrayList<String> nextCouple = new ArrayList<String>();
                    nextCouple.add(first.getMetaData().getColumnName(i).toUpperCase());
                    nextCouple.add(first.getMetaData().getColumnTypeName(i));
                    schema.add(nextCouple);
                }
                // names.add(first.getMetaData().getColumnName(i));
                // typenames.add(first.getMetaData().getColumnTypeName(i));
                //      }
                //  schema.add(names);
                //  schema.add(typenames);

                HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
                h.put("schema", schema);
                h.put("errors", new ArrayList<ArrayList<String>>());
                Gson g = new Gson();
                StringBuilder sb = new StringBuilder();
                sb.append(g.toJson(h, h.getClass()));

                while (first.next()) {
                    sb.append("\n");
                    ArrayList<Object> res = new ArrayList<Object>();
                    for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                        if (i == 2) {
                            res.add("adp");
                        } else if (i == 3) {
                            //table names
                            res.add(dbID + "_" + first.getString(i));
                        } else {
                            res.add(first.getObject(i));
                        }
                    }
                    sb.append(g.toJson((ArrayList<Object>) res));
                }
                first.close();
                AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
                result.setCloseStOnClose(true);
                return result;
            }
        }

        endpoints = cons.getSchemas().iterator();
        Schema nextEndpoint = endpoints.next();
        dbID = nextEndpoint.getId();
        Connection c = cons.getConnection(nextEndpoint);
        String schemaPatt = nextEndpoint.getSchema();
        if (c.getClass().getName().contains("postgresql")) {
            schemaPatt = null;
        }
        ResultSet first = cons.getConnection(nextEndpoint).getMetaData().getTables(catalog, schemaPatt, tableNamePattern, types);

        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
        for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
            ArrayList<String> nextCouple = new ArrayList<String>();
            nextCouple.add(first.getMetaData().getColumnName(i).toUpperCase());
            nextCouple.add(first.getMetaData().getColumnTypeName(i));
            schema.add(nextCouple);
        }
        /*  ArrayList<String> typenames=new ArrayList<String>();
         ArrayList<String> names=new ArrayList<String>();
         for(int i=1;i<first.getMetaData().getColumnCount()+1;i++){
         names.add(first.getMetaData().getColumnName(i));
         typenames.add(first.getMetaData().getColumnTypeName(i));
         }
         schema.add(names);
         schema.add(typenames);*/

        HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
        h.put("schema", schema);
        h.put("errors", new ArrayList<ArrayList<String>>());
        Gson g = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append(g.toJson(h, h.getClass()));

        while (first.next()) {
            sb.append("\n");
            ArrayList<Object> res = new ArrayList<Object>();
            for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                if (i == 2) {
                    res.add("adp");
                } else if (i == 3) {
                    //table names
                    res.add(dbID + "_" + first.getString(i));
                } else {
                    res.add(first.getObject(i));
                }
            }
            sb.append(g.toJson((ArrayList<Object>) res));
        }
        first.close();

        while (endpoints.hasNext()) {
            nextEndpoint = endpoints.next();
            dbID = nextEndpoint.getId();
            c = cons.getConnection(nextEndpoint);
            schemaPatt = nextEndpoint.getSchema();
            if (c.getClass().getName().contains("postgresql")) {
                schemaPatt = null;
            }
            ResultSet next = cons.getConnection(nextEndpoint).getMetaData().getTables(catalog, schemaPatt, tableNamePattern, types);
            while (next.next()) {
                sb.append("\n");
                ArrayList<Object> res = new ArrayList<Object>();
                for (int i = 1; i < next.getMetaData().getColumnCount() + 1; i++) {
                    if (i == 2) {
                        res.add("adp");
                    } else if (i == 3) {
                        //table names
                        res.add(dbID + "_" + next.getString(i));
                    } else {
                        res.add(next.getObject(i));
                    }
                }
                sb.append(g.toJson((ArrayList<Object>) res));
            }
            next.close();
        }

        Statement exSt = this.con.createStatement();
        ResultSet internalExaremeTables = exSt.executeQuery("getTables " + catalog + " null " + tableNamePattern);

        // ResultSet internalExaremeTables = con.getRegistryConnection().getMetaData().getTables(catalog, schemaPatt, tableNamePattern, types);
        while (internalExaremeTables.next()) {
            sb.append("\n");
            ArrayList<Object> res = new ArrayList<Object>();
            for (int i = 1; i < internalExaremeTables.getMetaData().getColumnCount() + 1; i++) {
                if (i == 2) {
                    res.add("adp");
                } else {
                    res.add(internalExaremeTables.getObject(i));
                }
            }
            sb.append(g.toJson((ArrayList<Object>) res));
        }
        internalExaremeTables.close();
        exSt.close();

        AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
        result.setCloseStOnClose(true);
        return result;
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
        //ArrayList<String> typenames=new ArrayList<String>();
        //typenames.add("VARCHAR");
        //schema.add(typenames);
        ArrayList<String> names = new ArrayList<String>();
        names.add("TABLE_SCHEM");
        names.add("VARCHAR");
        schema.add(names);
        ArrayList<String> names2 = new ArrayList<String>();
        names2.add("TABLE_CATALOG");
        names2.add("VARCHAR");
        schema.add(names2);

        HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
        h.put("schema", schema);
        h.put("errors", new ArrayList<ArrayList<String>>());
        Gson g = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append(g.toJson(h, h.getClass()));
        sb.append("\n");
        ArrayList<Object> res = new ArrayList<Object>();
        res.add("adp");
        res.add(null);
        sb.append(g.toJson((ArrayList<Object>) res));
        AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
        result.setCloseStOnClose(true);
        return result;
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern,
                                String tableNamePattern, String columnNamePattern)
            throws SQLException {
        FederatedConnections cons = con.getFederatedConnections();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }

        String dbID = "";
        Iterator<Schema> endpoints = cons.getSchemas().iterator();
        while (endpoints.hasNext()) {
            Schema nextEndpoint = endpoints.next();
            dbID = nextEndpoint.getId();
            if (tableNamePattern.toUpperCase().startsWith(dbID.toUpperCase() + "_")) {
                Connection c = cons.getConnection(nextEndpoint);

                tableNamePattern = tableNamePattern.substring(dbID.length() + 1);
                String schemaPatt = nextEndpoint.getSchema();
                if (c.getClass().getName().contains("postgresql")) {
                    schemaPatt = null;
                }
                ResultSet first = cons.getConnection(nextEndpoint).getMetaData().getColumns(catalog, schemaPatt, tableNamePattern, columnNamePattern);

                ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
                for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                    ArrayList<String> nextCouple = new ArrayList<String>();
                    nextCouple.add(first.getMetaData().getColumnName(i).toUpperCase());
                    nextCouple.add(first.getMetaData().getColumnTypeName(i));
                    schema.add(nextCouple);
                }
                /* ArrayList<String> typenames=new ArrayList<String>();
                 ArrayList<String> names=new ArrayList<String>();
                 for(int i=1;i<first.getMetaData().getColumnCount()+1;i++){
                 names.add(first.getMetaData().getColumnName(i));
                 typenames.add(first.getMetaData().getColumnTypeName(i));
                 }
                 schema.add(names);
                 schema.add(typenames);*/

                HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
                h.put("schema", schema);
                h.put("errors", new ArrayList<ArrayList<String>>());
                Gson g = new Gson();
                StringBuilder sb = new StringBuilder();
                sb.append(g.toJson(h, h.getClass()));

                while (first.next()) {
                    sb.append("\n");
                    ArrayList<Object> res = new ArrayList<Object>();
                    for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                        if (i == 3 || i == 21) {
                            //table names
                            res.add(dbID + "_" + first.getString(i));
                        } else if (i == 2) {
                            res.add("adp");
                        } else {
                            res.add(first.getObject(i));
                        }
                    }
                    sb.append(g.toJson((ArrayList<Object>) res));

                }
                first.close();
                AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
                result.setCloseStOnClose(true);
                return result;
            }
        }

        endpoints = cons.getSchemas().iterator();
        Schema nextEndpoint = endpoints.next();
        dbID = nextEndpoint.getId();
        Connection c = cons.getConnection(nextEndpoint);
        String schemaPatt = nextEndpoint.getSchema();
        if (c.getClass().getName().contains("postgresql")) {
            schemaPatt = null;
        }
        ResultSet first = cons.getConnection(nextEndpoint).getMetaData().getColumns(catalog, schemaPatt, tableNamePattern, columnNamePattern);

        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
        for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
            ArrayList<String> nextCouple = new ArrayList<String>();
            nextCouple.add(first.getMetaData().getColumnName(i).toUpperCase());
            nextCouple.add(first.getMetaData().getColumnTypeName(i));
            schema.add(nextCouple);
        }
        /*ArrayList<String> typenames=new ArrayList<String>();
         ArrayList<String> names=new ArrayList<String>();
         for(int i=1;i<first.getMetaData().getColumnCount()+1;i++){
         names.add(first.getMetaData().getColumnName(i));
         typenames.add(first.getMetaData().getColumnTypeName(i));
         }
         schema.add(names);
         schema.add(typenames);*/

        HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
        h.put("schema", schema);
        h.put("errors", new ArrayList<ArrayList<String>>());
        Gson g = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append(g.toJson(h, h.getClass()));

        while (first.next()) {
            sb.append("\n");
            ArrayList<Object> res = new ArrayList<Object>();
            for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                if (i == 3 || i == 21) {
                    //table names
                    res.add(dbID + "_" + first.getString(i));
                } else if (i == 2) {
                    res.add("adp");
                } else {
                    res.add(first.getObject(i));
                }

            }
            sb.append(g.toJson((ArrayList<Object>) res));

        }
        first.close();

        while (endpoints.hasNext()) {
            nextEndpoint = endpoints.next();
            dbID = nextEndpoint.getId();
            c = cons.getConnection(nextEndpoint);
            schemaPatt = nextEndpoint.getSchema();
            if (c.getClass().getName().contains("postgresql")) {
                schemaPatt = null;
            }
            ResultSet next = cons.getConnection(nextEndpoint).getMetaData().getColumns(catalog, schemaPatt, tableNamePattern, columnNamePattern);
            while (next.next()) {
                sb.append("\n");
                ArrayList<Object> res = new ArrayList<Object>();
                for (int i = 1; i < next.getMetaData().getColumnCount() + 1; i++) {
                    if (i == 3 || i == 21) {
                        //table names
                        res.add(dbID + "_" + next.getString(i));
                    } else if (i == 2) {
                        res.add("adp");
                    } else {
                        res.add(next.getObject(i));
                    }
                }
                sb.append(g.toJson((ArrayList<Object>) res));
            }
            next.close();
        }

        Statement exSt = this.con.createStatement();
        ResultSet internalExaremeTableCols = exSt.executeQuery("getColumns " + catalog + " null " + tableNamePattern + " " + columnNamePattern);
        //ResultSet internalExaremeTableCols = con.getRegistryConnection().getMetaData().getColumns(catalog, schemaPatt, tableNamePattern, columnNamePattern);
        while (internalExaremeTableCols.next()) {
            sb.append("\n");
            ArrayList<Object> res = new ArrayList<Object>();
            for (int i = 1; i < internalExaremeTableCols.getMetaData().getColumnCount() + 1; i++) {
                if (i == 2) {
                    res.add("adp");
                } else {
                    res.add(internalExaremeTableCols.getObject(i));
                }
            }
            if (res.size() < schema.size()) {
                //sqlite not returning IS_AUTOINCREMENT?
                res.add("");
            }
            sb.append(g.toJson((ArrayList<Object>) res));
        }
        internalExaremeTableCols.close();
        exSt.close();

        AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
        result.setCloseStOnClose(true);
        return result;

    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema,
                                         String table, String columnNamePattern)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
                                        String tableNamePattern) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema,
                                          String table, int scope,
                                          boolean nullable) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table)
            throws SQLException {
        FederatedConnections cons = con.getFederatedConnections();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }

        String dbID = "";
        Schema nextEndpoint = null;
        Iterator<Schema> endpoints = cons.getSchemas().iterator();
        while (endpoints.hasNext()) {
            nextEndpoint = endpoints.next();
            dbID = nextEndpoint.getId();
            if (table.toUpperCase().startsWith(dbID.toUpperCase() + "_")) {
                table = table.substring(dbID.length() + 1);
                break;
            }
        }
        if (nextEndpoint == null) {
            throw new SQLException("No endpoint found for table " + table);
        }
        Connection c = cons.getConnection(nextEndpoint);
        String schemaPatt = nextEndpoint.getSchema();
        if (c.getClass().getName().contains("postgresql")) {
            schemaPatt = null;
        }
        ResultSet first = cons.getConnection(nextEndpoint).getMetaData().getPrimaryKeys(catalog, schemaPatt, table);

        ArrayList<ArrayList<String>> schemaList = new ArrayList<ArrayList<String>>();
        for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
            ArrayList<String> nextCouple = new ArrayList<String>();
            nextCouple.add(first.getMetaData().getColumnName(i).toUpperCase());
            nextCouple.add(first.getMetaData().getColumnTypeName(i));
            schemaList.add(nextCouple);
        }
        /* ArrayList<String> typenames=new ArrayList<String>();
         ArrayList<String> names=new ArrayList<String>();
         for(int i=1;i<first.getMetaData().getColumnCount()+1;i++){
         names.add(first.getMetaData().getColumnName(i));
         typenames.add(first.getMetaData().getColumnTypeName(i));
         }
      
         schemaList.add(names);
         schemaList.add(typenames);*/

        HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
        h.put("schema", schemaList);
        h.put("errors", new ArrayList<ArrayList<String>>());
        Gson g = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append(g.toJson(h, h.getClass()));

        while (first.next()) {
            sb.append("\n");
            ArrayList<Object> res = new ArrayList<Object>();
            for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                if (i == 3 || i == 7) {
                    //table names
                    res.add(dbID + "_" + first.getString(i));
                } else if (i == 2) {
                    res.add("adp");
                } else {
                    res.add(first.getObject(i));
                }
            }
            sb.append(g.toJson((ArrayList<Object>) res));
        }
        first.close();
        AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
        result.setCloseStOnClose(true);
        return result;

    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table)
            throws SQLException {
        FederatedConnections cons = con.getFederatedConnections();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }

        String dbID = "";
        Schema nextEndpoint = null;
        Iterator<Schema> endpoints = cons.getSchemas().iterator();
        while (endpoints.hasNext()) {
            nextEndpoint = endpoints.next();
            dbID = nextEndpoint.getId();
            if (table.toUpperCase().startsWith(dbID.toUpperCase() + "_")) {
                table = table.substring(dbID.length() + 1);
                break;
            }
        }
        if (nextEndpoint == null) {
            throw new SQLException("No endpoint found for table " + table);
        }
        Connection c = cons.getConnection(nextEndpoint);
        String schemaPatt = nextEndpoint.getSchema();
        if (c.getClass().getName().contains("postgresql")) {
            schemaPatt = null;
        }
        ResultSet first = cons.getConnection(nextEndpoint).getMetaData().getImportedKeys(catalog, schemaPatt, table);

        ArrayList<ArrayList<String>> schemaList = new ArrayList<ArrayList<String>>();
        for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
            ArrayList<String> nextCouple = new ArrayList<String>();
            nextCouple.add(first.getMetaData().getColumnName(i).toUpperCase());
            nextCouple.add(first.getMetaData().getColumnTypeName(i));
            schemaList.add(nextCouple);
        }
        /* ArrayList<String> typenames=new ArrayList<String>();
         ArrayList<String> names=new ArrayList<String>();
         for(int i=1;i<first.getMetaData().getColumnCount()+1;i++){
         names.add(first.getMetaData().getColumnName(i));
         typenames.add(first.getMetaData().getColumnTypeName(i));
         }
      
         schemaList.add(names);
         schemaList.add(typenames);*/

        HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
        h.put("schema", schemaList);
        h.put("errors", new ArrayList<ArrayList<String>>());
        Gson g = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append(g.toJson(h, h.getClass()));

        while (first.next()) {

            String otherSchema = first.getString(2);
            if (otherSchema == null) {
                //mysql returns cat?
                otherSchema = first.getString(1);
            }
            String primaryId = "";
            Iterator<Schema> endpoints2 = cons.getSchemas().iterator();
            while (endpoints2.hasNext()) {
                Schema nextEndpoint2 = endpoints2.next();

                String nextSchema = nextEndpoint2.getId();
                if (otherSchema.equalsIgnoreCase(nextSchema)) {
                    primaryId = nextEndpoint2.getId();
                    break;
                }
            }
            if (primaryId.equals("")) {
                continue;
                //references a table in an unknown schema
            }
            sb.append("\n");
            ArrayList<Object> res = new ArrayList<Object>();
            for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                if (i == 7) {
                    //table names
                    res.add(dbID + "_" + first.getString(i));
                } else if (i == 6) {
                    res.add("adp");
                } else if (i == 2) {
                    if (primaryId.equals("")) {
                        res.add(first.getString(2));
                    } else {
                        res.add("adp");
                    }
                } else if (i == 3) {
                    if (primaryId.equals("")) {
                        res.add(first.getString(3));
                    } else {
                        res.add(primaryId + "_" + first.getString(i));
                    }
                } else {
                    res.add(first.getObject(i));
                }
            }
            sb.append(g.toJson((ArrayList<Object>) res));
        }
        first.close();

        /*   while(endpoints.hasNext()){
         dbID=endpoints.next();
         ResultSet next=cons.get(dbID).getMetaData().getImportedKeys(catalog, schema, table);
         while(next.next()){
         ArrayList<Object> res=new ArrayList<Object>();
         for(int i=1;i<first.getMetaData().getColumnCount();i++){
         if(i==3 || i==7){
         //table names
         res.add(dbID+"_"+first.getString(i));
         }
         else{
         res.add(first.getObject(i));
         }
         }
         sb.append(g.toJson((ArrayList<Object>) res));
         }}
         */
        AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
        result.setCloseStOnClose(true);
        return result;
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table)
            throws SQLException {
        FederatedConnections cons = con.getFederatedConnections();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }

        String dbID = "";
        Schema nextEndpoint = null;
        Iterator<Schema> endpoints = cons.getSchemas().iterator();
        while (endpoints.hasNext()) {
            nextEndpoint = endpoints.next();
            dbID = nextEndpoint.getId();
            if (table.toUpperCase().startsWith(dbID.toUpperCase() + "_")) {
                table = table.substring(dbID.length() + 1);
                break;
            }
        }
        if (nextEndpoint == null) {
            throw new SQLException("No endpoint found for table " + table);
        }

        Connection c = cons.getConnection(nextEndpoint);
        String schemaPatt = nextEndpoint.getSchema();
        if (c.getClass().getName().contains("postgresql")) {
            schemaPatt = null;
        }
        ResultSet first = cons.getConnection(nextEndpoint).getMetaData().getExportedKeys(catalog, schemaPatt, table);

        ArrayList<ArrayList<String>> schemaList = new ArrayList<ArrayList<String>>();
        for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
            ArrayList<String> nextCouple = new ArrayList<String>();
            nextCouple.add(first.getMetaData().getColumnName(i).toUpperCase());
            nextCouple.add(first.getMetaData().getColumnTypeName(i));
            schemaList.add(nextCouple);
        }
        /* ArrayList<String> typenames=new ArrayList<String>();
         ArrayList<String> names=new ArrayList<String>();
         for(int i=1;i<first.getMetaData().getColumnCount()+1;i++){
         names.add(first.getMetaData().getColumnName(i));
         typenames.add(first.getMetaData().getColumnTypeName(i));
         }
      
         schemaList.add(names);
         schemaList.add(typenames);*/

        HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
        h.put("schema", schemaList);
        h.put("errors", new ArrayList<ArrayList<String>>());
        Gson g = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append(g.toJson(h, h.getClass()));

        while (first.next()) {

            String otherSchema = first.getString(6);
            if (otherSchema == null) {
                //mysql returns cat?
                otherSchema = first.getString(5);
            }
            String foreignId = "";
            Iterator<Schema> endpoints2 = cons.getSchemas().iterator();
            while (endpoints2.hasNext()) {
                Schema nextEndpoint2 = endpoints2.next();

                String nextSchema = nextEndpoint2.getId();
                if (otherSchema.equalsIgnoreCase(nextSchema)) {
                    foreignId = nextEndpoint2.getId();
                    break;
                }
            }
            if (foreignId.equals("")) {
                continue;
                //references a table in an unknown schema
            }
            sb.append("\n");
            ArrayList<Object> res = new ArrayList<Object>();
            for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                if (i == 3) {
                    //table names
                    res.add(dbID + "_" + first.getString(i));
                } else if (i == 2) {
                    res.add("adp");
                } else if (i == 6) {
                    if (foreignId.equals("")) {
                        res.add(first.getString(6));
                    } else {
                        res.add("adp");
                    }
                } else if (i == 7) {
                    if (foreignId.equals("")) {
                        res.add(first.getString(7));
                    } else {
                        res.add(foreignId + "_" + first.getString(i));
                    }
                } else {
                    res.add(first.getObject(i));
                }
            }
            sb.append(g.toJson((ArrayList<Object>) res));
        }
        first.close();

        /*   while(endpoints.hasNext()){
         dbID=endpoints.next();
         ResultSet next=cons.get(dbID).getMetaData().getImportedKeys(catalog, schema, table);
         while(next.next()){
         ArrayList<Object> res=new ArrayList<Object>();
         for(int i=1;i<first.getMetaData().getColumnCount();i++){
         if(i==3 || i==7){
         //table names
         res.add(dbID+"_"+first.getString(i));
         }
         else{
         res.add(first.getObject(i));
         }
         }
         sb.append(g.toJson((ArrayList<Object>) res));
         }}
         */
        AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
        result.setCloseStOnClose(true);
        return result;

    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema,
                                       String parentTable, String foreignCatalog,
                                       String foreignSchema, String foreignTable)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        /*add all the column types from the first DB. Then for each DB
         * add columns types whose int id does not exist so far. !!what should we do
         * when different DBs have the type with same id but different other
         * fields e.g. precision?
         */
        FederatedConnections cons = con.getFederatedConnections();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }

        Iterator<Schema> endpoints = cons.getSchemas().iterator();
        Schema nextEndpoint = endpoints.next();
        ResultSet first = cons.getConnection(nextEndpoint).getMetaData().getTypeInfo();

        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
        for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
            ArrayList<String> nextCouple = new ArrayList<String>();
            nextCouple.add(first.getMetaData().getColumnName(i).toUpperCase());
            nextCouple.add(first.getMetaData().getColumnTypeName(i));
            schema.add(nextCouple);
        }
        /*ArrayList<String> typenames=new ArrayList<String>();
         ArrayList<String> names=new ArrayList<String>();
         for(int i=1;i<first.getMetaData().getColumnCount()+1;i++){
         names.add(first.getMetaData().getColumnName(i));
         typenames.add(first.getMetaData().getColumnTypeName(i));
         }
      
         schema.add(names);
         schema.add(typenames);*/
        HashSet<Integer> usedtypes = new HashSet<>();
        HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
        h.put("schema", schema);
        h.put("errors", new ArrayList<ArrayList<String>>());
        Gson g = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append(g.toJson(h, h.getClass()));

        while (first.next()) {
            sb.append("\n");
            usedtypes.add(Integer.valueOf(first.getInt(2)));
            ArrayList<Object> res = new ArrayList<Object>();
            for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                res.add(first.getObject(i));
            }
            sb.append(g.toJson((ArrayList<Object>) res));
        }
        first.close();

        while (endpoints.hasNext()) {
            nextEndpoint = endpoints.next();

            ResultSet next = cons.getConnection(nextEndpoint).getMetaData().getTypeInfo();
            while (next.next()) {

                if (!usedtypes.contains(Integer.valueOf(next.getInt(2)))) {
                    sb.append("\n");
                    usedtypes.add(Integer.valueOf(next.getInt(2)));
                    ArrayList<Object> res = new ArrayList<Object>();
                    for (int i = 1; i < next.getMetaData().getColumnCount() + 1; i++) {
                        res.add(next.getObject(i));
                    }
                    sb.append(g.toJson((ArrayList<Object>) res));
                }
            }
            next.close();
        }
        AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
        result.setCloseStOnClose(true);
        return result;
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table,
                                  boolean unique, boolean approximate) throws
            SQLException {
        FederatedConnections cons = con.getFederatedConnections();
        if (cons.isEmpty()) {
            throw new SQLException("No federated endpoints found");
        }

        String dbID = "";
        Schema nextEndpoint = null;
        Iterator<Schema> endpoints = cons.getSchemas().iterator();
        while (endpoints.hasNext()) {
            nextEndpoint = endpoints.next();
            dbID = nextEndpoint.getId();

            if (table.toUpperCase().startsWith(dbID.toUpperCase() + "_")) {
                table = table.substring(dbID.length() + 1);
                Connection c = cons.getConnection(nextEndpoint);
                String schemaPatt = nextEndpoint.getSchema();
                if (c.getClass().getName().contains("postgresql")) {
                    schemaPatt = null;
                }
                ResultSet first = cons.getConnection(nextEndpoint).getMetaData().getIndexInfo(catalog, schemaPatt, table, unique, approximate);

                ArrayList<ArrayList<String>> schemaList = new ArrayList<ArrayList<String>>();
                for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                    ArrayList<String> nextCouple = new ArrayList<String>();
                    nextCouple.add(first.getMetaData().getColumnName(i).toUpperCase());
                    nextCouple.add(first.getMetaData().getColumnTypeName(i));
                    schemaList.add(nextCouple);
                }
                /* ArrayList<String> typenames=new ArrayList<String>();
                 ArrayList<String> names=new ArrayList<String>();
                 for(int i=1;i<first.getMetaData().getColumnCount()+1;i++){
                 names.add(first.getMetaData().getColumnName(i));
                 typenames.add(first.getMetaData().getColumnTypeName(i));
                 }
                 schemaList.add(names);
                 schemaList.add(typenames);*/

                HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
                h.put("schema", schemaList);
                h.put("errors", new ArrayList<ArrayList<String>>());
                Gson g = new Gson();
                StringBuilder sb = new StringBuilder();
                sb.append(g.toJson(h, h.getClass()));

                while (first.next()) {
                    sb.append("\n");
                    ArrayList<Object> res = new ArrayList<Object>();
                    for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                        if (i == 3) {
                            //table names
                            res.add(dbID + "_" + first.getString(i));
                        } else if (i == 2) {
                            res.add("adp");
                        } else {
                            res.add(first.getObject(i));
                        }
                    }
                    sb.append(g.toJson((ArrayList<Object>) res));
                }
                first.close();
                AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
                result.setCloseStOnClose(true);
                return result;
            }
        }
        try {
            Statement exSt = this.con.createStatement();
            ResultSet internalExaremeIndex = exSt.executeQuery("getIndexInfo " + catalog + " null " + table + " " + unique + " " + approximate);
            ArrayList<ArrayList<String>> schemaList = new ArrayList<ArrayList<String>>();
            for (int i = 1; i < internalExaremeIndex.getMetaData().getColumnCount() + 1; i++) {
                ArrayList<String> nextCouple = new ArrayList<String>();
                nextCouple.add(internalExaremeIndex.getMetaData().getColumnName(i).toUpperCase());
                nextCouple.add(internalExaremeIndex.getMetaData().getColumnTypeName(i));
                schemaList.add(nextCouple);
            }
            /* ArrayList<String> typenames=new ArrayList<String>();
             ArrayList<String> names=new ArrayList<String>();
             for(int i=1;i<first.getMetaData().getColumnCount()+1;i++){
             names.add(first.getMetaData().getColumnName(i));
             typenames.add(first.getMetaData().getColumnTypeName(i));
             }
             schemaList.add(names);
             schemaList.add(typenames);*/

            HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
            h.put("schema", schemaList);
            h.put("errors", new ArrayList<ArrayList<String>>());
            Gson g = new Gson();
            StringBuilder sb = new StringBuilder();
            sb.append(g.toJson(h, h.getClass()));
            // ResultSet internalExaremeTables = con.getRegistryConnection().getMetaData().getTables(catalog, schemaPatt, tableNamePattern, types);
            while (internalExaremeIndex.next()) {
                sb.append("\n");
                ArrayList<Object> res = new ArrayList<Object>();
                for (int i = 1; i < internalExaremeIndex.getMetaData().getColumnCount() + 1; i++) {
                    if (i == 2) {
                        res.add("adp");
                    } else {
                        res.add(internalExaremeIndex.getObject(i));
                    }
                }
                sb.append(g.toJson((ArrayList<Object>) res));
            }
            internalExaremeIndex.close();
            exSt.close();
            AdpResultSet result = new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());
            result.setCloseStOnClose(true);
            return result;
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern,
                             String typeNamePattern, int[] types) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.con;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern,
                                   String typeNamePattern) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern,
                                    String tableNamePattern) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern,
                                   String typeNamePattern,
                                   String attributeNamePattern) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getSQLStateType() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern,
                                  String functionNamePattern) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern,
                                        String functionNamePattern,
                                        String columnNamePattern) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern,
                                      String tableNamePattern,
                                      String columnNamePattern) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T
    unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
