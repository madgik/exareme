/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.federation;

import madgik.exareme.master.queryProcessor.decomposer.query.Column;
import madgik.exareme.master.queryProcessor.decomposer.query.Output;
import madgik.exareme.master.queryProcessor.decomposer.query.SQLQuery;
import madgik.exareme.master.queryProcessor.decomposer.query.Table;
import madgik.exareme.master.queryProcessor.estimator.NodeSelectivityEstimator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dimitris
 */
class JoinQueryDecomposer {

    private SQLQuery outerJoin;
    ArrayList<SQLQuery> result;
    private ColumnsToTableNames c2t;
    private List<Column> allReferencedBaseColumns;
    private String database;
    private int partNo;
    private boolean centralized;
    private boolean multi;
    private boolean notNulls;
    private boolean pushRefCols;
    private NodeSelectivityEstimator nse;

    JoinQueryDecomposer(SQLQuery s, String db, int noOfparts, boolean centralizedExecution,
        boolean multiOpt, boolean addNotNulls, boolean pushRefCols, NodeSelectivityEstimator nse) {
        this.outerJoin = s;
        this.database = db;
        this.notNulls = addNotNulls;
        this.centralized = centralizedExecution;
        this.multi = multiOpt;
        this.partNo = noOfparts;
        this.pushRefCols = pushRefCols;
        this.nse = nse;
        allReferencedBaseColumns = new ArrayList<Column>();
        for (Column c : outerJoin.getAllSubqueryColumns()) {
            if (!allReferencedBaseColumns.contains(c)) {
                allReferencedBaseColumns.add(c);
            }
        }
        c2t = new ColumnsToTableNames();
        result = new ArrayList<SQLQuery>();
    }

    ArrayList<SQLQuery> getSubqueries() {
        outerJoin.setLeftJoinTable(decompose(outerJoin.getLeftJoinTable()));
        outerJoin.setRightJoinTable(decompose(outerJoin.getRightJoinTable()));

        //update output columns with temp table names
        for (Output o : outerJoin.getOutputs()) {
            for (Column c : o.getObject().getAllColumnRefs()) {
                boolean exists = false;
                for (Output o2 : outerJoin.getLeftJoinTable().getOutputs()) {
                    if ((c.tableAlias + "_" + c.columnName).equals(o2.getOutputName())) {
                        o.setObject(new Column(outerJoin.getLeftJoinTable().getResultTableName(),
                            o2.getOutputName()));
                        exists = true;
                    }
                }
                for (Output o2 : outerJoin.getRightJoinTable().getOutputs()) {
                    if ((c.tableAlias + "_" + c.columnName).equals(o2.getOutputName())) {
                        o.setObject(new Column(outerJoin.getRightJoinTable().getResultTableName(),
                            o2.getOutputName()));
                        exists = true;
                    }
                }
                if (!exists) {
                    System.out.println("not exists" + c.toString());
                    //exception: column does not exist in join tables
                }
                /*   if (c2t.getTablenameForColumn(c) != null) {
                 if(o.getObject() instanceof Column) {
                 o.setObject(new Column(c2t.getTablenameForColumn(c), c.tableAlias+"_"+c.columnName));
                
                 }
                 else{
                 o.getObject().changeColumn(c, new Column(c2t.getTablenameForColumn(c), c.tableAlias+"_"+c.columnName));
                 }}*/

            }
        }
        updateWhereColumns(outerJoin, true);
        //decomposeMultiWayJoins(outerJoin);


        // updateC2T(outerJoin);
        return result;
    }

    private SQLQuery decompose(SQLQuery q) {
        if (q.isBaseTable()) {
            Table input = q.getInputTables().get(0);
            if (input.isFederated()) {
                //create a query for q, else (input is local table) we don't have to do sth
                q.setFederated(true);
                q.setMadisFunctionString(
                    DBInfoReaderDB.dbInfo.getDB(input.getDBName()).getMadisString());
                for (Column c : allReferencedBaseColumns) {
                    if (c.tableAlias.equals(input.getAlias())) {
                        q.getOutputs().add(new Output(c.tableAlias + "_" + c.columnName,
                            new Column(c.tableAlias, c.columnName)));
                        c2t.putColumnInTable(c, q.getResultTableName());
                    }
                }
                result.add(q);
            }
            return q;
        } else {
            if (q.getLeftJoinTable() == null) {
                QueryDecomposer d = new QueryDecomposer(q, this.database, this.partNo, nse);
                ArrayList<SQLQuery> subqueries = d.getSubqueries();
                SQLQuery last = null;
                for (SQLQuery sub : subqueries) {
                    if (!result.contains(sub)) {
                        result.add(sub);
                    }
                    if (!sub.isTemporary()) {
                        last = sub;
                        sub.setTemporary(true);
                    }
                }
                //updateWhereColumns(last, false);

                // decomposeMultiWayJoins(q);

                updateC2T(last);
                return last;
            } else {
                if (!result.contains(q.getLeftJoinTable())) {
                    q.setLeftJoinTable(decompose(q.getLeftJoinTable()));
                }

                if (!result.contains(q.getRightJoinTable())) {
                    q.setRightJoinTable(decompose(q.getRightJoinTable()));
                }


                updateWhereColumns(q, false);

                // decomposeMultiWayJoins(q);

                updateC2T(q);
                result.add(q);
                return q;
            }
        }
    }

