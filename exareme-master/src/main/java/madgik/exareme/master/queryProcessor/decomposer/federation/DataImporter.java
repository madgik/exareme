package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.DecomposerUtils;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import madgik.exareme.master.queryProcessor.decomposer.query.Table;
import org.apache.log4j.Logger;

import java.sql.*;

public class DataImporter implements Runnable {
    private SQLQuery s;
    private String dbPath;
    private static final Logger log = Logger.getLogger(DataImporter.class);

    public DataImporter(SQLQuery q, String db) {
        this.s = q;
        this.dbPath = db;
    }

    @Override public void run() {
        DB db = DBInfoReaderDB.dbInfo.getDBForMadis(s.getMadisFunctionString());
        if (db == null) {
            log.error("Could not import Data. DB not found:" + s.getMadisFunctionString());
            return;
        }
        String driverClass = db.getDriver();
        try {
            Class.forName(driverClass);
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e1) {
            log.error("Could not import Data. Driver not found:" + driverClass);
            return;
        }

        String conString = db.getURL();

        String qString = s.getExecutionStringInFederatedSource();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        long start = -1;
        int count = 0;
        Connection sqliteConnection = null;
        PreparedStatement sqliteStatement = null;

        try {
            sqliteConnection = DriverManager.getConnection(
                "jdbc:sqlite:" + dbPath + "import/" + s.getTemporaryTableName() + ".db");
            //statement.setQueryTimeout(30);
            sqliteConnection.setAutoCommit(false);
            connection = DriverManager.getConnection(conString, db.getUser(), db.getPass());
            int fetch = 100;
            if (db.getDriver().contains("OracleDriver")) {
                fetch = DecomposerUtils.FETCH_SIZE_ORACLE;
            } else if (db.getDriver().contains("postgresql")) {
                fetch = DecomposerUtils.FETCH_SIZE_POSTGRES;
            } else if (db.getDriver().contains("mysql")) {
                fetch = DecomposerUtils.FETCH_SIZE_MYSQL;
            }

            String sql = "insert into " + s.getTemporaryTableName() + " values (";

            StringBuilder createTableSQL = new StringBuilder();
            createTableSQL.append("CREATE TABLE ");
            createTableSQL.append(s.getTemporaryTableName());
            createTableSQL.append("( ");

            statement =
                connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(fetch);
            start = System.currentTimeMillis();
            resultSet = statement.executeQuery(qString);

            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            String comma = "";
            String questionmark = "?";
            for (int i = 1; i <= columnsNumber; i++) {
                sql += questionmark;
                questionmark = ",?";
                String l = rsmd.getColumnLabel(i);
                int type = rsmd.getColumnType(i);
                String coltype = "";
                if (JdbcDatatypesToSQLite.intList.contains(type)) {
                    coltype = "INTEGER";
                } else if (JdbcDatatypesToSQLite.numericList.contains(type)) {
                    coltype = "NUMERIC";
                } else if (JdbcDatatypesToSQLite.realList.contains(type)) {
                    coltype = "REAL";
                } else if (JdbcDatatypesToSQLite.textList.contains(type)) {
                    coltype = "TEXT";
                } else if (JdbcDatatypesToSQLite.BLOB == type) {
                    coltype = "BLOB";
                }
                createTableSQL.append(comma);
                createTableSQL.append(l);
                createTableSQL.append(" ");
                createTableSQL.append(coltype);
                comma = ",";
            }
            sql += ")";
            createTableSQL.append(")");
            Statement creatSt = sqliteConnection.createStatement();
            creatSt.execute(createTableSQL.toString());
            creatSt.close();
            sqliteStatement = sqliteConnection.prepareStatement(sql);

            while (resultSet.next()) {

                final int batchSize = 1000;

                for (int i = 1; i <= columnsNumber; i++) {

                    sqliteStatement.setObject(i, resultSet.getObject(i));
                }

                sqliteStatement.addBatch();

                if (++count % batchSize == 0) {
                    sqliteStatement.executeBatch();
                }
            }
            sqliteStatement.executeBatch(); // insert remaining records
            sqliteConnection.commit();
            sqliteStatement.close();
            sqliteConnection.close();
            resultSet.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not import data from endpoint\n" + e.getMessage());
            return;
        }

        log.debug(
            count + " rows were imported in " + (System.currentTimeMillis() - start) + "msec");
        StringBuilder madis = new StringBuilder();
        madis.append("sqlite '");
        madis.append(this.dbPath);
        madis.append("import/");
        madis.append(s.getTemporaryTableName());
        madis.append(".db' ");
        s.setMadisFunctionString(madis.toString());
        s.setSelectAll(true);

        s.removeInfo();
        s.getInputTables().add(new Table(s.getTemporaryTableName(), s.getTemporaryTableName()));

    }

}
