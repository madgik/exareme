requirevars 'input_global_tbl' 'defaultDB' ;
attach database '%{defaultDB}' as defaultDB;

drop table if exists global_oneconfusionmatrix;
create temp table global_oneconfusionmatrix as
select  iterationNumber, actualclass, predictedclass, sum(val) as val
from %{input_global_tbl}
group by actualclass,predictedclass;

--drop table if exists defaultDB.lala;
--create table defaultDB.lala as select * from global_oneconfusionmatrix;

var 'jsonResult' from select '{ "type": "application/json", "data": ' || componentresult || '}' from
( select tabletojson(actualclass,predictedclass,val, "actualclass,predictedclass,val",0)  as componentresult
from global_oneconfusionmatrix );

--var 'heatmap' from select highchartheatmap(actualclass,predictedclass,val,"confusion matrix", "actual values", "predicted values") from global_oneconfusionmatrix;

var 'heatmap' from select * from (highchartheatmap title:Confusion_Matrix, xtitle:Actual_Values, ytitle:Predicted_Values select actualclass,predictedclass,val from global_oneconfusionmatrix);

drop table if exists confusionmatrixstats;
create temp table confusionmatrixstats as
rconfusionmatrixtable select predictedclass,actualclass,val from global_oneconfusionmatrix;


var 'a'  from select statsval from confusionmatrixstats where statscolname = 'ResultOverall';
var 'b' from select statsval from confusionmatrixstats where statscolname = 'ResultClassNames';


select '{"result": [' || '%{jsonResult}' ||','||'%{heatmap}' ||','||'%{a}'||',' || '%{b}' ||']}';
