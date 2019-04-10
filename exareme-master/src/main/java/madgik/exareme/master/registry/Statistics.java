package madgik.exareme.master.registry;

import madgik.exareme.common.art.*;
import madgik.exareme.common.optimizer.OperatorType;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.*;

/**
 * Created by vagos on 3/4/15.
 */
public class Statistics {

    private static final Logger log = Logger.getLogger(Statistics.class);
    private static Connection conn = null;
    private static String database = null;

    public Statistics(String path) {

        new File(path).mkdirs();
        database = path + "/statistics.db";

        try {
            Class.forName("org.sqlite.JDBC");
            // create a database connection
            conn = DriverManager.getConnection("jdbc:sqlite:" + database);
            Statement stmt = conn.createStatement();

            //Statistics Tables //TODO: plan session id needed?
            //Query Statistics
            stmt.execute("create table if not exists query_statistics(" + "query_id integer, "
                    + "plan_session_id integer, " + "start_time_ms integer, " + "end_time_ms integer, "
                    + "primary key(query_id));");


            //Container Statistics //TODO: remove plan session id?
            stmt.execute(
                    "create table if not exists container_statistics(" + "container_name text, "
                            + "container_session_id integer, " + "plan_session_id integer, "
                            + "query_id integer, " + "primary key(query_id, container_name)"
                            + "foreign key(query_id) references query_statistics(query_id));");

            //Operator Statistics
            stmt.execute("create table if not exists operator_statistics(" + "operator_name text, "
                    + "operator_category text, " + "operator_type_id integer, "
                    + "start_time_ms integer, " + "end_time_ms integer, " + "exception text, "
                    + "user_time_ms integer, " + "user_cpu_time_ms integer, "
                    + "system_time_ms integer, " + "system_cpu_time integer, "
                    + "block_time_ms integer, " + "total_time_ms integer, " + "total_cpu_time integer, "
                    + "exit_code integer, " + "exit_message text, " + "query_id integer, "
                    + "primary key(query_id, operator_name), "
                    + "foreign key(operator_type_id) references operator_types(operator_type_id), "
                    + "foreign key(query_id) references query_statistics(query_id));");

            //Operator Types
            stmt.execute("create table if not exists operator_types(" + "operator_type_id integer, "
                    + "operator_type text, " + "primary key(operator_type_id));");
            //Initialize Operator Types
            stmt.execute("INSERT INTO operator_types(operator_type_id, " + "operator_type) "
                    + "SELECT 0, \'processing\' "
                    + "WHERE NOT EXISTS (SELECT * FROM operator_types WHERE operator_type_id=0);");
            stmt.execute("INSERT INTO operator_types(operator_type_id, " + "operator_type) "
                    + "SELECT 1, \'dataMaterialization\' "
                    + "WHERE NOT EXISTS (SELECT * FROM operator_types WHERE operator_type_id=1);");
            stmt.execute("INSERT INTO operator_types(operator_type_id, " + "operator_type) "
                    + "SELECT 2, \'dataTransfer\' "
                    + "WHERE NOT EXISTS (SELECT * FROM operator_types WHERE operator_type_id=2);");

            //Adaptor Statistics
            stmt.execute("create table if not exists adaptor_statistics(" + "adaptor_name text, "
                    + "from_operator_name text, " + "to_operator_name text, " + "bytes integer,"
                    + "bytes_per_sec real, " + "remote integer, " + "query_id integer, "
                    + "foreign key(query_id, from_operator_name) references operator_statistics(query_id, operator_name), "
                    + "foreign key(query_id, to_operator_name) references operator_statistics(query_id, operator_name));");


            //Buffer Statistics
            stmt.execute("create table if not exists buffer_statistics(" + "buffer_name text, "
                    + "data_write integer, " + "data_read integer, " + "query_id integer, "
                    + "foreign key(query_id) references query_statistics(query_id));");

            stmt.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    //    public Registry.Schema getSchema() {
    //        return new Schema(this.database, getPhysicalTables());
    //    }

    public String getDatabase() {
        return database;
    }

    //    public static class Schema implements Serializable {
    //        private static final long serialVersionUID = 1L;
    //
    //        private String database = null;
    //        private HashMap<String, PhysicalTable> tables = null;
    //
    //        // private HashMap<String, Index> indexes = null;
    //        // private String mappings = null;
    //
    //        public Schema(String database, Collection<PhysicalTable> physicalTables) {
    //            this.database = database;
    //            this.tables = new HashMap<String, PhysicalTable>();
    //
    //            for (PhysicalTable table : physicalTables) {
    //                this.tables.put(table.getName(), table);
    //            }
    //        }
    //
    //        public String getDatabase() {
    //            return database;
    //        }
    //
    //        public PhysicalTable getPhysicalTable(String name) {
    //            return tables.get(name);
    //        }
    //
    //        public Collection<PhysicalTable> getPhysicalTables() {
    //            return tables.values();
    //        }
    //    }

    public void addQueryStatistics(long query_id, PlanSessionStatistics planStatistics) {

        //TODO: add query statistics
        log.info("PlanStatistics Insert Into Registry, query_id: " + query_id);

        try {
            PreparedStatement insertQueryStatisticsStatement =
                    conn.prepareStatement("INSERT INTO query_statistics(" +
                            "query_id, plan_session_id, start_time_ms, end_time_ms ) " +
                            "VALUES(?, ?, ?, ?)");

            PreparedStatement insertContainerStatisticsStatement =
                    conn.prepareStatement("INSERT INTO container_statistics(" +
                            "container_name, container_session_id , query_id ) " +
                            "VALUES(?, ?, ?)");

            PreparedStatement insertOperatorsStatisticsStatement = conn.prepareStatement(
                    "INSERT INTO operator_statistics("
                            + "operator_name, operator_category, operator_type_id, start_time_ms, end_time_ms, "
                            + "exception, user_time_ms, user_cpu_time_ms, system_time_ms, system_cpu_time, block_time_ms, "
                            + "total_time_ms, total_cpu_time, exit_code, exit_message, query_id ) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            PreparedStatement insertBufferStatisticsStatement =
                    conn.prepareStatement("INSERT INTO buffer_statistics(" +
                            "buffer_name, data_write, data_read, query_id ) " +
                            "VALUES(?, ?, ?, ?)");

            PreparedStatement insertAdaptorStatisticsStatement =
                    conn.prepareStatement("INSERT INTO adaptor_statistics(" +
                            "adaptor_name, from_operator_name, to_operator_name, bytes, bytes_per_sec, remote, query_id ) "
                            +
                            "VALUES(?, ?, ?, ?, ?, ?, ?)");


            insertQueryStatisticsStatement.setLong(1, query_id);
            insertQueryStatisticsStatement.setLong(2, -1); //TODO create getter for plan session id
            insertQueryStatisticsStatement.setLong(3, planStatistics.startTime());
            insertQueryStatisticsStatement.setLong(4, planStatistics.endTime());
            insertQueryStatisticsStatement.execute();

            for (ContainerSessionStatistics containerStats : planStatistics.containerStats) {
                //Add container statistics
                insertContainerStatisticsStatement.setString(1, containerStats.containerName);
                insertContainerStatisticsStatement
                        .setLong(2, containerStats.getSessionID().getLongId());
                insertContainerStatisticsStatement.setLong(3, query_id);
                insertContainerStatisticsStatement.addBatch();

                for (ConcreteOperatorStatistics operatorStatistics : containerStats.operators) {
                    //Add operator statistics
                    insertOperatorsStatisticsStatement
                            .setString(1, operatorStatistics.getOperatorName());
                    insertOperatorsStatisticsStatement
                            .setString(2, operatorStatistics.getOperatorCategory());
                    insertOperatorsStatisticsStatement
                            .setInt(3, categoryToInt(operatorStatistics.getOperatorType()));
                    insertOperatorsStatisticsStatement
                            .setLong(4, operatorStatistics.getStartTime_ms());
                    insertOperatorsStatisticsStatement
                            .setLong(5, operatorStatistics.getEndTime_ms());
                    insertOperatorsStatisticsStatement.setString(6, "null"); //TODO fix exception
                    insertOperatorsStatisticsStatement
                            .setLong(7, operatorStatistics.getUserTime_ms());
                    insertOperatorsStatisticsStatement
                            .setLong(8, operatorStatistics.getUserCpuTime_ms());
                    insertOperatorsStatisticsStatement
                            .setLong(9, operatorStatistics.getSystemTime_ms());
                    insertOperatorsStatisticsStatement
                            .setLong(10, operatorStatistics.getSystemCpuTime_ms());
                    insertOperatorsStatisticsStatement
                            .setLong(11, operatorStatistics.getBlockTime_ms());
                    insertOperatorsStatisticsStatement
                            .setLong(12, operatorStatistics.getTotalTime_ms());
                    insertOperatorsStatisticsStatement
                            .setLong(13, operatorStatistics.getTotalCpuTime_ms());
                    insertOperatorsStatisticsStatement.setInt(14, operatorStatistics.getExitCode());
                    insertOperatorsStatisticsStatement.setString(15,
                            operatorStatistics.getExitMessage().toString()); //TODO: check this
                    insertOperatorsStatisticsStatement.setLong(16, query_id);

                    insertOperatorsStatisticsStatement.addBatch();
                }
                insertOperatorsStatisticsStatement.executeBatch();

                for (BufferStatistics bufferStatistics : containerStats.buffer) {
                    //Add buffer statistics
                    insertBufferStatisticsStatement.setString(1, bufferStatistics.getBufferName());
                    insertBufferStatisticsStatement.setLong(2, bufferStatistics.getDataWrite());
                    insertBufferStatisticsStatement.setLong(3, bufferStatistics.getDataRead());
                    insertBufferStatisticsStatement.setLong(4, query_id);

                    insertBufferStatisticsStatement.addBatch();
                }
                insertBufferStatisticsStatement.executeBatch();

                for (AdaptorStatistics adaptorStatistics : containerStats.adaptors) {
                    //Add adaptor statistics
                    insertAdaptorStatisticsStatement.setString(1, adaptorStatistics.getName());
                    insertAdaptorStatisticsStatement.setString(2, adaptorStatistics.getFrom());
                    insertAdaptorStatisticsStatement.setString(3, adaptorStatistics.getTo());
                    insertAdaptorStatisticsStatement.setLong(4, adaptorStatistics.getBytes());
                    insertAdaptorStatisticsStatement
                            .setDouble(5, adaptorStatistics.getBytesPerSecond());
                    insertAdaptorStatisticsStatement.setLong(6, query_id);

                    insertAdaptorStatisticsStatement.addBatch();
                }
                insertAdaptorStatisticsStatement.executeBatch();


            }
            insertContainerStatisticsStatement.executeBatch();

            //Close all statements
            insertAdaptorStatisticsStatement.close();
            insertBufferStatisticsStatement.close();
            insertOperatorsStatisticsStatement.close();
            insertQueryStatisticsStatement.close();
            insertContainerStatisticsStatement.close();
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private int categoryToInt(OperatorType type) {
        switch (type) {
            case processing:
                return 0;
            case dataMaterialization:
                return 1;
            case dataTransfer:
                return 2;
        }
        throw new IllegalArgumentException("Operator Category not found: " + type);
    }

}
