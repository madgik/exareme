/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema;

import madgik.exareme.common.schema.expression.SQLSelect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author herald
 * @author Christoforos Svingos
 */
public class Select extends Query {
    private static final long serialVersionUID = 1L;

    private String comments = null;
    private TableView outputTable = null;
    private ArrayList<TableView> inputTables = null;
    private HashMap<String, TableView> inputTablesMap = null;

    private SQLSelect parsedSqlQuery = null;
    private List<String> queryStatements = null;

    public Select(int id, SQLSelect sqlQuery, TableView outputTable) {
        super(id, sqlQuery.getSql(), sqlQuery.getComments().toString());
        this.outputTable = outputTable;
        this.parsedSqlQuery = sqlQuery;
        this.comments = parsedSqlQuery.getComments().toString();

        this.inputTables = new ArrayList<>();
        this.inputTablesMap = new HashMap<>();
        this.queryStatements = new ArrayList<>();
    }

    public void addInput(TableView input) {
        inputTables.add(input);
        inputTablesMap.put(input.getName(), input);
    }

    public void addQueryStatement(String query) {
        this.queryStatements.add(query);
    }

    public void clearQueryStatement() {
        this.queryStatements.clear();
    }

    public List<String> getQueryStatements() {
        return this.queryStatements;
    }

    public String getSelectQueryStatement() {
        String query;
        if (this.queryStatements.size() == 0) {
            query = getQuery();
        } else {
            query = this.queryStatements.get(this.queryStatements.size() - 1);
        }

        return query;
    }

    public TableView getOutputTable() {
        return outputTable;
    }

    public void setOutputTable(TableView outputTable) {
        this.outputTable = outputTable;
    }

    public List<TableView> getInputTables() {
        return Collections.unmodifiableList(inputTables);
    }

    public void clearInputTables() {
        inputTables.clear();
        inputTablesMap.clear();
    }

    public void renameInputTable(String oldName, String newName){
        inputTablesMap.put(newName,inputTablesMap.remove(oldName));
    }

    public SQLSelect getParsedSqlQuery() {
        return parsedSqlQuery;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Database   : " + getDatabaseDir() + "\n");
        if (outputTable != null) {
            sb.append("Output     : " + outputTable.toString() + "\n");
        } else {
            sb.append("Output     : --\n");
        }
        sb.append("Inputs     : " + inputTables.size() + "\n");
        for (TableView in : inputTables) {
            sb.append(" -> : " + in.toString() + "\n");
        }
        if (comments != null) {
            sb.append(comments);
        }
        sb.append(getQuery() + ";");

        return sb.toString();
    }
}
