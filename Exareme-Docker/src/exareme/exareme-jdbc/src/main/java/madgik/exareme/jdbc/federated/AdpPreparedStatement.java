/**
 * Copyright MaDgIK Group 2010 - 2013.
 */
package madgik.exareme.jdbc.federated;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dimitris
 */
final class AdpPreparedStatement extends AdpStatement implements
        PreparedStatement, ParameterMetaData {
    private String sql;
    private AdpConnection con;
    private boolean closeOnCompletion;
    private Gson g;
    private HttpClient httpclient;
    private InputStreamReader result;
    private int timeout;

    public AdpPreparedStatement(AdpConnection conn, String sqlInitial) {
        super(conn);
        this.sql = sqlInitial;
        this.con = conn;
        this.closeOnCompletion = false;
        this.g = new Gson();
        httpclient = new DefaultHttpClient();
        timeout = 0;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        if (sql.startsWith("addFederatedEndpoint") || sql.startsWith("removeFederatedEndpoint")) {
            AdpRequest q = new AdpRequest(sql, "query", con.getDbPath(), String.valueOf(timeout));
            getResponse(q);
            return new AdpResultSet(manageFederatedEndpoints(sql), this);
        } else if (sql.startsWith("select * from") && sql.endsWith("where 0 = 1")) {
            //schema spy query to collect metadata!!!!
            //hack to execute directly to endpoint without connecting to ADP server

            String table = sql.split(" ")[3];
            if (table.startsWith("adp")) {
                table = table.substring(4);
            }
            table = table.replaceAll("\"", "");
            for (Schema endpoint : this.con.getFederatedConnections().getSchemas()) {
                if (table.toUpperCase().startsWith(endpoint.getId().toUpperCase() + "_")) {
                    Connection remote = this.con.getFederatedConnections().getConnection(endpoint);
                    String remoteTableName = table.substring(endpoint.getId().length() + 1);
                    StringBuilder sb;
                    try (Statement s = remote.createStatement()) {
                        ResultSet first;
                        first = s.executeQuery("select * from " + endpoint.getSchema() + "." + remoteTableName + " where 0=1");
                        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
                        for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {
                            ArrayList<String> nextCouple = new ArrayList<String>();
                            nextCouple.add(first.getMetaData().getColumnName(i));
                            nextCouple.add(first.getMetaData().getColumnTypeName(i));
                            schema.add(nextCouple);
                        }
                        HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
                        h.put("schema", schema);
                        h.put("errors", new ArrayList<ArrayList<String>>());
                        Gson g = new Gson();
                        sb = new StringBuilder();
                        sb.append(g.toJson(h, h.getClass()));
                        while (first.next()) {
                            sb.append("\n");
                            ArrayList<Object> res = new ArrayList<Object>();
                            for (int i = 1; i < first.getMetaData().getColumnCount() + 1; i++) {

                                res.add(first.getObject(i));
                            }

                            sb.append(g.toJson((ArrayList<Object>) res));
                        }
                        first.close();
                    }

                    return new AdpResultSet(new InputStreamReader(new StringBufferInputStream(sb.toString())), this.con.createStatement());

                }
            }
        }
        if (sql.endsWith(") LIMIT 1") || sql.endsWith(") LIMIT 10")) {
            sql = sql.replace(") LIMIT 1", ") derivedtablealias LIMIT 1");
        }
        AdpRequest q = new AdpRequest(sql, "query", con.getDbPath(), String.valueOf(timeout));
        getResponse(q);
        return new AdpResultSet(result, this);

    }

    @Override
    public int executeUpdate() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
        // return executeUpdate(sql);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearParameters() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean execute() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addBatch() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        //Execute SQL!!!! expensive
        return this.executeQuery().getMetaData();

    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType,
                          int scaleOrLength) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws
            SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getParameterCount() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int isNullable(int param) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSigned(int param) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getPrecision(int param) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getScale(int param) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getParameterType(int param) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getParameterTypeName(int param) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getParameterClassName(int param) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getParameterMode(int param) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void getResponse(AdpRequest q) throws SQLException {
        String json = g.toJson(q);
        try {
            HttpPost httppost = new HttpPost(con.getMetaData().getURL());
            StringEntity stringEntity = new StringEntity(json);
            stringEntity.setContentType("application/json");
            httppost.setEntity(stringEntity);
            HttpResponse response = httpclient.execute(httppost);

            HttpEntity resEntity = response.getEntity();
            result = new InputStreamReader(resEntity.getContent());
        } catch (java.io.IOException e) {
            throw new SQLException("Connection to Database failed");
        }
    }

    private InputStreamReader manageFederatedEndpoints(String sql) throws SQLException {
        FederatedConnections federatedCons = con.getFederatedConnections();
        String sqlParams = sql.substring(sql.indexOf("(") + 1, sql.lastIndexOf(")"));
        sqlParams = sqlParams.replaceAll(" ", "");
        String[] params = sqlParams.split(",");
        if (sql.startsWith("addFederatedEndpoint")) {


            try {
                Class.forName(params[2]);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AdpStatement.class.getName()).log(Level.SEVERE, null, ex);
            }
            Connection conn = null;
            conn = DriverManager.getConnection(params[1], params[3], params[4]);
            federatedCons.putSchema(new Schema(params[0], params[5]), conn);
            return createOKResultStreamReader();
        } else {
            federatedCons.removeSchema(new Schema(params[0], params[5]));
            return createOKResultStreamReader();
        }

    }

    private InputStreamReader createOKResultStreamReader() {
        //HashMap<String, ArrayList<ArrayList<String>>> firstRow=new HashMap<String, ArrayList<ArrayList<String>>>();
        ArrayList<ArrayList<String>> schema = new ArrayList<ArrayList<String>>();
        // ArrayList<String> typenames=new ArrayList<String>();
        //typenames.add("VARCHAR");
        // schema.add(typenames);
        ArrayList<String> names = new ArrayList<String>();
        names.add("RESULT");
        names.add("VARCHAR");
        schema.add(names);
        HashMap<String, ArrayList<ArrayList<String>>> h = new HashMap<String, ArrayList<ArrayList<String>>>();
        h.put("schema", schema);
        h.put("errors", new ArrayList<ArrayList<String>>());
        Gson g = new Gson();
        StringBuilder sb = new StringBuilder();
        sb.append(g.toJson(h, h.getClass()));
        sb.append("\n");
        ArrayList<Object> res = new ArrayList<Object>();
        res.add("OK");
        sb.append(g.toJson((ArrayList<Object>) res));
        return new InputStreamReader(new StringBufferInputStream(sb.toString()));
    }
}