    private void updateWhereColumns(SQLQuery q, boolean isFinalJoin) {


        for (Column c : q.getWhereColumns()) {
            //for every every column of the where (on) clause track the table from which we must take it



            boolean exists = false;
            for (Output o2 : q.getLeftJoinTable().getOutputs()) {
                if ((c.tableAlias).equals(o2.getOutputName().split("_")[0])) {
                    if (!isFinalJoin && !q.getOutputs()
                        .contains(new Output(o2.getOutputName(), c))) {
                        q.getOutputs().add(new Output(o2.getOutputName(), c));
                    }
                    if ((c.tableAlias + "_" + c.columnName).equals(o2.getOutputName())) {
                        // o.setObject(new Column(outerJoin.leftJoinTable.getResultTableName(), o2.getOutputName()));
                        c.tableAlias = q.getLeftJoinTable().getResultTableName();
                        c.columnName = o2.getOutputName();
                        exists = true;
                    }



                }
            }
            for (Output o2 : q.getRightJoinTable().getOutputs()) {
                if ((c.tableAlias).equals(o2.getOutputName().split("_")[0])) {
                    if (!isFinalJoin && !q.getOutputs()
                        .contains(new Output(o2.getOutputName(), c))) {
                        q.getOutputs().add(new Output(o2.getOutputName(), c));
                    }
                    if ((c.tableAlias + "_" + c.columnName).equals(o2.getOutputName())) {
                        // o.setObject(new Column(outerJoin.leftJoinTable.getResultTableName(), o2.getOutputName()));
                        c.tableAlias = q.getRightJoinTable().getResultTableName();
                        c.columnName = o2.getOutputName();
                        exists = true;
                    }



                }
            }

            if (!exists) {
                System.out.println("not exists" + c.toString());
                //exception: column does not exist in join tables
            }




            /*if (c2t.getTablenameForColumn(c) != null) {
             String temporarySubqueryname = c2t.getTablenameForColumn(c);
             SQLQuery temporarySubquery = getTemporarySubquery(temporarySubqueryname);
             temporarySubquery.setPartitioningOnColum(c);
             for (Output o : temporarySubquery.outputs) {
             if (o.getOutputName().equals(c.tableAlias + "_" + c.columnName)) {
             c.tableAlias = temporarySubqueryname;
             c.columnName = o.getOutputName();
             if(!isFinalJoin && !q.outputs.contains(new Output(o.getOutputName(), c))){
             q.outputs.add(new Output(o.getOutputName(), c));
             }
             }
             }

             }*/
        }



    }

    private void updateC2T(SQLQuery q) {
        for (Column cout : q.getAllOutputColumns()) {
            //  for (Output o : q.getOutputs()) {
            //update c2t to track the output columns from q
            //   if (o.getObject() instanceof Column) {
            //   Column cout = (Column) o.getObject();
            // boolean needed = false;
            for (Column initial : this.allReferencedBaseColumns) {
                if (cout.columnName.equals(initial.tableAlias + "_" + initial.columnName)) {
                    //   needed = true;
                    c2t.putColumnInTable(initial, q.getResultTableName());
                    //break;
                }
            }
        }
        //   }


    }

    private SQLQuery getTemporarySubquery(String tablename) {
        for (SQLQuery q : this.result) {
            if (q.getResultTableName().equals(tablename)) {
                return q;
            }
        }
        return null;
    }

