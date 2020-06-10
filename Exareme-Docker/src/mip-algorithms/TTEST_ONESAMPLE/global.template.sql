requirevars 'defaultDB' 'input_global_tbl' 'testvalue' 'hypothesis';
attach database '%{defaultDB}' as defaultDB;

drop table if exists globalstatistics;
create temp table  globalstatistics as
select colname, S1total/Ntotal as mean, SQROOT( FARITH('/', '-', '*', Ntotal, S2total, '*', S1total, S1total, '*', Ntotal, '-', Ntotal, 1)) as std, Ntotal
from (select colname, sum(S1) as S1total, sum(S2) as S2total, sum(N) as Ntotal
from %{input_global_tbl}
group by colname);

drop table if exists globalttestresult;
create temp table  globalttestresult as
select * from (ttest_onesample testvalue:%{testvalue} effectsize:1 ci:1 meandiff:1 hypothesis:%{hypothesis} sediff:0
               select * from   globalstatistics);

var 'resultschema' from select outputschema from globalttestresult limit 1;
var 'typesofresults' from select create_complex_query("","real" , "," , "" , '%{resultschema}');
var 'typesofresults2' from select strreplace(mystring) from (select 'text,real,int,'||'%{typesofresults}' as mystring);

var 'jsonResult' from select '{ "type": "application/json", "data": ' || val ||'}' from
(select tabletojson( colname, t_value, df,%{resultschema}, "colname,t_value,df,%{resultschema}",0) as val
 from globalttestresult);

var 'tableResult' from select * from (totabulardataresourceformat title:INDEPENDENT_TEST_TABLE types:%{typesofresults2}
              select colname,t_value,df,%{resultschema} from globalttestresult);

select '{"result": [' || '%{jsonResult}' || ',' || '%{tableResult}' || ']}';
