/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.jdbc.federated;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author heraldkllapi
 */
public class AdpResultSetMetaData implements ResultSetMetaData {
    private AdpResultSet resultSet;
    private ArrayList<String> typenames;
    private ArrayList<String> names;
    // private ArrayList<ArrayList<String>> metadata;

    public AdpResultSetMetaData(AdpResultSet adpResultSet, ArrayList<ArrayList<String>> metadata) {
        this.resultSet = adpResultSet;
        // ArrayList<ArrayList<String>> metadata = metadataRow.get("schema");
        //this.metadata = metadata;
        typenames = new ArrayList<String>();
        names = new ArrayList<String>();
        for (int i = 0; i < metadata.size(); i++) {
            names.add(metadata.get(i).get(0));
            typenames.add(metadata.get(i).get(1));
        }
        // typenames = metadata.get(0);
        //names = metadata.get(1);
    }

    @Override public int getColumnCount() throws SQLException {

        return this.names.size();
    }

    @Override public boolean isAutoIncrement(int column) throws SQLException {
        return false;
    }

    @Override public boolean isCaseSensitive(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean isSearchable(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean isCurrency(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int isNullable(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean isSigned(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getColumnDisplaySize(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String getColumnLabel(int column) throws SQLException {
        return this.names.get(column - 1); //will have "AS" to take into consideration???
    }

    @Override public String getColumnName(int column) throws SQLException {
        return this.names.get(column - 1);
    }

    @Override public String getSchemaName(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getPrecision(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getScale(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String getTableName(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String getCatalogName(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getColumnType(int column) throws SQLException {
        String typeName = getColumnTypeName(column).toUpperCase();
        if (typeName.equals("TEXT") || typeName.equals("NVARCHAR") || typeName.
            equals("VARYING CHARACTER") || typeName.equals("VARCHAR")) {
            return java.sql.Types.VARCHAR;
        }
        if (typeName.equals("TINYINT")) {
            return java.sql.Types.TINYINT;
        }
        if (typeName.equals("SMALLINT") || typeName.equals("INT2")) {
            return java.sql.Types.SMALLINT;
        }
        if (typeName.equals("BIGINT") || typeName.equals("INT8") || typeName
            .equals("UNSIGNED BIG INT")) {
            return java.sql.Types.BIGINT;
        }
        if (typeName.equals("DATE") || typeName.equals("DATETIME")) {
            return java.sql.Types.DATE;
        }
        if (typeName.equals("INT") || typeName.equals("INTEGER") || typeName.equals("MEDIUMINT")) {
            return java.sql.Types.INTEGER;
        }
        if (typeName.equals("DECIMAL")) {
            return java.sql.Types.DECIMAL;
        }
        if (typeName.equals("NUMERIC")) {
            return java.sql.Types.NUMERIC;
        }
        if (typeName.equals("DOUBLE") || typeName.equals("DOUBLE PRECISION")) {
            return java.sql.Types.DOUBLE;
        }
        if (typeName.equals("REAL")) {
            return java.sql.Types.REAL;
        }
        if (typeName.equals("FLOAT")) {
            return java.sql.Types.FLOAT;
        }
        if (typeName.equals("CHARACTER") || typeName.equals("NCHAR") || typeName
            .equals("NATIVE CHARACTER")) {
            return java.sql.Types.CLOB;
        }
        if (typeName.equals("CLOB")) {
            return java.sql.Types.BINARY;
        }
        if (typeName.equals("BINARY")) {
            return java.sql.Types.BLOB;
        }
        return java.sql.Types.NULL;


    }

    @Override public String getColumnTypeName(int column) throws SQLException {
        return typenames.get(column - 1);
    }

    @Override public boolean isReadOnly(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean isWritable(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean isDefinitelyWritable(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String getColumnClassName(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getColumnNumber(String name) throws SQLException {
        for (int i = 0; i < this.names.size(); i++) {
            if (this.names.get(i).equals(name)) {
                return i + 1;
            }
        }
        throw new SQLException("No column with name " + name + " exists.");
    }
}