    /*  private void decomposeMultiWayJoins(SQLQuery q) {

     //check if the where columns come from more than two tables
     //if yes we must decompose q further
     if (q.getListOfJoinTables().size() > 2) {
     //decompose further
     String firstTable=q.getListOfJoinTables().get(0);
     //leave only fisrt table in q and add as second table a new table subQ wgich will contain the rest
     SQLQuery subQ=new SQLQuery();
     //add to subQ outputs that don't contain coliumns from firstTable
     //and rename other tables in q.outputs with the subQ table name
     List<Output> toRemove=new ArrayList<>();
     for(Output o:q.outputs){
     boolean containsOnlyColumnsFromFirsttable=true;
     boolean containsAtLeastAColumnFromFirstTable=false;
     for(Column c:o.getObject().getAllColumnRefs()){
     if (!c.tableAlias.equals(firstTable)){
     containsOnlyColumnsFromFirsttable=false;
     }
     else{
     containsAtLeastAColumnFromFirstTable=true;
     }
     if(!containsAtLeastAColumnFromFirstTable){
     //add output to subQ and remove from q
     subQ.outputs.add(new Output(o.getOutputName(), o.getObject()));
     toRemove.add(o);
                        
     }
     else if(!containsOnlyColumnsFromFirsttable){
     //rename other tables with the subQ table name
     o.getObject().changeColumn(c, new Column(subQ.getResultTableName(), c.columnName));
     }
     }
     }
     for(Output remove:toRemove){
     q.outputs.remove(remove);
     }
            
     for (UnaryWhereCondition uwc:q.unaryWhereConditions){
     //leave to q the uwc which are on columns from first table, add the rest to subQ
     if(!uwc.getColumn().tableAlias.equals(firstTable)){
     subQ.unaryWhereConditions.add(uwc);
     }
     }
     for(UnaryWhereCondition uwc:subQ.unaryWhereConditions){
     q.unaryWhereConditions.remove(uwc);
     }
            
            
     List<BinaryWhereCondition> bwcToRemove = new ArrayList<>();
     for(BinaryWhereCondition bwc:q.binaryWhereConditions){
     //move bwc to subQ if doesn't have a column from firstTable
     //rename table names to columns from other tables in bwc if bwc has columns from both from first
     //and other tables
     boolean containsOnlyColumnsFromFirsttable=true;
     boolean containsAtLeastAColumnFromFirstTable=false;
     for(Column c:bwc.getAllColumnRefs()){
     if (!c.tableAlias.equals(firstTable)){
     containsOnlyColumnsFromFirsttable=false;
     }
     else{
     containsAtLeastAColumnFromFirstTable=true;
     }
     if(!containsAtLeastAColumnFromFirstTable){
     //add output to subQ and remove from q
     subQ.binaryWhereConditions.add(bwc);
     bwcToRemove.add(bwc);
                        
     }
     else if(!containsOnlyColumnsFromFirsttable){
     //rename other tables with the subQ table name
     if(bwc.getLeftOp() instanceof Column && ((Column)bwc.getLeftOp()).equals(c)){
     bwc.setLeftOp(new Column(subQ.getResultTableName(), c.columnName));
     }
     else{
     bwc.getLeftOp().changeColumn(c, new Column(subQ.getResultTableName(), c.columnName));}
     if(bwc.getRightOp() instanceof Column && ((Column)bwc.getRightOp()).equals(c)){
     bwc.setRightOp(new Column(subQ.getResultTableName(), c.columnName));
     }
     else{
     bwc.getRightOp().changeColumn(c, new Column(subQ.getResultTableName(), c.columnName));
     }
     }
     }
     }
     for(BinaryWhereCondition bwc:bwcToRemove){
     q.binaryWhereConditions.remove(bwc);
     }
            
            
     subQ.joinType=q.joinType;
     subQ.leftJoinTable=getTemporarySubquery(subQ.getListOfJoinTables().get(0));
     subQ.rightJoinTable=getTemporarySubquery(subQ.getListOfJoinTables().get(1));
     q.rightJoinTable=getTemporarySubquery(q.getListOfJoinTables().get(0));
     q.leftJoinTable=subQ;
            
     //add output columns from join tables to subQ
     SQLQuery l=getTemporarySubquery(subQ.leftJoinTable.getResultTableName());
     for(Output o:l.outputs){
     Column c=(Column) o.getObject();
     Output o2=new Output(o.getOutputName(), new Column(l.getResultTableName(), c.columnName));
     if(!q.outputs.contains(o2)){
     q.outputs.add(o2);
     }
     }
     SQLQuery r=getTemporarySubquery(subQ.rightJoinTable.getResultTableName());
     for(Output o:r.outputs){
     Column c=(Column) o.getObject();
     Output o2=new Output(o.getOutputName(), new Column(r.getResultTableName(), c.columnName));
     if(!q.outputs.contains(o2)){
     q.outputs.add(o2);
     }
     }
            
     decompose(subQ);
            
     }
        
     if(q.getListOfJoinTables().size()==1){
     //CARTESIAN?
     }
     else{
     //change the join accordingly
     //take care not to change the join order
     //we condider that at least one join table is base table and doesn't have to change
     //TODO consider tha case that both tables are not base tables
     if(q.leftJoinTable.isBaseTable){
     if(getTemporarySubquery(q.getListOfJoinTables().get(0)).equals(q.leftJoinTable)){
     q.rightJoinTable=getTemporarySubquery(q.getListOfJoinTables().get(1));
     }
     else{
     q.rightJoinTable=getTemporarySubquery(q.getListOfJoinTables().get(0));
     }
     }
     if(q.rightJoinTable.isBaseTable){
     if(getTemporarySubquery(q.getListOfJoinTables().get(0)).equals(q.rightJoinTable)){
     q.leftJoinTable=getTemporarySubquery(q.getListOfJoinTables().get(1));
     }
     else{
     q.leftJoinTable=getTemporarySubquery(q.getListOfJoinTables().get(0));
     }
     }
            
     }
     }*/
}
