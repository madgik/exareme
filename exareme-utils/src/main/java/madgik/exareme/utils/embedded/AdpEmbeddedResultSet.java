/**
 * Copyright MaDgIK Group 2010 - 2013.
 */
package madgik.exareme.utils.embedded;

import com.google.gson.Gson;
import madgik.exareme.utils.embedded.process.QueryResultStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author herald
 */
public class AdpEmbeddedResultSet implements ResultSet {
  private final QueryResultStream inputStream;
  private final AdpEmbeddedStatement statement;
  private final Gson jsonParser;
  int currentNumberOfRecordColumns;
  private AdpEmbeddedResultSetMetaData rsMetadata;
  private HashMap<String, ArrayList<ArrayList<String>>> schema;
  private HashMap<String, Object> error;
  private int numRows = 0;
  private ArrayList<Object> currentRecord;
  private boolean closed = false;

  public AdpEmbeddedResultSet(QueryResultStream inputStream, AdpEmbeddedStatement statement)
      throws SQLException {
    this.inputStream = inputStream;
    this.statement = statement;
    this.jsonParser = new Gson();
    this.schema = new HashMap<String, ArrayList<ArrayList<String>>>();
    this.error = new HashMap<String, Object>();
    this.currentRecord = new ArrayList<Object>();
    this.currentNumberOfRecordColumns = -1;
    // Get the metadata
    try {
      String schemaStr = inputStream.getSchema();
      // Check for errors
      error = jsonParser.fromJson(schemaStr, error.getClass());
      checkForError();

      schema = jsonParser.fromJson(schemaStr, schema.getClass());
      this.rsMetadata = new AdpEmbeddedResultSetMetaData(this, schema.get("schema"));
    } catch (Exception e) {
      throw new SQLException(e.getMessage());
    }
  }

  @Override
  public Statement getStatement() throws SQLException {
    return this.statement;
  }

  @Override
  public boolean next() throws SQLException {
    checkForError();
    try {
      String nextRecord = inputStream.getNextRecord();
      if (nextRecord == null) {
        inputStream.close();
        this.currentNumberOfRecordColumns = -1;
        return false;
      }
      currentRecord = jsonParser.fromJson(nextRecord, currentRecord.getClass());
      if (this.currentNumberOfRecordColumns != -1
          && currentRecord.size() != this.currentNumberOfRecordColumns) {
        throw new RuntimeException(
            "Different Number Of Columns: Posible " + "multiple queries in one statement");
      }

      this.currentNumberOfRecordColumns = currentRecord.size();
      return true;
    } catch (IOException e) {
      throw new SQLException("Cannot read record", e);
    }
  }

  @Override
  public void close() throws SQLException {
    if (closed) {
      return;
    }
    try {
      inputStream.close();
      this.currentNumberOfRecordColumns = -1;
    } catch (Exception e) {
      throw new SQLException("Cannot close data input stream!", e);
    }
    closed = true;
  }

  @Override
  public boolean isClosed() throws SQLException {
    return closed;
  }

  @Override
  public String getString(int columnIndex) throws SQLException {
    // TODO: Make this correct
    if (true) {
      return "" + currentRecord.get(columnIndex - 1);
    }

    int type = rsMetadata.getColumnType(columnIndex);
    if (type == java.sql.Types.VARCHAR || type == java.sql.Types.LONGVARCHAR
        || type == java.sql.Types.CHAR) {
      return "" + currentRecord.get(columnIndex - 1); //to fix later!
    } else if (type == java.sql.Types.NULL) {
      return null;
    } else {
//            System.out.println(this.currentRecord);
      throw new SQLException("Column " + columnIndex + " is not a String");
    }
  }

