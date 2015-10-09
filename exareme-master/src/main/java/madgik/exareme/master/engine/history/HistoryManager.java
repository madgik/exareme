package madgik.exareme.master.engine.history;

import org.apache.log4j.Logger;

import java.sql.*;

/**
 * History Manager
 * + thread-safe
 * + transactional
 *
 * @author alex
 * @author vagnik
 */
public class HistoryManager {
    private static final Logger log = Logger.getLogger(HistoryManager.class);
    private static final HistoryManager instance = new HistoryManager();
    private Connection connection;

    protected HistoryManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + "/tmp/.exareme-history.db");
            Statement statement = connection.createStatement();
            // Notice global keys TODO(alex) salt table keys per database.

            // databases
            statement.execute(
                "create table if not exists database( " + "d_dbname text, " + "d_dbpath text,"
                    + "d_dbowner text," + "d_dbgroup text," + "d_dbtime_created text,"
                    + "primary key(d_dbname)" + ");");
            // queries
            statement.execute(
                "create table if not exists query( " + "q_dbname text," + "q_querykey text, "
                    + "q_queryscript text, " + "q_querysessionkey integer, "
                    + "q_querytime_started integer, " + "q_querytime_ended integer, "
                    + "primary key(q_querykey),"
                    + "foreign key(q_dbname) references database(d_dbname)" + ");");
            log.info("Databases table created");
            // plans
            statement.execute(
                "create table if not exists plan( " + "p_querykey text," + "p_plankey text, "
                    + "p_planscript text, " + "p_plansessionkey integer, "
                    + "p_plantime_started integer, " + "p_plantime_ended integer, "
                    + "primary key(p_plankey),"
                    + "foreign key(p_querykey) references query(q_querykey)" + ");");
            log.info("Queries table created");
            // containers
            statement.execute("create table if not exists container(" + "c_plansessionkey integer,"
                + "c_containername text," + "c_containersessionkey integer,"
                + "primary key(c_containername, c_containersessionkey),"
                + "foreign key(c_plansessionkey) references plan(p_plansessionkey)" + ");");
            log.info("Containers table created");
            // operators
            statement.execute(
                "create table if not exists operator(" + "o_containersessionkey integer,"
                    + "o_operatorname text," + "o_operatorcategory text," + "o_operator_type text,"
                    + "o_operatortime_started integer," + "o_operatortime_ended integer,"
                    + "o_operatorexception text," + "o_exitcode integer,"
                    + "primary key(o_operatorname, o_containersessionkey),"
                    + "foreign key(o_containersessionkey) references container(c_containersessionkey)"
                    + ");");
            log.info("Operators table created");
            // adaptors
            statement.execute("create table if not exists adaptor(" + "o_operatorname text,"
                + "a_containersessionkey integer, " + "a_adaptorname text, "
                + "a_adaptorfrom text, " + "a_adaptorto text, " + "a_adaptorbytes integer, "
                + "a adaptorremote integer, " + "foreign key(a_operatorname)"
                + "references operator(o_operatorname), " + "foreign key(a_containersessionkey) "
                + "references operator(o_containersessionkey), " + "foreign key(a_adaptorfrom)"
                + "references operator(o_operatorname), " + "foreign key(a_adaptorto) "
                + "references operator(o_operatorname) " + ");");
            log.info("Adaptors table created");
            // buffers
            statement.execute("create table if not exists buffer( " + "b_operatorname text, "
                + "b_containersessionkey text, " + "b_buffername text, " + "b_bufferwrite integer, "
                + "b_bufferread integer, " + "foreign key(b_operatorname)"
                + "references operator(o_operatorname), " + "foreign key(b_containersessionkey) "
                + "references operator(o_containersessionkey)" + ");");
            log.info("Buffers table created");

            statement.close();
        } catch (ClassNotFoundException e) {
            log.error(e);
        } catch (SQLException e) {
            log.error(e);
        } finally {
        }

    }

    public static final HistoryManager getInstance() {
        return instance;
    }

    public static void main(String[] args) {

        HistoryManager manager = HistoryManager.getInstance();
        //        manager.insert_database(
        //            "test-" + String.valueOf(System.currentTimeMillis()),
        //            "/tmp/test", "","","");
        //        manager.insert_query(
        //            "test",
        //            String.valueOf(System.currentTimeMillis()),
        //            "script",
        //            (int)System.currentTimeMillis(),
        //            (int)System.currentTimeMillis(),
        //            (int)System.currentTimeMillis()
        //        );

    }

    public void insert_database(String dbname, String dbpath, String owner, String group,
        String timestamp) {
        try {
            PreparedStatement prepareStatement = connection.prepareStatement(
                "insert into database(" + "d_dbname, " + "d_dbpath, " + "d_dbowner, "
                    + "d_dbgroup, " + "d_dbtime_created" + ") values(?,?,?,?,?);");
            prepareStatement.setString(1, dbname);
            prepareStatement.setString(2, dbpath);
            prepareStatement.setString(3, owner);
            prepareStatement.setString(4, group);
            prepareStatement.setString(5, timestamp);
            prepareStatement.execute();
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public void insert_query(String dbname, String id, String script, int sessionID, int startTime,
        int endTime) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                "insert into query(" + "q_dbname," + "q_querykey, " + "q_queryscript, "
                    + "q_querysessionkey, " + "q_querytime_started, " + "q_querytime_ended "
                    + ") values(?,?,?,?,?,?);");
            preparedStatement.setString(1, dbname);
            preparedStatement.setString(2, id);
            preparedStatement.setString(3, script);
            preparedStatement.setInt(4, sessionID);
            preparedStatement.setInt(5, startTime);
            preparedStatement.setInt(6, endTime);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert_plan() {

    }

}
