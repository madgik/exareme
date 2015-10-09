//package madgik.exareme.master.engine.executor.remote.operator.control;
//
//import madgik.exareme.master.client.AdpDBClient;
//import madgik.exareme.master.client.AdpDBClientFactory;
//import madgik.exareme.master.client.AdpDBClientProperties;
//import madgik.exareme.master.client.AdpDBClientQueryStatus;
//import madgik.exareme.master.engine.AdpDBManager;
//import madgik.exareme.master.engine.AdpDBManagerLocator;
//import madgik.exareme.worker.art.concreteOperator.AbstractNiNo;
//import madgik.exareme.worker.art.parameter.Parameter;
//import madgik.exareme.worker.art.parameter.Parameters;
//import org.apache.commons.io.IOUtils;
//import org.apache.log4j.Logger;
//
//import java.io.InputStream;
//import java.nio.charset.Charset;
//
///**
// */
//public class DoWhile extends AbstractNiNo {
//
//    private static Logger log = Logger.getLogger(DoWhile.class);
//
//    @Override
//    public void run() throws Exception {
//
//        log.trace("Parsing parameters ...");
//        String conditionTable = super.getParameterManager().getParameter("conditionTable").get(0).getValue();
//        String queryString = super.getParameterManager().getQueryString();
//        AdpDBClientProperties clientProperties = createDBClientProperties(super.getParameterManager().getParameters());
//        log.info("--+" + queryString);
//        log.trace("Creating new db client ...");
//        AdpDBManager dbManager = AdpDBManagerLocator.getDBManager();
//        AdpDBClient dbClient = AdpDBClientFactory.createDBClient(dbManager, clientProperties);
//        int count = 0;
//        boolean hasToContinue = false;
//        do {
//            log.trace("Submitting new flow ...");
//            AdpDBClientQueryStatus queryResult = dbClient.query(
//                    String.format("query_%s_%d",conditionTable, count++),
//                    queryString
//            );
//            if (queryResult.hasException()){
//                log.trace("Error while executing flow.");
//                throw new Exception(queryResult.getException());
//            }
//            hasToContinue = checkConditionTable(dbClient, conditionTable);
//        } while (hasToContinue);
//
//        exit(0);
//    }
//
//
//    private boolean checkConditionTable(AdpDBClient dbClient, String conditionTable) throws Exception {
//        InputStream inputStream = dbClient.readTable(conditionTable);
//        if (inputStream == null ){
//            throw new Exception("Unable to read condition table.");
//        }
//        String results = IOUtils.toString(inputStream, Charset.defaultCharset());
//        log.info("--+" + results);
//        return results.toUpperCase().contains("TRUE");
//    }
//
//    private AdpDBClientProperties createDBClientProperties(Parameters parameters) throws Exception {
//        String database = null;
//        for (Parameter parameter : parameters) {
//            log.info(parameter.getName() + " " + parameter.getValue());
//            if ( "database".equals(parameter.getName())){
//                database = parameter.getValue();
//            }
//        }
//        if (database == null) throw new Exception("Please provide valid parameters.");
//        return new AdpDBClientProperties(database);
//    }
//}