  @Override
  public boolean getBoolean(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.BIT) {
      return (Boolean) currentRecord.get(columnIndex - 1);
    } else if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.NULL) {
      return false;
    } else {
      throw new SQLException("Column " + columnIndex + " is not a Boolean");
    }
  }

  @Override
  public byte getByte(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.TINYINT) {
      return (Byte) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Byte");
    }
  }

  @Override
  public short getShort(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.SMALLINT) {
      return (Short) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Short");
    }
  }

  @Override
  public int getInt(int columnIndex) throws SQLException {
    // if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.INTEGER) {
    try {
      return (Integer) currentRecord.get(columnIndex - 1);
    } catch (java.lang.ClassCastException e) {
      return Math.round(Float.parseFloat(currentRecord.get(columnIndex - 1).
          toString()));
    }
  }

  @Override
  public long getLong(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.BIGINT) {
      return (Long) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Long");
    }
  }

  @Override
  public float getFloat(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.REAL
        || rsMetadata.getColumnType(columnIndex) == java.sql.Types.FLOAT) {
      return (Float) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Float");
    }
  }

  @Override
  public double getDouble(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.DOUBLE) {
      return (Double) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Double");
    }
  }

  @Override
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.NUMERIC) {
      return (BigDecimal) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not BigDecimal");
    }
  }

  @Override
  public Date getDate(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.DATE) {
      return (Date) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Date");
    }
  }

  @Override
  public Time getTime(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.TIME) {
      return (Time) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Time");
    }
  }

  @Override
  public Timestamp getTimestamp(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.TIMESTAMP) {
      return (Timestamp) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Timestamp");
    }
  }

  @Override
  public String getString(String columnLabel) throws SQLException {
//        System.out.println(columnLabel);
    return getString(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public boolean getBoolean(String columnLabel) throws SQLException {
    return getBoolean(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public byte getByte(String columnLabel) throws SQLException {
    return getByte(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public short getShort(String columnLabel) throws SQLException {
    return getShort(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public int getInt(String columnLabel) throws SQLException {
    return getInt(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public long getLong(String columnLabel) throws SQLException {
    return getLong(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public float getFloat(String columnLabel) throws SQLException {
    return getFloat(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public double getDouble(String columnLabel) throws SQLException {
    return getDouble(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
    return getBigDecimal(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public byte[] getBytes(String columnLabel) throws SQLException {
    return getBytes(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public Date getDate(String columnLabel) throws SQLException {
    return getDate(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public Time getTime(String columnLabel) throws SQLException {
    return getTime(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public Timestamp getTimestamp(String columnLabel) throws SQLException {
    return getTimestamp(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public InputStream getAsciiStream(String columnLabel) throws SQLException {
    return getAsciiStream(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public InputStream getUnicodeStream(String columnLabel) throws SQLException {
    return getUnicodeStream(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public InputStream getBinaryStream(String columnLabel) throws SQLException {
    return getBinaryStream(this.rsMetadata.getColumnNumber(columnLabel));
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return this.rsMetadata;
  }

  @Override
  public Object getObject(int columnIndex) throws SQLException {
    return this.currentRecord.get(columnIndex - 1);
  }


  @Override
  public Blob getBlob(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.BLOB) {
      return (Blob) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Blob");
    }
  }

  @Override
  public Clob getClob(int columnIndex) throws SQLException {
    if (rsMetadata.getColumnType(columnIndex) == java.sql.Types.CLOB) {
      return (Clob) currentRecord.get(columnIndex - 1);
    } else {
      throw new SQLException("Column " + columnIndex + " is not Clob");
    }
  }

  private void checkForError() throws SQLException {
    if (error == null) {
      if (this.inputStream.getException() == null) {
        throw new SQLException("Error is null!");
      } else {
        throw new SQLException(this.inputStream.getException().getMessage());
      }
    }
    Object strError = error.get("error");
    if (strError != null) {
      try {
        inputStream.close();
        this.currentNumberOfRecordColumns = -1;
      } catch (IOException e) {
        throw new SQLException("Cannot close stream", e);
      }
      throw new SQLException(strError.toString());
    }
  }

  // NOT IMPLEMENTED BELOW


  @Override
  public SQLWarning getWarnings() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void clearWarnings() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getCursorName() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public byte[] getBytes(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean wasNull() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public InputStream getAsciiStream(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public InputStream getUnicodeStream(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public InputStream getBinaryStream(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object getObject(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int findColumn(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Reader getCharacterStream(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Reader getCharacterStream(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isBeforeFirst() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isAfterLast() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isFirst() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isLast() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void beforeFirst() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void afterLast() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean first() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean last() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getRow() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean absolute(int row) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean relative(int rows) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean previous() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getFetchDirection() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getFetchSize() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getType() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getConcurrency() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean rowUpdated() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean rowInserted() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean rowDeleted() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNull(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBoolean(int columnIndex, boolean x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateByte(int columnIndex, byte x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateShort(int columnIndex, short x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateInt(int columnIndex, int x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateLong(int columnIndex, long x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateFloat(int columnIndex, float x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateDouble(int columnIndex, double x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateString(int columnIndex, String x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBytes(int columnIndex, byte[] x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateDate(int columnIndex, Date x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateTime(int columnIndex, Time x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, int length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, int length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, int length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateObject(int columnIndex, Object x, int scaleOrLength)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateObject(int columnIndex, Object x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNull(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBoolean(String columnLabel, boolean x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateByte(String columnLabel, byte x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateShort(String columnLabel, short x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateInt(String columnLabel, int x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateLong(String columnLabel, long x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateFloat(String columnLabel, float x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateDouble(String columnLabel, double x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateString(String columnLabel, String x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBytes(String columnLabel, byte[] x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateDate(String columnLabel, Date x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateTime(String columnLabel, Time x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, int length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, int length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader, int length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateObject(String columnLabel, Object x, int scaleOrLength)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateObject(String columnLabel, Object x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void insertRow() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateRow() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteRow() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void refreshRow() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void cancelRowUpdates() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void moveToInsertRow() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void moveToCurrentRow() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object getObject(int columnIndex, Map<String, Class<?>> map)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Ref getRef(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Array getArray(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object getObject(String columnLabel, Map<String, Class<?>> map)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Ref getRef(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Blob getBlob(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Clob getClob(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Array getArray(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Date getDate(int columnIndex, Calendar cal) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Date getDate(String columnLabel, Calendar cal) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Time getTime(int columnIndex, Calendar cal) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Time getTime(String columnLabel, Calendar cal) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public URL getURL(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public URL getURL(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateRef(int columnIndex, Ref x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateRef(String columnLabel, Ref x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBlob(int columnIndex, Blob x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBlob(String columnLabel, Blob x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateClob(int columnIndex, Clob x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateClob(String columnLabel, Clob x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateArray(int columnIndex, Array x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateArray(String columnLabel, Array x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public RowId getRowId(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public RowId getRowId(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateRowId(int columnIndex, RowId x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateRowId(String columnLabel, RowId x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getHoldability() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNString(int columnIndex, String nString) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNString(String columnLabel, String nString) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public NClob getNClob(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public NClob getNClob(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SQLXML getSQLXML(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SQLXML getSQLXML(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getNString(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getNString(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Reader getNCharacterStream(int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Reader getNCharacterStream(String columnLabel) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateClob(int columnIndex, Reader reader, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateClob(String columnLabel, Reader reader, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader, long length)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateClob(int columnIndex, Reader reader) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateClob(String columnLabel, Reader reader) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
