/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.jdbc.federated;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dimitris
 */
public class AdpConnection implements Connection {

    private URL url;
    private AdpDatabaseMetaData metadata;
    private FederatedConnections federatedCons;
    private String dbPath;

    public AdpConnection(String urlString, Properties info) throws SQLException {
        this.dbPath = "";
        this.federatedCons = new FederatedConnections();
        this.metadata = new AdpDatabaseMetaData(this);
        try {
            URL urlInitial = new URL(urlString.substring(12));

            if (!urlString.contains("-fedDB-")) {
                //no federated endpoints defined
                this.url = urlInitial;
                //change dir to /query/
                this.dbPath = url.getPath();
                url = new URL(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort()
                    + "/decomposer/");
                // this.url=""
            } else {
                //we have feferated DBs
                String[] splitted = urlString.split("-fedDB-");
                this.url = new URL(splitted[0].substring(12));
                //change dir to /query/
                this.dbPath = url.getPath();
                url = new URL(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort()
                    + "/decomposer/");
                for (int i = 1; i < splitted.length; i++) {
                    String[] endpointData = splitted[i].split("-next-");
                    try {
                        Class.forName(endpointData[2]);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(AdpConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Connection conn = DriverManager
                        .getConnection(endpointData[1], endpointData[3], endpointData[4]);
                    federatedCons.putSchema(new Schema(endpointData[0], endpointData[5]), conn);
                    Statement st = this.createStatement();
                    st.executeQuery(
                        "addFederatedEndpoint(" + endpointData[0] + "," + endpointData[1] + ","
                            + endpointData[2] + "," + endpointData[3] + "," + endpointData[4] + ","
                            + endpointData[5] + ")");
                    st.close();

                }

            }

        } catch (MalformedURLException ex) {
            //throw new SQLException(ex.getMessage());
            Logger.getLogger(AdpConnection.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    protected URL getURL() {
        return url;
    }

    @Override public AdpStatement createStatement() throws SQLException {
        return new AdpStatement(this);
    }

    @Override public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new AdpPreparedStatement(this, sql);
    }

    @Override public CallableStatement prepareCall(String sql) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String nativeSQL(String sql) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean getAutoCommit() throws SQLException {
        return true;
    }

    @Override public void setAutoCommit(boolean autoCommit) throws SQLException {
        Logger.getLogger(AdpConnection.class.getName())
            .log(Level.WARNING, "Trying to set autoCommit. Not Supported");
        return;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void commit() throws SQLException {
        Logger.getLogger(AdpConnection.class.getName())
            .log(Level.WARNING, "Trying to commit. Not Supported");
        return;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void rollback() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void close() throws SQLException {
        this.metadata = null;
        this.url = null;
    }

    @Override public boolean isClosed() throws SQLException {
        return this.url == null;
    }

    @Override public DatabaseMetaData getMetaData() throws SQLException {
        return metadata;
    }

    @Override public boolean isReadOnly() throws SQLException {
        return true;
    }

    @Override public void setReadOnly(boolean readOnly) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String getCatalog() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void setCatalog(String catalog) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getTransactionIsolation() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void setTransactionIsolation(int level) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Statement createStatement(int resultSetType, int resultSetConcurrency)
        throws SQLException {
        // return new AdpStatement(this);
        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY) {
            Logger.getLogger(AdpConnection.class.getName()).log(Level.WARNING,
                "Trying to set Statement resultSetType to value different from ResultSet.TYPE_FORWARD_ONLY. Not supported.");
            /*
             throw new UnsupportedOperationException("Resultset type: " + resultSetType
             + " not supported yet.");
             */
        }
        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
            throw new UnsupportedOperationException(
                "Concurrency type: " + resultSetConcurrency + " not supported yet.");
        }
        return new AdpStatement(this);
    }

    @Override public PreparedStatement prepareStatement(String sql, int resultSetType,
        int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getHoldability() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void setHoldability(int holdability) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Savepoint setSavepoint() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Savepoint setSavepoint(String name) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void rollback(Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Statement createStatement(int resultSetType, int resultSetConcurrency,
        int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public PreparedStatement prepareStatement(String sql, int resultSetType,
        int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
        int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public PreparedStatement prepareStatement(String sql, String[] columnNames)
        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Clob createClob() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Blob createBlob() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public NClob createNClob() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public SQLXML createSQLXML() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean isValid(int timeout) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void setClientInfo(String name, String value) throws SQLClientInfoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String getClientInfo(String name) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Properties getClientInfo() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void setClientInfo(Properties properties) throws SQLClientInfoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String getSchema() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void setSchema(String schema) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void abort(Executor executor) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void setNetworkTimeout(Executor executor, int milliseconds)
        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getNetworkTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FederatedConnections getFederatedConnections() {
        return this.federatedCons;
    }

    public String getDbPath() {
        return dbPath;
    }
}
