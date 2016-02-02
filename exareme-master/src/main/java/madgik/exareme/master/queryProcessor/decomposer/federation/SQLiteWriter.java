package madgik.exareme.master.queryProcessor.decomposer.federation;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


import org.apache.log4j.Logger;

import madgik.exareme.master.queryProcessor.decomposer.DecomposerUtils;

public class SQLiteWriter extends Writer {
	
	private static final Logger log = Logger.getLogger(SQLiteWriter.class);

	private final int batchSize = DecomposerUtils.NO_OF_RECORDS;;
	private PreparedStatement sqliteStatement;
	private int buffer = 0;
	private Connection sqliteConnection;

	public SQLiteWriter() {
		super();
	}

	public SQLiteWriter(Connection sqliteConn, int noOfBufferedRecords,
			String query, Statement statement, String sql,
			StringBuilder createTableSQL) throws SQLException {
		super();
		ResultSet resultSet = statement.executeQuery(query + " LIMIT 0 ");

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
		sqliteConnection = sqliteConn;
		Statement creatSt = sqliteConnection.createStatement();
		creatSt.execute(createTableSQL.toString());
		creatSt.close();
		sqliteStatement = sqliteConnection.prepareStatement(sql);
		
	}

	public SQLiteWriter(Object lock) {
		super(lock);
	}

	@Override
	public void close() throws IOException {
		flush();
		try {
			sqliteStatement.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void flush() throws IOException {
		//log.debug("Flushing! Record size:"+records.size());
		try {
			
			sqliteStatement.executeBatch();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(char[] arg0, int arg1, int arg2) throws IOException {
		String s = new String(arg0);
		write(s.substring(arg1, arg1 + arg2));
	}

	public void write(char buf[]) throws IOException {
		write(buf, 0, buf.length);
	}

	public void write(String record) throws IOException {
		try {
		//CSVParser parser = CSVParser.parse(record, CSVFormat.DEFAULT);
		//for (CSVRecord csvRecord : parser) {
		//	for(int i=0;i<csvRecord.size();i++){
				String[] s=record.split("#");
				for(int i=0;i<s.length;i++){
					sqliteStatement.setObject(i+1, s[i]);
				}
				sqliteStatement.addBatch();
		//	}
			buffer++;
	//	}
		if (buffer >= batchSize) {
			buffer=0;
			sqliteStatement.executeBatch();
		}
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

}
