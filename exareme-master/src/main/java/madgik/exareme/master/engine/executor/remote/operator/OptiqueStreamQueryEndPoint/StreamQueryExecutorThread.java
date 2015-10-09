package madgik.exareme.master.engine.executor.remote.operator.OptiqueStreamQueryEndPoint;

import com.google.gson.Gson;
import madgik.exareme.utils.association.SimplePair;
import org.apache.log4j.Logger;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class StreamQueryExecutorThread extends Thread {
    public enum State {LOADING, RUN, DIE, DESTROY}


    ;

    private ArrayDeque<SimplePair<Long, Deque<Object[]>>> buffers = null;
    private int bufferTimeInSec;
    private boolean stopGenerator = false;
    private Gson gson = new Gson();
    Connection conn;
    Statement stmt;

    private String errmsg;
    private State state = State.LOADING;
    private String sqlQuery;
    private String registerDate;
    private String lastProducedTupleDate;
    private SimpleDateFormat dateParser;

    private int timeColumn;
    ArrayList<String[]> schema;

    private static String madisPath =
        "/home/xrs/Code/LALA/exareme/exareme-tools/madis/src/mterm.py";
    //    private static String madisPath =  PropertyFactory.getRestServerProperty().
    //            getString("madis.path");

    private static final Logger log = Logger.getLogger(StreamQueryExecutorThread.class);

    public StreamQueryExecutorThread(String sqlQuery, int bufferTimeInSec) {
        dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        dateParser.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.sqlQuery = sqlQuery;

        this.buffers = new ArrayDeque<>(bufferTimeInSec);
        this.bufferTimeInSec = bufferTimeInSec;
        registerDate = dateParser.format(new Date());
    }

    private ResultSet executeQuery(String sqlQuery)
        throws ClassNotFoundException, SQLException, ParseException {
        Class.forName("madgik.exareme.utils.embedded.AdpEmbeddedDriver");
        Properties prop = new Properties();
        prop.setProperty("MADIS_PATH", madisPath);
        conn = DriverManager.getConnection("jdbc:adp:", prop);
        stmt = conn.createStatement();

        this.sqlQuery = sqlQuery;
        System.out.println(ParseUtils.queryRewriting(sqlQuery));
        ResultSet rs = stmt.executeQuery(ParseUtils.queryRewriting(sqlQuery));

        schema = new ArrayList<String[]>();

        int columnsNumber = rs.getMetaData().getColumnCount();
        for (int i = 0; i < columnsNumber; ++i) {
            String column = rs.getMetaData().getColumnName(i + 1);
            if (column.equals("timestamp") || column.equals("t")) {
                timeColumn = i + 1;
            }

            String[] type = new String[2];
            type[0] = rs.getMetaData().getColumnName(i + 1);
            type[1] = rs.getMetaData().getColumnTypeName(i + 1);
            schema.add(type);
        }

        if (timeColumn == 0) {
            stopGenerator = true;
            throw new ParseException("No time column in Query: " + sqlQuery, timeColumn);
        }

        return rs;
    }

    public void stopGenerator() {
        stopGenerator = true;
        if (this.state == State.LOADING) {
            close("The stream data production stops!");
        }
    }

    public void destroyGenerator() {
        stopGenerator = true;
        if (this.state == State.LOADING) {
            close("The stream data production stops!");
        }

        state = State.DESTROY;
    }

    private void close(String errmsg) {
        this.errmsg = errmsg;
        this.state = State.DIE;
        this.buffers = null;
        try {
            this.stmt.cancel();
            this.conn.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override public void run() {
        log.debug("Loading Stream Data Query ...");
        state = State.LOADING;
        errmsg = "Query loading please wait";
        ResultSet rs;
        try {
            rs = executeQuery(this.sqlQuery);

            state = State.RUN;
            log.debug("Starting Producing Stream Data Thread ...");
            Object[] line = null;
            String timestamp = null;

            while (rs.next() && !stopGenerator) {
                synchronized (this) {
                    line = new Object[rs.getMetaData().getColumnCount()];

                    for (int c = 0; c < rs.getMetaData().getColumnCount(); ++c) {
                        try {
                            line[c] = rs.getObject(c + 1);
                            if (((int) (double) line[c]) == (double) line[c]) {
                                line[c] = (int) (double) line[c];
                            }
                        } catch (Exception e) {
                            //                            line[c] = rs.getObject(c + 1);
                        }
                    }

                    timestamp = (String) rs.getObject(timeColumn);

                    long dateInMilli;
                    try {
                        dateInMilli = dateParser.parse(timestamp).getTime();
                    } catch (ParseException ex) {
                        close(ex.getMessage());
                        log.error(ex.getMessage(), ex);
                        return;
                    }

                    long posixDate = dateInMilli / 1000;

                    if (buffers.isEmpty())
                        buffers.add(new SimplePair<Long, Deque<Object[]>>(posixDate,
                            new ArrayDeque<Object[]>()));

                    for (int i = 0; i < (posixDate - buffers.getFirst().first); ++i) {
                        long time = buffers.getFirst().first + i + 1;
                        buffers.addFirst(new SimplePair<Long, Deque<Object[]>>(time,
                            new ArrayDeque<Object[]>()));
                    }

                    buffers.getFirst().second.addFirst(line);

                    while (buffers.size() >= bufferTimeInSec) {
                        buffers.removeLast().second.clear();
                    }

                }

                line = null;
                timestamp = null;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            close(ex.getMessage());
            return;
        }

        close("The stream data production stops!");
        log.info("The stream data production stops ...");
    }

    public synchronized SimplePair<List<String[]>, ArrayDeque<Object[]>> getBuffer(
        long startUnixTime, long endUnixtime) throws IllegalThreadStateException {
        log.debug("Starttimestamp: " + startUnixTime + ", EndTimestamp: " + endUnixtime);
        if (state != State.RUN) {
            throw new IllegalThreadStateException(errmsg);
        }

        ArrayDeque<Object[]> retBuffer = new ArrayDeque<Object[]>();

        if ((endUnixtime - startUnixTime) < 0)
            return new SimplePair(schema, retBuffer);

        long numberOfSeconds = buffers.getFirst().first - startUnixTime + 1;
        log.debug("Available Unix Time: " + buffers.getFirst().first);

        int loopCounter = 0;
        if (numberOfSeconds < 0)
            numberOfSeconds = 0;


        for (SimplePair<Long, Deque<Object[]>> sec : buffers) {
            if (loopCounter == numberOfSeconds)
                break;

            ++loopCounter;
            if (sec.first >= endUnixtime)
                continue;

            for (Object[] tuple : sec.second) {
                retBuffer.addFirst(tuple);
            }
        }

        return new SimplePair(schema, retBuffer);
    }

    public State state() {
        return this.state;
    }

    public String query() {
        return this.sqlQuery;
    }

    public String registerDate() {
        return registerDate;
    }

}
