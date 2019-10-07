/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.registry;

import com.google.gson.Gson;
import madgik.exareme.common.schema.Index;
import madgik.exareme.common.schema.Partition;
import madgik.exareme.common.schema.PhysicalTable;
import madgik.exareme.common.schema.Table;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author Christoforos Svingos
 */
public class Registry {

    private static final Logger log = Logger.getLogger(Registry.class);
    private static List<Registry> registryObjects = new ArrayList<>();
    private Connection regConn = null;
    private String database = null;

    private Registry(String path) {
        new File(path).mkdirs();
        database = path + "/registry.db";

        try {
            Class.forName("org.sqlite.JDBC");
            // create a database connection
            regConn = DriverManager.getConnection("jdbc:sqlite:" + database);
            Statement stmt = regConn.createStatement();

            stmt.execute(
                    "create table if not exists sql(" + "table_name text, " + "sql_definition text, "
                            + "primary key(table_name));");

            stmt.execute("create table if not exists partition(" + "table_name text, "
                    + "partition_number integer, " + "location text, " + "partition_column text, "
                    + "primary key(table_name, partition_number, location, partition_column), "
                    + "foreign key(table_name) references sql(table_name));");

            stmt.execute("create table if not exists table_index(" + "index_name text, "
                    + "table_name text, " + "column_name text, " + "partition_number integer, "
                    + "primary key(index_name, partition_number), "
                    + "foreign key(table_name) references sql(table_name));");

            stmt.close();
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    public synchronized static Registry getInstance(String path) {
        String db = path + "/registry.db";
        for (Registry registry : registryObjects) {
            if (registry.getDatabase().equals(db))
                return registry;
        }

        Registry registry = new Registry(path);
        registryObjects.add(registry);

        return registry;
    }

    public Registry.Schema getSchema() {
        return new Schema(database, getPhysicalTables());
    }

    public String getDatabase() {
        return database;
    }

    public List<String> getTableDefinitions() {
        List<String> sqlSchemaTables = new ArrayList<String>();

        try (Statement statement = regConn.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM sql;");
            while (rs.next()) {
                sqlSchemaTables.add(rs.getString("sql_definition") + ";\n");
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return sqlSchemaTables;
    }

    public void addPhysicalTable(PhysicalTable table) {
        Gson gson = new Gson();
        log.debug("PhysicalTable Insert Into Registry: " + gson.toJson(table));

        try (
                PreparedStatement insertSqlStatemenet = regConn
                        .prepareStatement("INSERT INTO sql(table_name, sql_definition) VALUES(?, ?)");


                PreparedStatement insertPartitionStatemenet = regConn.prepareStatement(
                        "INSERT INTO partition(table_name, " + "location, " + "partition_column, "
                                + "partition_number) " + "VALUES(?, ?, ?, ?)");

                PreparedStatement insertIndexStatemenet = regConn.prepareStatement(
                        "INSERT INTO table_index(index_name, " + "table_name, " + "column_name, "
                                + "partition_number) " + "VALUES(?, ?, ?, ?)")
        ) {
            insertSqlStatemenet.setString(1, table.getTable().getName());
            insertSqlStatemenet.setString(2, table.getTable().getSqlDefinition());
            insertSqlStatemenet.execute();

            for (Partition partition : table.getPartitions()) {
                for (int i = 0; i < partition.getLocations().size(); ++i) {
                    if (partition.getPartitionColumns().isEmpty()) {
                        insertPartitionStatemenet.setString(1, table.getTable().getName());
                        insertPartitionStatemenet.setString(2, partition.getLocations().get(i));
                        insertPartitionStatemenet.setString(3, null);
                        insertPartitionStatemenet.setInt(4, partition.getpNum());
                        insertPartitionStatemenet.addBatch();
                    } else {
                        for (int j = 0; j < partition.getPartitionColumns().size(); ++j) {
                            insertPartitionStatemenet.setString(1, table.getTable().getName());
                            insertPartitionStatemenet.setString(2, partition.getLocations().get(i));
                            insertPartitionStatemenet
                                    .setString(3, partition.getPartitionColumns().get(j));
                            insertPartitionStatemenet.setInt(4, partition.getpNum());
                            insertPartitionStatemenet.addBatch();
                        }

                    }
                }
            }
            insertPartitionStatemenet.executeBatch();

            for (Index index : table.getIndexes()) {
                for (int i = index.getParitions().nextSetBit(0);
                     i >= 0; i = index.getParitions().nextSetBit(i + 1)) {
                    insertIndexStatemenet.setString(1, index.getIndexName());
                    insertIndexStatemenet.setString(2, index.getTableName());
                    insertIndexStatemenet.setString(3, index.getColumnName());
                    insertIndexStatemenet.setInt(4, i);
                    insertIndexStatemenet.addBatch();
                }
            }
            insertIndexStatemenet.executeBatch();

            insertSqlStatemenet.close();
            insertPartitionStatemenet.close();
            insertIndexStatemenet.close();
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public PhysicalTable removePhysicalTable(String name) {
        PhysicalTable returnPhysicalTable = getPhysicalTable(name);
        try (
                Statement deleteIndexStatement = regConn.createStatement();
                Statement deletePartitionStatement = regConn.createStatement();
                Statement deleteSqlStatement = regConn.createStatement()
        ) {
            deleteIndexStatement
                    .execute("DELETE FROM table_index WHERE table_name = '" + name + "'");

            deletePartitionStatement
                    .execute("DELETE FROM partition WHERE table_name = '" + name + "'");

            deleteSqlStatement.execute("DELETE FROM sql WHERE table_name = '" + name + "'");

        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return returnPhysicalTable;
    }

    public boolean containsPhysicalTable(String name) {
        boolean ret = false;

        try (Statement statement = regConn.createStatement()) {
            ResultSet rs = statement.executeQuery(
                    "SELECT table_name " + "FROM sql " + "WHERE table_name = '" + name + "';");

            if (rs.next())
                ret = true;

        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return ret;
    }

    public PhysicalTable getPhysicalTable(String name) {
        PhysicalTable returnPhysicalTable = null;

        try (
                Statement getSqlStatement = regConn.createStatement();
                Statement getIndexStatement = regConn.createStatement();
                Statement getPartitionStatement = regConn.createStatement()
        ) {

            ResultSet rs = getSqlStatement
                    .executeQuery("SELECT * FROM sql " + "WHERE table_name = '" + name + "'");

            if (rs.next()) {
                Table table = new Table(rs.getString("table_name"));
                table.setSqlDefinition(rs.getString("sql_definition"));

                returnPhysicalTable = new PhysicalTable(table);
            }
            rs.close();

            rs = getPartitionStatement.executeQuery(
                    "SELECT partition_number, " + "partition_column, location " + "FROM partition "
                            + "WHERE table_name = '" + name + "' "
                            + "GROUP BY partition_number, partition_column, location;");

            int pNum = -1;
            Partition part = null;
            while (rs.next()) {
                int tempPNum = rs.getInt("partition_number");
                if (pNum != tempPNum) {
                    pNum = tempPNum;
                    if (part != null)
                        returnPhysicalTable.addPartition(part);

                    part = new Partition(returnPhysicalTable.getTable().getName(), pNum);
                }

                part.addLocation(rs.getString("location"));
                part.addPartitionColumn(rs.getString("partition_column"));
            }
            if (part != null)
                returnPhysicalTable.addPartition(part);
            rs.close();

            rs = getIndexStatement.executeQuery(
                    "SELECT DISTINCT index_name " + "FROM table_index " + "WHERE table_name = '" + name
                            + "';");

            while (rs.next()) {
                // TODO: one query
                Index index = getIndex(rs.getString("index_name"));
                returnPhysicalTable.addIndex(index);
            }

        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return returnPhysicalTable;
    }

    public Collection<PhysicalTable> getPhysicalTables() {
        List<PhysicalTable> list = new ArrayList<PhysicalTable>();

        try (Statement statement = regConn.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT table_name FROM sql;");

            while (rs.next()) {
                list.add(getPhysicalTable(rs.getString("table_name")));
            }

        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return list;
    }

    public void addIndex(Index idx) {
        try (
                PreparedStatement insertIndexStatemenet = regConn.prepareStatement(
                        "INSERT INTO table_index(index_name, " + "table_name, " + "column_name, "
                                + "partition_number) " + "VALUES(?, ?, ?, ?)")
        ) {
            for (int i = idx.getParitions().nextSetBit(0);
                 i >= 0; i = idx.getParitions().nextSetBit(i + 1)) {
                insertIndexStatemenet.setString(1, idx.getIndexName());
                insertIndexStatemenet.setString(2, idx.getTableName());
                insertIndexStatemenet.setString(3, idx.getColumnName());
                insertIndexStatemenet.setInt(4, i);
                insertIndexStatemenet.addBatch();
            }

            insertIndexStatemenet.executeBatch();

        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private boolean containesIndex(String name) {
        boolean ret = false;

        try (Statement statement = regConn.createStatement()) {
            ResultSet rs = statement.executeQuery(
                    "SELECT table_name " + "FROM table_index " + "WHERE index_name = '" + name + "';");

            if (rs.next())
                ret = true;
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return ret;
    }

    private Index getIndex(String name) {
        Index index = null;
        try (Statement statement = regConn.createStatement()) {
            ResultSet rs = statement.executeQuery(
                    "SELECT * FROM table_index " + "WHERE index_name = '" + name + "' "
                            + "GROUP BY table_name, " + "column_name, " + "index_name, "
                            + "partition_number;");

            boolean first = true;
            while (rs.next()) {
                if (first) {
                    index = new Index(rs.getString("table_name"), rs.getString("column_name"),
                            rs.getString("index_name"));
                    first = false;
                }

                index.addPartition(rs.getInt("partition_number"));
            }

        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return index;
    }

    public String getMappings() {
        // throw new UnsupportedOperationException("Not supported yet.");
        return null;
    }

    public void setMappings(String mappings) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Object[]> getMetadata() {
        List<Object[]> metatada = new ArrayList<>();
        try (Statement statement = regConn.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT table_name, sql_definition FROM sql;");

            while (rs.next()) {
                Object[] tuple = new Object[2];
                tuple[0] = rs.getString("table_name");
                tuple[1] = rs.getString("sql_definition");
                metatada.add(tuple);
            }

            rs.close();
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }

        return metatada;
    }


    public static class Schema implements Serializable {
        private static final long serialVersionUID = 1L;

        private String database = null;
        private HashMap<String, PhysicalTable> tables = null;

        // private HashMap<String, Index> indexes = null;
        // private String mappings = null;

        public Schema(String database, Collection<PhysicalTable> physicalTables) {
            this.database = database;
            this.tables = new HashMap<String, PhysicalTable>();

            for (PhysicalTable table : physicalTables) {
                this.tables.put(table.getName(), table);
            }
        }

        public String getDatabase() {
            return database;
        }

        public PhysicalTable getPhysicalTable(String name) {
            return tables.get(name);
        }

        public Collection<PhysicalTable> getPhysicalTables() {
            return tables.values();
        }
    }

}

