package madgik.exareme.master.engine.executor.remote.operator.control;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import madgik.exareme.common.schema.QueryScript;
import madgik.exareme.common.schema.Select;
import madgik.exareme.common.schema.TableView;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.AdpDBManagerLocator;
import madgik.exareme.master.engine.dflSegment.ScriptSegment;
import madgik.exareme.master.engine.dflSegment.Segment;
import madgik.exareme.worker.art.concreteOperator.AbstractNiNo;
import madgik.exareme.worker.art.parameter.Parameter;
import madgik.exareme.worker.art.parameter.Parameters;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class DoWhile extends AbstractNiNo {

    private static Logger log = Logger.getLogger(DoWhile.class);
    private List<Segment> bodySegments;
    private QueryScript controlScript;
    private String current_cond_table;
    @Override
    public void run() throws Exception {

        log.trace("Parsing parameters ...");
        Type type;

        controlScript =  new Gson().fromJson(super.getParameterManager().getParameter("whileScript").get(0).getValue(),
                QueryScript.class);
        type = new TypeToken<ArrayList<ScriptSegment>>(){}.getType();
        bodySegments = (ArrayList<Segment>) new Gson().fromJson(
                super.getParameterManager().getParameter("SubSegments").get(0).getValue(), type);

        String conditionTable = getConditionTable(controlScript);
        current_cond_table = conditionTable;
        AdpDBClientProperties clientProperties = createDBClientProperties(super.getParameterManager().getParameters());
        log.trace("Creating new db client ...");
        AdpDBManager dbManager = AdpDBManagerLocator.getDBManager();
        AdpDBClient dbClient = AdpDBClientFactory.createDBClient(dbManager, clientProperties);

        List<String> dynamicTables = findDynamicTablesInBody();

        int count = 0;
        int niterations = 0;
        boolean hasToContinue =false;
        do {
            log.trace("Submitting new flow ...");



            AdpDBClientQueryStatus queryResult = dbClient.query("loop_body_step" + String.valueOf(niterations), bodySegments);

            if (queryResult.hasError()){
                log.trace("Error while executing flow.");
                throw new Exception(queryResult.getError());
            }

            queryResult = dbClient.query("loop_controlScript_step" + String.valueOf(niterations),
                    controlScript
            );
            if (queryResult.hasError()){
                log.trace("Error while executing flow.");
                throw new Exception(queryResult.getError());
            }
            hasToContinue = checkConditionTable(dbClient);
            niterations++;
            fixDynamicTableNamesPerStep(dynamicTables, conditionTable, niterations);

        } while (hasToContinue);

        exit(0);
    }

    private List<String> findDynamicTablesInBody(){
        ArrayList<String> dynamicTables = new ArrayList<>();
        for (Segment seg : bodySegments){
            List<Select> queries = seg.getQueryScript().getSelectQueries();

            for (Select query : queries){
                if (! query.getOutputTable().getTable().isTemp()) {
                    String dynTableName = query.getOutputTable().getTable().getName();
                    dynamicTables.add(dynTableName);
                    //fix output tables
                    query.getOutputTable().getTable().setName(dynTableName.concat("_step0"));
                    // break because only one table in each script will not be temporary
                    break;
                }
            }

        }

        return dynamicTables;
    }


    private void fixDynamicTableNamesPerStep(List<String> dynamicTables, String conditionTable, int step){
        int dynamicTablesIndex = 0;

        String outputSuffix = "_step".concat(new Integer(step).toString());
        String inputSuffix = "";
        String previousStepInputSuffix = "";

        if (step > 0)
            inputSuffix = "_step".concat(new Integer(step - 1).toString());

        if (step > 1)
            previousStepInputSuffix = "_step".concat(new Integer(step - 2).toString());


        for (Segment seg : bodySegments){
            List<Select> queries = seg.getQueryScript().getSelectQueries();

            for (Select query : queries){
                if (! query.getOutputTable().getTable().isTemp()) {
                    query.getOutputTable().getTable().setName(dynamicTables.get(dynamicTablesIndex).concat(outputSuffix));
                }

                String madisQuery = query.getQuery();
                for(TableView tv : query.getInputTables()){
                    String tableName = tv.getTable().getName();

                    for(String dynamicTableName : dynamicTables) {
                        if (tableName.equals(dynamicTableName.concat(previousStepInputSuffix))){
                            tv.getTable().setName(dynamicTableName.concat(inputSuffix));
                            query.renameInputTable(dynamicTableName.concat(previousStepInputSuffix), dynamicTableName.concat(inputSuffix));
                            // TODO replace might be dangerous
                            madisQuery = madisQuery.replace(dynamicTableName.concat(previousStepInputSuffix), dynamicTableName.concat(inputSuffix));
                            break;
                        }
                    }
                }
                query.setQuery(madisQuery);

            }
            dynamicTablesIndex++;
        }

        // control script
        for (Select query : controlScript.getSelectQueries()) {
            if (!query.getOutputTable().getTable().isTemp()) {
                query.getOutputTable().getTable().setName(conditionTable.concat(outputSuffix));
                current_cond_table = conditionTable.concat(outputSuffix);
            }

            String madisQuery = query.getQuery();
            for(TableView tv : query.getInputTables()){
                String tableName = tv.getTable().getName();

                for(String dynamicTableName : dynamicTables){
                    if(tableName.equals(dynamicTableName.concat(previousStepInputSuffix))){
                        tv.getTable().setName(dynamicTableName.concat(inputSuffix));
                        query.renameInputTable(dynamicTableName.concat(previousStepInputSuffix), dynamicTableName.concat(inputSuffix));

                        madisQuery = madisQuery.replace(dynamicTableName.concat(previousStepInputSuffix), dynamicTableName.concat(inputSuffix));
                        break;
                    }

                }
            }
            query.setQuery(madisQuery);

        }

    }

    private boolean checkConditionTable(AdpDBClient dbClient) throws Exception {
        InputStream inputStream = dbClient.readTable(current_cond_table);
        if (inputStream == null ){
            throw new Exception("Unable to read condition table.");
        }

        String results = IOUtils.toString(inputStream, Charset.defaultCharset());

        log.info("--+" + results);
        return results.toUpperCase().contains("TRUE");
    }

    private AdpDBClientProperties createDBClientProperties(Parameters parameters) throws Exception {
        String database = null;
        for (Parameter parameter : parameters) {
            log.info(parameter.getName() + " " + parameter.getValue());
            if ( "database".equals(parameter.getName())){
                database = parameter.getValue();
            }
        }
        if (database == null) throw new Exception("Please provide valid parameters.");
        return new AdpDBClientProperties(database);
    }

    private String getConditionTable(QueryScript controlScript) {
        List<Select> queries = controlScript.getSelectQueries();

        for (Select query : queries) {
            if (!query.getOutputTable().getTable().isTemp()) {
                return query.getOutputTable().getTable().getName();
            }
        }

        //TODO throw exception
        return null;
    }
}
