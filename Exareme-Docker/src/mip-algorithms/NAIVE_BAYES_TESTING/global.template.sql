requirevars 'input_global_tbl' 'defaultDB' ;
attach database '%{defaultDB}' as defaultDB;

drop table if exists global_oneconfusionmatrix;
create temp table global_oneconfusionmatrix as
select  iterationNumber, actualclass, predictedclass, sum(val) as val
from %{input_global_tbl}
group by actualclass,predictedclass;

var 'jsonResult' from select '{ "type": "application/json", "data": ' || componentresult || '}' from
( select tabletojson(actualclass,predictedclass,val, "actualclass,predictedclass,val",0)  as componentresult
from global_oneconfusionmatrix );

select '{"result": [' || '%{jsonResult}' || ']}';
