/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.logger;

/**
 * @author heraldkllapi
 */
public class ElasticTreeLoggerUtils {

    public static LogEvents.Event parseEvent(String event) {
        String[] parts = event.split(ElasticTreeLogger.SEP);
        long time = Long.parseLong(parts[0]);
        String type = parts[1];
        if (type.equalsIgnoreCase(ElasticTreeLogger.Code.INIT)) {
            return new LogEvents.Init(time, Long.parseLong(parts[2]));
        } else if (type.equalsIgnoreCase(ElasticTreeLogger.Code.QUERIES)) {
            return new LogEvents.Queries(time, Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
        } else if (type.equalsIgnoreCase(ElasticTreeLogger.Code.QSTART)) {
            return new LogEvents.QueryStart(time, Long.parseLong(parts[2]));
        } else if (type.equalsIgnoreCase(ElasticTreeLogger.Code.QSUCCESS)) {
            return new LogEvents.QuerySuccess(time, Long.parseLong(parts[2]),
                    Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
        } else if (type.equalsIgnoreCase(ElasticTreeLogger.Code.QERROR)) {
            return new LogEvents.QueryError(time, Long.parseLong(parts[2]));
        } else if (type.equalsIgnoreCase(ElasticTreeLogger.Code.ELASTIC_TREE)) {
            return new LogEvents.ElasticTreeLevel(time, Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]), Long.parseLong(parts[4]), Double.parseDouble(parts[5]),
                    0.0, Double.parseDouble(parts[7]), 0.0);
        } else if (type.equalsIgnoreCase(ElasticTreeLogger.Code.MONEY)) {
            return new LogEvents.Money(time, Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]), Double.parseDouble(parts[4]),
                    Double.parseDouble(parts[5]), Double.parseDouble(parts[6]));
        } else {
            System.out.println("Type not known: '" + type + "' Ignoring ... ");
        }
        return new LogEvents.Unknown(time, parts);
    }
}
