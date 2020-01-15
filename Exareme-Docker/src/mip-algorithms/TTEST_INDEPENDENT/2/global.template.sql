requirevars 'defaultDB' 'input_global_tbl' 'hypothesis' 'effectsize' 'ci' 'y' 'meandiff' 'xlevels';
attach database '%{defaultDB}' as defaultDB;

 --var 'input_global_tbl' 'defaultDB.localstatistics2';

drop table if exists defaultDB.globalstatistics2;
create table  defaultDB.globalstatistics2 as
select colname, groupval, mean, std,  Ntotal, sum(sse) as sse
from %{input_global_tbl}
group by colname, groupval;

var 'distinctvaluesofx' from select group_concat(groupval) from (select distinct groupval as groupval from defaultDB.globalstatistics2 order by groupval);
var 'xlevels1' from select case when '%{xlevels}'<>'' then '%{xlevels}' else '%{distinctvaluesofx}' end;

drop table if exists defaultDB.globalttestresult;
create table defaultDB.globalttestresult as
select * from (ttest_independent colnames:%{y}  ylevels:%{xlevels1} effectsize:%{effectsize} ci:%{ci} meandiff:%{meandiff} hypothesis:%{hypothesis}
               select colname, groupval, mean, std,  Ntotal,sse from  defaultDB.globalstatistics2 order by colname,groupval);

var 'resultschema' from select outputschema from globalttestresult limit 1;
var 'typesofresults' from select create_complex_query("","real" , "," , "" , '%{resultschema}');
var 'typesofresults2' from select strreplace(mystring) from (select 'text,real,int,'||'%{typesofresults}' as mystring);

var 'jsonResult' from select '{ "type": "application/json", "data": ' || val ||'}' from
(select tabletojson( colname, t_value, df,%{resultschema}, "colname,t_value,df,%{resultschema}",0) as val
 from defaultDB.globalttestresult);

var 'tableResult' from select * from (totabulardataresourceformat title:INDEPENDENT_TEST_TABLE types:%{typesofresults2}
              select colname,t_value,df,%{resultschema} from defaultDB.globalttestresult);

select '{"result": [' || '%{jsonResult}' || ',' || '%{tableResult}' || ']}';
