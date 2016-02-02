package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.common.schema.Partition;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.master.queryProcessor.decomposer.DecomposerUtils;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import madgik.exareme.master.queryProcessor.decomposer.query.Table;
import madgik.exareme.master.registry.Registry;
import madgik.exareme.utils.embedded.db.TableInfo;
import madgik.exareme.worker.art.registry.ArtRegistryLocator;

import org.apache.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DataImporter implements Runnable {
	private SQLQuery s;
	private String dbPath;
	private boolean addToRegistry;
	private static final Logger log = Logger.getLogger(DataImporter.class);

	public DataImporter(SQLQuery q, String db) {
		this.s = q;
		this.dbPath = db;
		this.addToRegistry=false;
	}

	@Override
	public void run() {
		DB db = DBInfoReaderDB.dbInfo.getDBForMadis(s.getMadisFunctionString());
		StringBuilder createTableSQL = new StringBuilder();
		if (db == null) {
			log.error("Could not import Data. DB not found:"
					+ s.getMadisFunctionString());
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
		
		Map<String, String> correspondingOutputs=new HashMap<String, String>();
		if (db.getDriver().contains("OracleDriver")) {
			correspondingOutputs=s.renameOracleOutputs();
		}

		String qString = s.getExecutionStringInFederatedSource(true);
		log.debug("importing:" + qString);
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		long start = -1;
		long count = 0;
		Connection sqliteConnection = null;
		PreparedStatement sqliteStatement = null;
		String importString="import/";
		String part=".db";
		if(addToRegistry){
			importString="";
			part=".0.db";
		}

		try {
			sqliteConnection = DriverManager.getConnection("jdbc:sqlite:"
					+ dbPath + importString + s.getTemporaryTableName() + part);
			// statement.setQueryTimeout(30);
			sqliteConnection.setAutoCommit(false);
			connection = DriverManager.getConnection(conString, db.getUser(),
					db.getPass());
			int fetch = 100;
			if (db.getDriver().contains("OracleDriver")) {
				fetch = DecomposerUtils.FETCH_SIZE_ORACLE;
			} else if (db.getDriver().contains("postgresql")) {
				fetch = DecomposerUtils.FETCH_SIZE_POSTGRES;
				connection.setAutoCommit(false);
			} else if (db.getDriver().contains("mysql")) {
				fetch = DecomposerUtils.FETCH_SIZE_MYSQL;
			}

			String sql = "insert into " + s.getTemporaryTableName()
					+ " values (";

			
			createTableSQL.append("CREATE TABLE ");
			createTableSQL.append(s.getTemporaryTableName());
			createTableSQL.append("( ");

			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			statement.setFetchSize(fetch);
			start = System.currentTimeMillis();

			if (db.getDriver().contains("postgresql")
					&& DecomposerUtils.USE_POSTGRES_COPY) {
				SQLiteWriter swriter=new SQLiteWriter(sqliteConnection, DecomposerUtils.NO_OF_RECORDS, s.getExecutionStringInFederatedSource(false), statement, sql, createTableSQL);
				CopyManager copyManager = new CopyManager((BaseConnection) connection);
	            count=copyManager.copyOut("COPY ("+qString+") TO STDOUT WITH DELIMITER '#'", swriter);
	            swriter.close();
			} else {

				resultSet = statement.executeQuery(qString);

				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				String comma = "";
				String questionmark = "?";
				for (int i = 1; i <= columnsNumber; i++) {
					sql += questionmark;
					questionmark = ",?";
					String l = rsmd.getColumnLabel(i);
					
					if(correspondingOutputs.containsKey(l.toUpperCase())){
						l=correspondingOutputs.get(l.toUpperCase());						
					}
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
				log.debug("executing:" +createTableSQL);
				creatSt.execute(createTableSQL.toString());
				creatSt.close();
				sqliteStatement = sqliteConnection.prepareStatement(sql);
				final int batchSize = DecomposerUtils.NO_OF_RECORDS;
				while (resultSet.next()) {

					

					for (int i = 1; i <= columnsNumber; i++) {

						sqliteStatement.setObject(i, resultSet.getObject(i));
					}

					sqliteStatement.addBatch();

					if (++count % batchSize == 0) {
						sqliteStatement.executeBatch();
					}
				}
				sqliteStatement.executeBatch(); // insert remaining records
				sqliteStatement.close();
				resultSet.close();
			}

			sqliteConnection.commit();
			
			sqliteConnection.close();
			
			statement.close();
			connection.close();

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Could not import data from endpoint\n" + e.getMessage() +
					" from query:"+qString);
			return;
		}

		log.debug(count + " rows were imported in "
				+ (System.currentTimeMillis() - start) + "msec from query: "+ qString);
		StringBuilder madis = new StringBuilder();
		madis.append("sqlite '");
		madis.append(this.dbPath);
		madis.append("import/");
		madis.append(s.getTemporaryTableName());
		madis.append(".db' ");
		s.setMadisFunctionString(madis.toString());
		s.setSelectAll(true);

		s.removeInfo();
		s.getInputTables()
				.add(new Table(s.getTemporaryTableName(), s
						.getTemporaryTableName()));
		if(this.addToRegistry){
			madgik.exareme.common.schema.Table table=new madgik.exareme.common.schema.Table(s.getTemporaryTableName());
			Registry reg = Registry.getInstance(this.dbPath);
			table.setSqlDefinition(createTableSQL.toString());
			//TableInfo ti=new TableInfo(s.getTemporaryTableName());
			//ti.setSqlDefinition(createTableSQL.toString());
			table.setTemp(false);
			/*if use cache
			table.setSqlQuery(s.toDistSQL());
			table.setSize(4096);
			table.setHashID(s.getHashId().asBytes());*/
			PhysicalTable pt=new PhysicalTable(table);
			Partition partition0 = new Partition(s.getTemporaryTableName(), 0);
            partition0.addLocation(ArtRegistryLocator.getLocalRmiRegistryEntityName().getIP());
            //partition0.addPartitionColumn("");
			pt.addPartition(partition0);
			reg.addPhysicalTable(pt);
		}

	}
	
	public void setAddToRegisrty(boolean b){
		this.addToRegistry=b;
	}

}
